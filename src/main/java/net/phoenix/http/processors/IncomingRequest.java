package net.phoenix.http.processors;

import net.phoenix.Server;
import net.phoenix.http.builder.HttpResponseBuilder;
import net.phoenix.http.container.HttpRequest;
import net.phoenix.http.container.HttpResponse;
import net.phoenix.http.reflection.Router;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Objects;

/**
 * A class that processes incoming HTTP requests.
 */
public class IncomingRequest {

    /**
     * Processes an incoming HTTP request. This method should not be called, edited, or otherwise used or modified by the end user in any situation.
     *
     * @param request The request to process
     * @return The response to send back to the client
     */
    public static HttpResponse processRequest(HttpRequest request) {
        try {
            if (request.path().contains("..")) {
                Server.logger.logError("Illegal path traversal containing \"../\" detected from " + request.ip());
                return getError(403).build();
            }
            Method runner = Router.route(request.method().toString(), request.path().split("\\?")[0]);
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
                        response = getFile((String) r).build();
                    } catch (NullPointerException e) {
                        response = getError(404).build();
                    }
                } else {
                    response = new HttpResponseBuilder().setStatusCode(500).build();
                }
            } else {
                try {
                    response = getFile(request.path().split("\\?")[0]).build();
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

    /**
     * Gets an error response with the specified status code. This method checks if the error page exists in the resources folder, and if it does not, it will return an error code with no entity.
     *
     * @param code The status code to get the error response for
     * @return The error response
     */
    private static HttpResponseBuilder getError(int code) {
        HttpResponseBuilder responseBuilder;
        try {
            return getFile(code + ".html");
        } catch (NullPointerException | IOException e2) {
            responseBuilder = new HttpResponseBuilder().setStatusCode(code);
        }
        return responseBuilder;
    }

    /**
     * Gets a file from the resources' folder.
     *
     * @param path The path to the file
     * @return The response containing the file
     * @throws IOException          If an I/O error occurs
     * @throws NullPointerException If the file does not exist
     */
    @SuppressWarnings("StringEquality")
    private static HttpResponseBuilder getFile(String path) throws IOException, NullPointerException {
        File f = new File(Objects.requireNonNull(Server.clazz.getResource("/static/" + path)).getFile());
        String type = Files.probeContentType(f.toPath());
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
        responseBuilder.setStatusCode(200);
        responseBuilder.addHeader("Content-Type", type);
        responseBuilder.addHeader("Content-Disposition", type.contains("text") || type == "application/javascript" ? "inline" : "attachment" + "; filename=" + path);
        responseBuilder.setEntity(f);
        return responseBuilder;
    }
}
