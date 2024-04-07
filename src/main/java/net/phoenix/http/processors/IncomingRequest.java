package net.phoenix.http.processors;

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
            Method runner = Router.route(request.method, request.path.split("\\?")[0]);
            HttpResponse response = null;
            if (runner != null) {
                Object r =  runner.invoke(null, request);
                if (r instanceof HttpResponse) {
                    response = (HttpResponse) r;
                } else if (r instanceof String) {
                    File f = new File(runner.getDeclaringClass().getResource("/" + r).getFile());
                    String type = Files.probeContentType(f.toPath());
                    HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
                    responseBuilder.setStatusCode(200);
                    responseBuilder.addHeader("Content-Type", type);
                    responseBuilder.addHeader("Content-Disposition", type.contains("text") || type == "application/javascript" ? "inline" : "attachment" + "; filename=" + r);
                    responseBuilder.setEntity(new FileInputStream(f));
                    response = responseBuilder.build();
                } else {
                    response = new HttpResponseBuilder().setStatusCode(500).build();
                }
            } else {
                response = new HttpResponseBuilder().setStatusCode(404).build();
            }
            OutputStream outputStream = clientConnection.getOutputStream();
            outputStream.write(response.toString().getBytes());
            response.writeInputStream(outputStream);
            clientConnection.close();
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
