package net.phoenix.server.http.http1.processors;

import net.phoenix.server.Server;
import net.phoenix.server.http.container.HttpRequest;
import net.phoenix.server.http.container.HttpResponse;
import net.phoenix.server.http.reflection.Route;
import net.phoenix.server.http.reflection.Router;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static net.phoenix.server.http.reflection.Route.getError;
import static net.phoenix.server.http.reflection.Route.getFile;

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
    public static HttpResponse processRequest(@NotNull HttpRequest request) {
        try {
            if (request.path().contains("..")) {
                Server.logger.logError("Illegal path traversal containing \"../\" detected from " + request.ip());
                return getError(403).build();
            }
            Route r = Router.route(request.method().toString(), request.path().split("\\?")[0]);
            if (r == null) {
                try {
                    return getFile(request.path().split("\\?")[0]).build();
                } catch (IOException e) {
                    Server.logger.logError("Failed to process request due to: " + e.getMessage());
                    return getError(404).build();
                } catch (NullPointerException e) {
                    return getError(404).build();
                }
            }
            Server.logger.logConnection(request.ip(), request.path());

            return r.route(request);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Server.logger.logError("Failed to process request due to: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
