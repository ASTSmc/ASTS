package tw.asts.mc.asts.util;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

final public class Log {
    private static Logger logger;
    public static void init(@NotNull Logger logger) {
        Log.logger = logger;
    }
    public static Logger get() {
        return logger;
    }
}
