package tw.asts.mc.asts.util;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

final public class Log {
    private static Logger logger;
    public static void init(@NotNull Logger logger) {
        Log.logger = logger;
    }
    public static Logger get() {
        return logger;
    }
    public static void info(String message) {
        get().info(message);
    }
    public static void warn(Throwable thrown) {
        warn(thrown.getMessage(), thrown.getCause());
    }
    public static void warn(String message, Throwable thrown) {
        get().log(Level.WARNING, message, thrown);
    }
}
