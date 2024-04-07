package net.phoenix.http.processors;

import net.phoenix.Server;
import net.phoenix.http.builder.HttpResponseBuilder;
import net.phoenix.http.container.HttpRequest;
import net.phoenix.http.container.HttpResponse;
import net.phoenix.http.reflection.Router;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Objects;

public class IncomingRequest {

    public static HttpResponse processRequest(HttpRequest request) {
        try {
            if (request.path().contains("..")) {
                Server.logger.logError("Illegal path traversal containing \"../\" detected from " + request.ip());
                return getError(403).build();
            }
            Method runner = Router.route(request.method(), request.path().split("\\?")[0]);
            Server.logger.logConnection(request.ip(), request.path());
            HttpResponse response;
            if (runner != null) {
                Object r = runner.invoke(null, request);
                if (r instanceof HttpResponse) {
                    response = (HttpResponse) r;
                    if (response.statusCode() != 200) {
                        Server.logger.logDebug("Invalid status code " + response.statusCode() + " returned from " + request.path());
                        if (response.entity().isEmpty()) {
                            response = getError(response.statusCode()).setHeaders(response.responseHeaders()).build();
                        }
                    }
                } else if (r instanceof String) {
                    try {
                        response = getFile((String) r);
                    } catch (NullPointerException e) {
                        response = getError(404).build();
                    }
                } else {
                    response = new HttpResponseBuilder().setStatusCode(500).build();
                }
            } else {
                try {
                    response = getFile(request.path().split("\\?")[0]);
                } catch (NullPointerException e) {
                    response = getError(404).build();
                }
            }
            return response;
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            Server.logger.logError("Failed to process request due to: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("StringEquality")
    private static HttpResponseBuilder getError(int code) {
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
        try {
            File f = new File(Objects.requireNonNull(Server.clazz.getResource("/static/" + code + ".html")).getFile());
            String type = Files.probeContentType(f.toPath());
            responseBuilder.setStatusCode(code);
            responseBuilder.addHeader("Content-Type", type);
            responseBuilder.addHeader("Content-Disposition", type.contains("text") || type == "application/javascript" ? "inline" : "attachment" + "; filename=" + code + ".html");
            responseBuilder.setEntity(new FileInputStream(f));
        } catch (NullPointerException | IOException e2) {
            responseBuilder = new HttpResponseBuilder().setStatusCode(code);
        }
        return responseBuilder;
    }

    @SuppressWarnings("StringEquality")
    private static HttpResponse getFile(String path) throws IOException, NullPointerException {
        HttpResponse response;
        File f = new File(Objects.requireNonNull(Server.clazz.getResource("/static/" + path)).getFile());
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
