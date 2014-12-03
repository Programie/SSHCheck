package com.selfcoders.sshcheck;

import com.jcraft.jsch.Logger;

public class SSHLogger implements Logger {
    private String data;

    public SSHLogger() {
        this.startSession();
    }
    public boolean isEnabled(int level) {
        return true;
    }

    public void log(int level, String message) {
        this.data += message + "\n";
    }

    public String getData() {
        return this.data;
    }

    public void startSession() {
        this.data = "";
    }
}
