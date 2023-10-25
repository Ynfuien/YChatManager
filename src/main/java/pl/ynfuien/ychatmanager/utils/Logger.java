package pl.ynfuien.ychatmanager.utils;

import org.bukkit.Bukkit;

import java.util.HashMap;

public class Logger {
    private static String prefix;

    public static void setPrefix(String prefix) {
        Logger.prefix = prefix;
    }

    public static void log(String message) {
        Messenger.send(Bukkit.getConsoleSender(), prefix + message);
    }

    public static void log(String message, HashMap<String, Object> placeholders) {
        Messenger.send(Bukkit.getConsoleSender(), prefix + message, placeholders);
    }

    public static void logWarning(String message) {
        log("<yellow>" + message);
    }

    public static void logError(String message) {
        log("<red>" + message);
    }
}
