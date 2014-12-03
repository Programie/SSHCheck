package com.selfcoders.sshcheck;

import com.beust.jcommander.JCommander;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    private SSH ssh;

    public Main(CommandLineArguments commandLineArguments) {
        this.ssh = new SSH(commandLineArguments.privateKeyFiles, commandLineArguments.privateKeyPassphrase);

        JsonObject rootObject;
        try {
            JsonParser parser = new JsonParser();
            FileReader fileReader = new FileReader(commandLineArguments.serverListFile);
            rootObject = parser.parse(fileReader).getAsJsonObject();
            fileReader.close();
        } catch (IOException e) {
            ErrorLogger.log("Unable to parse server list file!");
            return;
        }

        JsonObject globalObject = null;
        if (rootObject.has("global") && rootObject.get("global").isJsonObject()) {
            globalObject = rootObject.getAsJsonObject("global");
        }

        JsonObject checkResults = new JsonObject();
        int ok = 0;
        int errors = 0;

        JsonArray servers = rootObject.getAsJsonArray("servers");
        for (int index = 0; index < servers.size(); index++) {
            JsonElement serverElement = servers.get(index);

            JsonObject serverObject = serverElement.getAsJsonObject();

            JsonObject checkResult = this.checkServer(serverObject, globalObject);

            checkResults.add(Integer.toString(index), checkResult);

            if (checkResult.get("ok").getAsBoolean()) {
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

    private JsonObject checkServer(JsonObject serverObject, JsonObject globalObject) {
        JsonElement usernameElement = serverObject.get("username");
        String username = null;

        if (usernameElement != null && !usernameElement.isJsonNull()) {
            username = usernameElement.getAsString();
        }

        if (username == null || username.isEmpty()) {
            if (globalObject == null) {
                username = System.getProperty("user.name");
            } else {
                JsonElement globalUsernameElement = globalObject.get("username");
                if (globalUsernameElement == null || globalUsernameElement.isJsonNull()) {
                    username = System.getProperty("user.name");
                } else {
                    username = globalObject.get("username").getAsString();
                }
            }
        }

        JsonObject checkResult = new JsonObject();
        checkResult.addProperty("ok", this.ssh.testConnection(username, serverObject.get("hostname").getAsString()));
        checkResult.addProperty("response", this.ssh.getCheckResult().trim());

        return checkResult;
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
