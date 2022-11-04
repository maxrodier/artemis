package ca.artemis.engine.core;

import ca.artemis.Configuration;
import ca.artemis.engine.core.Assertions.Level;

public class Logger {
    
    public static void logError(String message) {
        if(Configuration.LOG_LEVEL.ordinal() >= Level.ERROR.ordinal()) {
            log(Level.ERROR.name() + " : " + message);
        }
    }

    public static void logWarns(String message) {
        if(Configuration.LOG_LEVEL.ordinal() >= Level.WARNS.ordinal()) {
            log(Level.WARNS.name() + " : " + message);
        }
    }
    
    public static void logDebug(String message) {
        if(Configuration.LOG_LEVEL.ordinal() >= Level.DEBUG.ordinal()) {
            log(Level.DEBUG.name() + " : " + message);
        }
    }

    public static void logTrace(String message) {
        if(Configuration.LOG_LEVEL.ordinal() >= Level.TRACE.ordinal()) {
            log(Level.TRACE.name() + " : " + message);
        }
    }

    public static void log(Level level, String message) {
        if(Configuration.LOG_LEVEL.ordinal() >= level.ordinal()) {
            log(level.name() + " : " + message);
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
