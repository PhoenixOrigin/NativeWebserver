package net.phoenix.http;

import net.phoenix.http.container.HttpResponse;
import net.phoenix.http.processors.IncomingRequest;
import net.phoenix.http.reflection.Route;
import net.phoenix.http.reflection.Router;
import net.phoenix.http.container.HttpRequest;
import net.phoenix.http.reflection.Webhandler;

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

    public Server(int port) {
        threadPool = Executors.newFixedThreadPool(500);
        try {
            socket = new ServerSocket(port);
            Router.generateRoutes();
        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void start() throws IOException {
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