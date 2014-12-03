package com.selfcoders.sshcheck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

public class WindowsRegistry {
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;
    public static final int REG_SUCCESS = 0;

    private static final int KEY_READ = 0x20019;
    private static Preferences userRoot = Preferences.userRoot();
    private static Preferences systemRoot = Preferences.systemRoot();
    private static Class<? extends Preferences> userClass = userRoot.getClass();
    private static Method regOpenKey = null;
    private static Method regCloseKey = null;
    private static Method regQueryValueEx = null;

    static {
        try {
            regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", new Class[] { int.class, byte[].class, int.class });
            regOpenKey.setAccessible(true);
            regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", new Class[] { int.class });
            regCloseKey.setAccessible(true);
            regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", new Class[] { int.class, byte[].class });
            regQueryValueEx.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WindowsRegistry() {}

    /**
     * Read a value from key and value name
     * @param hkey   HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key
     * @param valueName
     * @return the value
     * @throws IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public static String readString(int hkey, String key, String valueName) throws IllegalAccessException, InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readString(systemRoot, hkey, key, valueName);
        } else if (hkey == HKEY_CURRENT_USER) {
            return readString(userRoot, hkey, key, valueName);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    private static String readString(Preferences root, int hkey, String key, String value) throws IllegalAccessException,
            InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_READ);
        if (handles[1] != REG_SUCCESS) {
            return null;
        }

        byte[] valb = (byte[]) regQueryValueEx.invoke(root, handles[0], toCstr(value));
        regCloseKey.invoke(root, handles[0]);
        return (valb != null ? new String(valb).trim() : null);
    }

    private static byte[] toCstr(String str) {
        byte[] result = new byte[str.length() + 1];

        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }

        result[str.length()] = 0;
        return result;
    }
}
