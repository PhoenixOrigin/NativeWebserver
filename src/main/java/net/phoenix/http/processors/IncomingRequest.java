package net.phoenix.http.processors;

import net.phoenix.Server;
import net.phoenix.http.builder.HttpResponseBuilder;
import net.phoenix.http.container.HttpRequest;
import net.phoenix.http.container.HttpResponse;
import net.phoenix.http.reflection.Router;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.Files;

public class IncomingRequest {

    public static void processRequest(Socket clientConnection) {
        try {
            HttpRequest request = IncomingRequestDecoder.processRequest(clientConnection.getInputStream());
            if(request.path.contains("..")) {
                clientConnection.getOutputStream().write(getError(403).toString().getBytes());
                clientConnection.close();
                return;
            }
            Method runner = Router.route(request.method, request.path.split("\\?")[0]);
            Server.logger.logConnection(clientConnection.getInetAddress().getHostAddress(), request.path);
            HttpResponse response;
            if (runner != null) {
                Object r =  runner.invoke(null, request);
                if (r instanceof HttpResponse) {
                    response = (HttpResponse) r;
                } else if (r instanceof String) {
                    try {
                        response = getFile((String) r);
                    } catch (NullPointerException e) {
                        response = getError(404);
                    }
                } else {
                    response = new HttpResponseBuilder().setStatusCode(500).build();
                }
            } else {
                try {
                    response = getFile(request.path.split("\\?")[0]);
                } catch (NullPointerException e) {
                    response = getError(404);
                }
            }
            OutputStream outputStream = clientConnection.getOutputStream();
            outputStream.write(response.toString().getBytes());
            response.writeInputStream(outputStream);
            clientConnection.close();
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpResponse getError(int code) {
        HttpResponse response;
        try {
            File f = new File(Server.clazz.getResource("/static/" + code + ".html").getFile());
            String type = Files.probeContentType(f.toPath());
            HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
            responseBuilder.setStatusCode(code);
            responseBuilder.addHeader("Content-Type", type);
            responseBuilder.addHeader("Content-Disposition", type.contains("text") || type == "application/javascript" ? "inline" : "attachment" + "; filename=" + code + ".html");
            responseBuilder.setEntity(new FileInputStream(f));
            response = responseBuilder.build();
        } catch (NullPointerException | IOException e2) {
            response = new HttpResponseBuilder().setStatusCode(code).build();
        }
        return response;
    }

    private static HttpResponse getFile(String path) throws IOException, NullPointerException {
        HttpResponse response;
            File f = new File(Server.clazz.getResource("/static/" + path).getFile());
            String type = Files.probeContentType(f.toPath());
            HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
            responseBuilder.setStatusCode(200);
            responseBuilder.addHeader("Content-Type", type);
            responseBuilder.addHeader("Content-Disposition", type.contains("text") || type == "application/javascript" ? "inline" : "attachment" + "; filename=" + path);
            responseBuilder.setEntity(new FileInputStream(f));
            response = responseBuilder.build();
        return response;
    }
}
