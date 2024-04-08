package net.phoenix.logging.container;

import java.awt.Color;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a log.
 */
public class Log {
    private final Priority priority;
    private final String message;
    private final String timestamp;
    private final String loggerThread;
    private final String processId;
    private Color color;

    /**
     * Creates a new log.
     *
     * @param priority     The priority of the log
     * @param message      The message of the log
     * @param loggerThread The thread that logged the message
     */
    public Log(Priority priority, String message, String loggerThread) {
        this.priority = priority;
        this.message = message;
        this.timestamp = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss").format(new Date());
        this.loggerThread = loggerThread;
        this.processId = String.valueOf(ProcessHandle.current().pid());
        if (priority == Priority.DEBUG) {
            this.color = new Color(255, 255, 10);
        } else if (priority == Priority.INFO) {
            this.color = new Color(0, 255, 0);
        } else if (priority == Priority.WARN) {
            this.color = new Color(255, 255, 0);
        } else if (priority == Priority.ERROR) {
            this.color = new Color(255, 0, 0);
        }
    }

    /**
     * Generates the ANSI escape code for the color.
     *
     * @param r The red value of the color
     * @param g The green value of the color
     * @param b The blue value of the color
     * @return An ANSI escape code for the color
     */
    private String get_color_escape(int r, int g, int b) {
        return String.format("\033[%s;2;%s;%s;%sm", "38", r, g, b);
    }

    /**
     * Sends the log to an output stream.
     *
     * @param out The output stream to send the log to
     */
    public void send(PrintStream out) {
        out.println("[" + timestamp + "] " + "[" + loggerThread + ": " + processId + "] [" + priority + "] " + message);
        out.flush();
    }

    /**
     * Sends the log to an output stream and a log file.
     *
     * @param out     The output stream to send the log to
     * @param logFile The log file to write the log to
     */
    public void send(PrintStream out, PrintStream logFile) {
        out.println("[" + timestamp + "] " + "[" + loggerThread + ": " + processId + "] " + "[" + priority + "] " + get_color_escape(color.getRed(), color.getGreen(), color.getBlue()) + message + "\033[0;0m");
        logFile.println("[" + timestamp + "] [" + loggerThread + "] [" + priority + "] " + message);
        out.flush();
    }
}
