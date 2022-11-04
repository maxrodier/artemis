package ca.artemis.engine.core;

public class Assertions {

    public static boolean assertNull(Object object, Level level, String message) {
        if(object != null) {
            return failure(level, message);
        }
        return true;
    }

    public static boolean assertNotNull(Object object, Level level, String message) {
        if(object == null) {
            return failure(level, message);
        }
        return true;
    }

    public static boolean assertTrue(boolean value, Level level, String message) {
        if(!value) {
            return failure(level, message);
        }
        return true;
    }

    public static boolean assertFalse(boolean value, Level level, String message) {
        if(value) {
            return failure(level, message);
        }
        return true;
    }

    private static boolean failure(Level level, String message) {
        switch(level) {
            case FATAL:
                throw new AssertionError(message);
            default:
                Logger.log(level, message);
                break;   
        }

        return false;
    } 

    public enum Level {
        FATAL,
        ERROR,
        WARNS,
        DEBUG,
        TRACE;
    }
}
