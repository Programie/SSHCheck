package com.selfcoders.sshcheck;

import com.beust.jcommander.JCommander;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.JSch;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    private ArrayList<Server> servers = new ArrayList<>();
    private String defaultUsername;

    public Main(CommandLineArguments commandLineArguments) {
        if (commandLineArguments.username != null) {
            defaultUsername = commandLineArguments.username;
        }

        if (!tryLoadServerlistFile(commandLineArguments.serverListFile)) {
            System.out.println("Unable to read server list file: " + commandLineArguments.serverListFile);
            return;
        }

        if (commandLineArguments.verbose) {
            JSch.setLogger(new VerboseLogger());
        }

        SSH ssh = new SSH(commandLineArguments.privateKeyFiles, commandLineArguments.privateKeyPassphrase);

        int ok = 0;
        int errors = 0;

        for (Server server : servers) {
            if (ssh.testConnection(server)) {
                ok++;
            } else {
                errors++;
            }
        }

        String resultMessage = "Checked " + String.valueOf(ok + errors) + " servers.\n\n";
        resultMessage += "OK: " + String.valueOf(ok) + "\n";
        resultMessage += "Errors: " + String.valueOf(errors);

        System.out.println(resultMessage);
    }

    private boolean tryLoadServerlistFile(String filename) {
        if (loadServerlistAsJson(filename)) {
            return true;
        }

        if (loadServerlistAsPlaintext(filename)) {
            return true;
        }

        return false;
    }

    private boolean loadServerlistAsJson(String filename) {
        JsonObject rootObject;

        try {
            JsonParser parser = new JsonParser();
            FileReader fileReader = new FileReader(filename);
            rootObject = parser.parse(fileReader).getAsJsonObject();
            fileReader.close();
        } catch (IOException e) {
            return false;
        } catch (JsonSyntaxException e) {
            return false;
        }

        if (rootObject.has("global") && rootObject.get("global").isJsonObject()) {
            JsonObject globalObject = rootObject.getAsJsonObject("global");

            if (globalObject != null) {
                JsonElement globalUsernameElement = globalObject.get("username");
                if (globalUsernameElement != null) {
                    defaultUsername = globalUsernameElement.getAsString();
                }
            }
        }

        String globalUsername = defaultUsername;

        if (globalUsername == null) {
            globalUsername = System.getProperty("user.name");
        }

        JsonArray serversArray = rootObject.getAsJsonArray("servers");
        for (int index = 0; index < serversArray.size(); index++) {
            JsonObject serverObject = serversArray.get(index).getAsJsonObject();

            JsonElement hostnameElement = serverObject.get("hostname");
            JsonElement usernameElement = serverObject.get("username");

            if (hostnameElement == null) {
                System.out.println("Missing hostname property!");
                continue;
            }

            String username;
            String hostname = hostnameElement.getAsString();

            if (usernameElement == null) {
                username = globalUsername;
            } else {
                username = usernameElement.getAsString();
            }

            servers.add(new Server(hostname, username));
        }

        return true;
    }

    private boolean loadServerlistAsPlaintext(String filename) {
        String globalUsername = defaultUsername;

        if (globalUsername == null) {
            globalUsername = System.getProperty("user.name");
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("#") || line.startsWith(";") || line.startsWith("//")) {
                    continue;
                }

                String hostname;
                String username;

                int atIndex = line.indexOf("@");
                if (atIndex == -1) {
                    hostname = line;
                    username = globalUsername;
                } else {
                    hostname = line.substring(atIndex + 1).trim();
                    username = line.substring(0, atIndex).trim();
                }

                if (hostname.isEmpty() || username.isEmpty()) {
                    continue;
                }

                servers.add(new Server(hostname, username));
            }

            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        CommandLineArguments commandLineArguments = new CommandLineArguments();
        JCommander jCommander = new JCommander(commandLineArguments, args);

        if (commandLineArguments.showHelp) {
            jCommander.usage();
            return;
        }

        new Main(commandLineArguments);
    }
}
