package net.phoenix.http;

import net.phoenix.http.container.HttpResponse;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {

    private final static Map<String, Method> routes = new HashMap<>();
    private final ServerSocket socket;
    private final Executor threadPool;

    public Server(int port) {
        threadPool = Executors.newFixedThreadPool(500);
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
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
            try {
                HttpRequest request = processRequest(clientConnection.getInputStream());
                Method runner = routes.get(request.method.concat(request.path).split("\\?")[0]);
                HttpResponse response;
                if (runner != null) {
                    response = (HttpResponse) runner.invoke(null, request);

                } else {
                    response = new HttpResponse.Builder().setStatusCode(404).build();
                }
                OutputStream outputStream = clientConnection.getOutputStream();
                outputStream.write(response.toString().getBytes());
                clientConnection.close();
            } catch (IOException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
        threadPool.execute(httpRequestRunner);
    }

    public HttpRequest processRequest(InputStream inputStream) {
        String headerTempData = contentToString(inputStream);
        String[] headerData = headerTempData.split("\r\n");
        String[] requestLine = headerData[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];
        String body = null;
        HashMap<String, String> headers = new HashMap<>();
        for (int i = 1; i < headerData.length - 1; i++) {
            String[] header = headerData[i].split(": ");
            if(headerData[i].isEmpty()) {
                try {
                    body = headerData[headerData.length - 1];
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
                break;
            } else {
                headers.put(header[0], header[1]);
            }
        }
        HashMap<String, String> params = new HashMap<>();
        if (path.contains("?")) {
            String[] pathParts = path.split("\\?");
            path = pathParts[0];
            String[] queryParams = pathParts[1].split("&");
            for (String queryParam : queryParams) {
                String[] queryParamParts = queryParam.split("=");
                params.put(queryParamParts[0], queryParamParts[1]);
            }
        }
        return new HttpRequest(headers, method, path, body, params);
    }

    public static String contentToString(InputStream inputStream) {
        StringBuilder headerTempData = new StringBuilder();
        Reader reader = new InputStreamReader(inputStream);
        try {
            int c;
            while ((c = reader.read()) != -1) {
                headerTempData.append((char) c);
                if(headerTempData.toString().contains("\r\n\r\n")) {
                    String[] headers = headerTempData.toString().split("\r\n");
                    int contentLength = -1;
                    for (String header : headers) {
                        if (header.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(header.split(":")[1].trim());
                            break;
                        }
                    }
                    if (contentLength > 0) {
                        char[] bodyBuffer = new char[contentLength];
                        int bytesRead = reader.read(bodyBuffer, 0, contentLength);
                        if (bytesRead != -1) {
                            headerTempData.append(bodyBuffer, 0, bytesRead);
                        }
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return headerTempData.toString();
    }


    public static void addRoute(final String opCode, final String route, final Method runner) {
        routes.put(opCode.concat(route), runner);
    }
}