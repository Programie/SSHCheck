package com.selfcoders.sshcheck;

import com.beust.jcommander.Parameter;
import java.io.File;
import java.util.List;

public class CommandLineArguments {
    @Parameter(names = {"-h", "--help"}, description = "Show this help message", help = true)
    public boolean showHelp;

    @Parameter(names = {"-p", "--passphrase"}, description = "Specify the passphrase of the private SSH key")
    public String privateKeyPassphrase;

    @Parameter(names = {"-k", "--privatekey"}, description = "Specify the path(s) to your private SSH key(s)")
    public List<String> privateKeyFiles;

    @Parameter(names = {"-s", "--serverlist"}, description = "Specify a file containing a list of servers.")
    public String serverListFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() +
            "/servers.json";
}
