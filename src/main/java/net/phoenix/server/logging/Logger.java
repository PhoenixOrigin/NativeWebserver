package net.phoenix.server.logging;

import net.phoenix.server.logging.container.Log;
import net.phoenix.server.logging.container.Priority;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A logger implementation.
 */
public class Logger {

    private final PrintStream out;
    private final String threadName;
    private final @NotNull PrintStream logFile;
    private final @NotNull PrintStream accessLogs;

    public Logger(PrintStream out) {
        this(out, Thread.currentThread().getName(), new File("./logs/" + DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH).format(new Date()).replace(" ", "-").replace(",", "").toLowerCase() + ".log"), new File("./logs/access-log-" + DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH).format(new Date()).replace(",", "").replace(" ", "-").toLowerCase() + ".log"));
    }

    /**
     * Creates a new logger.
     *
     * @param out        The output stream to log to
     * @param threadName The name of the thread that is logging
     * @param logFile    The file to log to
     * @param accessLogs The file to log access logs to
     */
    public Logger(PrintStream out, String threadName, @NotNull File logFile, @NotNull File accessLogs) {
        this.out = out;
        this.threadName = threadName;
        this.logFile = processFile(logFile);
        this.accessLogs = processFile(accessLogs);
        Runtime.getRuntime().addShutdownHook(new Thread(this.logFile::close));
        Runtime.getRuntime().addShutdownHook(new Thread(this.accessLogs::close));
    }

    /**
     * Processes a file.
     *
     * @param toProcess The file to process
     * @return The processed file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private PrintStream processFile(File toProcess) {
        try {
            if (toProcess.exists()) {
                int i = 1;
                while (toProcess.exists()) {
                    toProcess = new File("./logs/" + DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH).format(new Date()).replace(" ", "-").replace(",", "").toLowerCase() + "-" + i + ".log");
                    i++;
                }
            }
            toProcess.createNewFile();
        } catch (IOException e) {
            logException(e);
        }
        try {
            return new PrintStream(new FileOutputStream(toProcess, false));
        } catch (FileNotFoundException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Logs a raw message to the output stream and the log file.
     *
     * @param message The message to log
     */
    public void logRaw(String message) {
        out.println(message);
        logFile.println(message);
    }

    /**
     * Logs a debug message.
     *
     * @param message The message to log
     */
    public void logDebug(String message) {
        new Log(Priority.DEBUG, message, threadName).send(out, logFile);
    }

    /**
     * Logs an info message.
     *
     * @param message The message to log
     */
    public void logInfo(String message) {
        new Log(Priority.INFO, message, threadName).send(out, logFile);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    public void logWarn(String message) {
        new Log(Priority.WARN, message, threadName).send(out, logFile);
    }

    /**
     * Logs an error message.
     *
     * @param message The message to log
     */
    public void logError(String message) {
        new Log(Priority.ERROR, message, threadName).send(out, logFile);
    }

    /**
     * Logs an exception.
     *
     * @param e The exception to log
     */
    public void logException(@NotNull Exception e) {
        new Log(Priority.ERROR, e.getMessage(), threadName).send(out, logFile);
    }

    /**
     * Logs a connection.
     *
     * @param ip   The IP address of the connection
     * @param path The path of the connection
     */
    public void logConnection(String ip, String path) {
        new Log(Priority.INFO, "Connection from " + ip + " to " + path, "server-thread").send(accessLogs);
    }

    /**
     * Shuts down the logger.
     */
    public void shutdown() {
        logFile.close();
        accessLogs.close();
    }

}
