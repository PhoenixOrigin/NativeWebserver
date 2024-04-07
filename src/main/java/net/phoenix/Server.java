package net.phoenix;

import net.phoenix.http.container.HttpResponse;
import net.phoenix.http.processors.IncomingRequest;
import net.phoenix.http.reflection.Route;
import net.phoenix.http.reflection.Router;
import net.phoenix.http.container.HttpRequest;
import net.phoenix.http.reflection.Webhandler;
import net.phoenix.logging.Logger;
import net.phoenix.logging.container.Log;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {

    private final ServerSocket socket;
    private final Executor threadPool;
    public static Logger logger;
    public static Class clazz;


    public Server(int port, Class clazz) {
        this(port, 500, clazz);
    }

    private Server(int port, int threadPoolSize, Class clazz) {
        Server.clazz = clazz;
        logger = new Logger(System.out);
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            logger.logError("Failed to start server due to: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void start() throws IOException {
        logger.logRaw("""
                \033[38;2;0;255;0m    _   __      __  _                   __
                   / | / /___ _/ /_(_)   _____         / /___ __   ______ _
                  /  |/ / __ `/ __/ / | / / _ \\   __  / / __ `/ | / / __ `/
                 / /|  / /_/ / /_/ /| |/ /  __/  / /_/ / /_/ /| |/ / /_/ /
                /_/ |_/\\__,_/\\__/_/ |___/\\___/   \\____/\\__,_/ |___/\\__,_/
                 _       __     __
                | |     / /__  / /_  ________  ______   _____  _____
                | | /| / / _ \\/ __ \\/ ___/ _ \\/ ___/ | / / _ \\/ ___/
                | |/ |/ /  __/ /_/ (__  )  __/ /   | |/ /  __/ /
                |__/|__/\\___/_.___/____/\\___/_/    |___/\\___/_/\033[0;0m
                """);
        try {
            Router.generateRoutes();
            for(Map.Entry<String, Method> entry : Router.getRoutes().entrySet()) {
                logger.logDebug("Route: " + entry.getKey() + " -> function " + entry.getValue().getName() + "()"
                        + " in " + entry.getValue().getDeclaringClass().getPackageName() + entry.getValue().getDeclaringClass().getName());
            }
        } catch (URISyntaxException | ClassNotFoundException e) {
            logger.logError("Failed to create routes due to: " + e.getMessage());
            throw new RuntimeException(e);
        }
        logger.logInfo("Server started on port " + socket.getLocalPort());
        while (true) {
            Socket clientConnection = socket.accept();
            handleConnection(clientConnection);
        }
    }

    private void handleConnection(Socket clientConnection) {
        Runnable httpRequestRunner = () -> {
            IncomingRequest.processRequest(clientConnection);
        };
        threadPool.execute(httpRequestRunner);
    }

}