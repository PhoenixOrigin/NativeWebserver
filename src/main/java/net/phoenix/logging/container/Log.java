package net.phoenix.logging.container;

import java.awt.*;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private final Priority priority;
    private final String message;
    private final String timestamp;
    private final String loggerThread;
    private Color color;
    private final String processId;

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

    private String get_color_escape(int r, int g, int b) {
        return String.format("\033[%s;2;%s;%s;%sm", "38", r, g, b);
    }

    public void send(PrintStream out) {
        out.println("[" + timestamp + "] " + "[" + loggerThread + ": " + processId + "] [" + priority + "] " + message);
        out.flush();
    }

    public void send(PrintStream out, PrintStream logFile) {
        out.println("[" + timestamp + "] " + "[" + loggerThread + ": " + processId + "] " + "[" + priority + "] " + get_color_escape(color.getRed(), color.getGreen(), color.getBlue()) + message + "\033[0;0m");
        logFile.println("[" + timestamp + "] [" + loggerThread + "] [" + priority + "] " + message);
        out.flush();
    }
}
