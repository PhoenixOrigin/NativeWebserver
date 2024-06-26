package net.phoenix.server;

import net.phoenix.server.http.RequestHandler;
import net.phoenix.server.http.reflection.Route;
import net.phoenix.server.http.reflection.Router;
import net.phoenix.server.http.reflection.WebHandler;
import net.phoenix.server.logging.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * The core HTTP server class. <br> <br>
 * This class is used to start the server and handle incoming requests. <br> <br>
 * Starting the server is as simple as <br> <br>
 * <code>
 * Server server = new Server(port, Main.class); <br>
 * server.start();
 * </code>
 * To create a route, simply annotate a method with the {@link Route} annotation and annotate the class with the {@link WebHandler} annotation. <br> <br>
 */
public class Server {

    public static @Nullable AsynchronousServerSocketChannel socket = null;
    public static Logger logger;
    public static Class<?> clazz;

    /**
     * Creates a new server instance.
     *
     * @param port  The port to start the server on
     * @param clazz The class to scan for routes
     */
    public Server(int port, Class<?> clazz) {
        Server.clazz = clazz;
        logger = new Logger(System.out);
        try {
            socket = AsynchronousServerSocketChannel.open();
            socket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            logger.logError("Failed to start server due to: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks the stack trace for a call to System.exit().
     *
     * @return The stack trace element that called System.exit(), or null if it was not called
     */
    private static @Nullable String checkStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().equals("java.lang.System") &&
                    element.getMethodName().equals("exit")) {
                return element.toString();
            }
        }
        return null;
    }

    /**
     * Starts the server.
     *
     * @throws IOException If the server fails to start
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue"})
    public void start() throws IOException {
        logger.logRaw("""
                \033[38;2;0;255;0m
                    _   __      __  _                   __
                   / | / /___ _/ /_(_)   _____         / /___ __   ______ _
                  /  |/ / __ `/ __/ / | / / _ \\   __  / / __ `/ | / / __ `/
                 / /|  / /_/ / /_/ /| |/ /  __/  / /_/ / /_/ /| |/ / /_/ /
                /_/ |_/\\__,_/\\__/_/ |___/\\___/   \\____/\\__,_/ |___/\\__,_/
                 _       __     __
                | |     / /__  / /_  ________  ______   _____  _____
                | | /| / / _ \\/ __ \\/ ___/ _ \\/ ___/ | / / _ \\/ ___/
                | |/ |/ /  __/ /_/ (__  )  __/ /   | |/ /  __/ /
                |__/|__/\\___/_.___/____/\\___/_/    |___/\\___/_/
                \033[0;0m
                """);
        String processId = String.valueOf(ProcessHandle.current().pid());
        String user = System.getProperty("user.name");
        logger.logInfo("Server starting by user " + user + " with PID " + processId);
        logger.logDebug("Scanning for routes in package \"" + clazz.getPackageName() + "\" initialised by " + clazz.getName());
        try {
            Router.generateRoutes();
            for (Map.Entry<String, Route> entry : Router.getRoutes().entrySet()) {
                assert entry.getValue().getMethod() != null;
                logger.logDebug("Route: " + entry.getKey() + " -> function " + entry.getValue().getPath() + "()"
                        + " in " + entry.getValue().getMethod().getDeclaringClass().getPackageName() + entry.getValue().getMethod().getDeclaringClass().getName());
            }
        } catch (URISyntaxException | ClassNotFoundException e) {
            logger.logError("Failed to create routes due to: " + e.getMessage());
            throw new RuntimeException(e);
        }
        logger.logDebug("Routes generated successfully");

        logger.logInfo("Registering shutdown hooks");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                assert socket != null;
                socket.close();
            } catch (IOException e) {
                logger.logError("Failed to close server socket due to: " + e.getMessage());
            }
            String stackTrace = checkStackTrace();
            if (stackTrace != null) {
                logger.logWarn("Server stopped by use of System.exit() in " + stackTrace);
            } else {
                logger.logInfo("Server stopped at " + new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss").format(new Date()) + " by " + System.getProperty("user.name"));
            }
            logger.shutdown();
        }));
        logger.logInfo("Shutdown hooks registered successfully");
        logger.logInfo("Server started at " + new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss").format(new Date()) + " on " + socket.getLocalAddress());
        logger.logInfo("Starting server-thread");
        Thread thread = new Thread(() -> {
            while (true) {
                socket.accept(null, new RequestHandler());
                try {
                    System.in.read();
                } catch (IOException e) {
                    logger.logError("Failed to read from System.in due to: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setName("Server-Thread");
        thread.start();
        logger.logInfo("server-thread started successfully");
        logger.logInfo("Server is now listening for incoming connections");

    }

}