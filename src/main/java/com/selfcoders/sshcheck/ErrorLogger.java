package com.selfcoders.sshcheck;

public class ErrorLogger {
    private ErrorLogger() {
        // Fix for "Utility classes should not have a public or default constructor."
    }

    public static void log(String message) {
        System.out.println(message);

        System.exit(1);
    }
}
