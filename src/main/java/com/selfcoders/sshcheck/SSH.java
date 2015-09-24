package com.selfcoders.sshcheck;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SSH extends JSch {
    private List<String> privateKeyFiles;
    private String privateKeyPassphrase;

    public SSH(List<String> privateKeyFiles, String privateKeyPassphrase) {
        this.privateKeyFiles = privateKeyFiles == null ? new ArrayList<String>() : privateKeyFiles;
        this.privateKeyPassphrase = privateKeyPassphrase;

        this.addPrivateKeys();
    }

    private void addPrivateKey(String filename) throws JSchException {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        File file = new File(filename);
        if (file.exists()) {
            System.out.println("Adding private key: " + filename);

            KeyPair keyPair = KeyPair.load(this, filename);
            if (keyPair.isEncrypted()) {
                this.tryDecryptPrivateKey(filename, keyPair);
                this.addIdentity(file.getAbsolutePath(), this.privateKeyPassphrase);
            } else {
                this.addIdentity(file.getAbsolutePath());
            }
        }
    }

    private void addPrivateKeys() {
        try {
            // Add specified private key files
            for (String filename : this.privateKeyFiles) {
                File file = new File(filename);
                if (file.isDirectory()) {
                    this.addPrivateKeys(filename);
                } else {
                    this.addPrivateKey(filename);
                }
            }

            // Add private key files from <home-directory>/.ssh
            this.addPrivateKeys(System.getProperty("user.home") + File.separator + ".ssh");

            // Add private key files from <cygwin-home-directory>/.ssh
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                String cygwinRootDir = null;

                try {
                    cygwinRootDir = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Cygwin\\setup", "rootdir");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }

                if (cygwinRootDir != null) {
                    String passwdPath = cygwinRootDir + File.separator + "etc" + File.separator + "passwd";
                    BufferedReader reader = new BufferedReader(new FileReader(passwdPath));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] splittedLine = line.split(":");
                        if (splittedLine.length >= 6 && splittedLine[0].equalsIgnoreCase(System.getProperty("user.name"))) {
                            String homePath = splittedLine[5];
                            if (homePath.startsWith("/cygdrive/")) {
                                homePath = homePath.replaceFirst("^/cygdrive/([a-z])", "$1:");
                            } else {
                                homePath = cygwinRootDir + "/" + homePath;
                            }

                            this.addPrivateKeys(homePath.replace("/", File.separator) + File.separator + ".ssh");
                        }
                    }
                }
            }
        } catch (JSchException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void addPrivateKeys(String path) throws JSchException {
        this.addPrivateKey(path + File.separator + "id_rsa");
        this.addPrivateKey(path + File.separator + "id_dsa");
    }

    private void tryDecryptPrivateKey(String filename, KeyPair keyPair) {
        if (this.privateKeyPassphrase == null || this.privateKeyPassphrase.isEmpty() || !keyPair.decrypt(this.privateKeyPassphrase)) {
            char[] passphrase = System.console().readPassword("Enter passphrase for key '" + filename + "': ");
            if (passphrase == null) {
                System.exit(1);
            }
            this.privateKeyPassphrase = new String(passphrase);

            this.tryDecryptPrivateKey(filename, keyPair);
        }
    }

    public boolean testConnection(Server server) {
        System.out.print("Testing '" + server.username + "@" + server.hostname + "': ");

        try {
            Session session = this.getSession(server.username, server.hostname);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
            session.disconnect();

            System.out.println("OK");

            return true;
        } catch (JSchException e) {
            System.out.println("ERROR (" + e.getMessage() + ")");

            return false;
        }
    }
}
