package net.phoenix.logging.container;

import net.phoenix.logging.Logger;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {
    private final Priority priority;
    private final String message;
    private String timestamp;
    private String loggerThread;
    private Color color;

    public Log(Priority priority, String message, String timestamp, String loggerThread, Color color) {
        this.priority = priority;
        this.message = message;
        this.timestamp = timestamp;
        this.loggerThread = loggerThread;
        this.color = color;
    }

    public Log(Priority priority, String message, String timestamp, String loggerThread) {
        this.priority = priority;
        this.message = message;
        this.timestamp = timestamp;
        this.loggerThread = loggerThread;

        if (priority == Priority.DEBUG) {
            this.color = new Color(0, 0, 255);
        } else if (priority == Priority.INFO) {
            this.color = new Color(0, 255, 0);
        } else if (priority == Priority.WARN) {
            this.color = new Color(255, 255, 0);
        } else if (priority == Priority.ERROR) {
            this.color = new Color(255, 0, 0);
        }
    }

    public Log(Priority priority, String message, String loggerThread) {
        this.priority = priority;
        this.message = message;
        this.timestamp = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss").format(new Date());
        this.loggerThread = loggerThread;

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

    public Priority getPriority() {
        return priority;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLoggerThread() {
        return loggerThread;
    }

    public Color getColor() {
        return color;
    }

    private String get_color_escape(int r, int g, int b) {
        return String.format("\033[%s;2;%s;%s;%sm", "38", r, g, b);
    }

    public void send(PrintStream out) {
        out.println("[" + timestamp + "] "  + "[" + loggerThread + "] [" + priority + "] " + message);
        out.flush();
    }

    public void send(PrintStream out, PrintStream logFile) {
        out.println("[" + timestamp + "] " + "[" + loggerThread + "] [" + priority + "] " + get_color_escape(color.getRed(), color.getGreen(), color.getBlue()) + message + "\033[0;0m");
        logFile.println("[" + timestamp + "] [" + loggerThread + "] [" + priority + "] " + message);
        out.flush();
    }
}
