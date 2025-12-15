package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class Log {

    public final static String[] loglevels = { "INFO ", "WARN ", "ERROR", "DEBUG", "RARE " };
    private static boolean isFormatLong = true;
    private final static Consumer<Object> log = System.out::println;
    private static int loglevel = 0; // INFO
    private static int previousLogLevel = 0;
    private static boolean isStrict = false;
    private static boolean reverseOrder = false;

    public static void toggleReverseOrder() {
        reverseOrder = !reverseOrder;
    }

    public static void toggleDateFormat() {
        isFormatLong = !isFormatLong;
    }

    public static void setLevel(int level) {
        setLevel(level, false);
    }

    public static void suppressLevels() {
        if (!isStrict) {
            previousLogLevel = loglevel;
            loglevel = loglevels.length; // no logs
        }
        isStrict = true;
    }

    public static void unsuppressLevels() {
        if (isStrict) {
            loglevel = previousLogLevel;
        }
        isStrict = false;
    }

    public static void setLevel(int level, boolean strict) {
        if (level < 0 || level >= loglevels.length) {
            loglevel = 0; // INFO
        }
        loglevel = level;
        isStrict = strict;
    }

    public static void log(String message, int level) {
        log((Object) message, level);
    }

    public static void log(Object object, int level) {
        if (isStrict) {
            if (level == loglevel) {
                handleLog(object, loglevels[level]);
            }
        } else {
            if (reverseOrder) {
                if (level <= loglevel) {
                    handleLog(object, loglevels[level]);
                }
            } else {
                if (level >= loglevel) {
                    handleLog(object, loglevels[level]);
                }
            }
        }
    }

    public static void log(String message) {
        log((Object) message);
    }

    public static void log(Object object) {
        log(object, 0);
    }

    public static void info(String message) {
        info((Object) message);
    }

    public static void info(Object object) {
        log(object, 0);
    }

    public static void warn(String message) {
        warn((Object) message);
    }

    public static void warn(Object object) {
        log(Log.paintString(object, Log.getColor(1)), 1);
    }

    public static void error(String message) {
        error((Object) message);
    }

    public static void error(Object object) {
        log(Log.paintString(object, Log.getColor(2)), 2);
    }

    public static void debug(String message) {
        debug((Object) message);
    }

    public static void debug(Object object) {
        log(Log.paintString(object, Log.getColor(3)), 3);
    }

    public static void rare(String message) {
        rare((Object) message);
    }

    public static void rare(Object object) {
        log(Log.paintString(object, Log.getColor(4)), 4);
    }

    public static void always(String message) {
        always((Object) message);
    }

    public static void always(Object object) {
        handleLog(paintString(object, getColor(5)), "ALWYS");
    }

    private static String getDateWithTime() {
        // [yyyy-mm-dd hh:mm:ss.sss]
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter;
        if (isFormatLong) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        } else {
            formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        }
        return "[" + now.format(formatter) + "]";
    }

    private static Object paintString(Object message, String colorCode) {
        String RESET = "\u001B[0m";
        return colorCode + message + RESET;
    }

    private static void handleLog(Object object, String level) {
        log.accept("[" + level + "] " + Log.getDateWithTime() + " " + object);
    }

    private static String getColor(int color) {
        switch (color) {
            case 0:
                return "\u001B[37m"; // WHITE
            case 1:
                return "\u001B[33m"; // YELLOW
            case 2:
                return "\u001B[31m"; // RED
            case 3:
                return "\u001B[32m"; // GREEN
            case 4:
                return "\u001B[35m"; // PURPLE
            case 5:
                return "\u001B[36m"; // CYAN
            case 6:
                return "\u001B[34m"; // BLUE
            default:
                return "\u001B[0m"; // RESET
        }
    }

}
