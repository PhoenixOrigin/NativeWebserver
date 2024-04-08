package net.phoenix.server.http.reflection;

import net.phoenix.server.Server;
import net.phoenix.server.http.builder.HttpResponseBuilder;
import net.phoenix.server.http.container.HttpOpCode;
import net.phoenix.server.http.container.HttpRequest;
import net.phoenix.server.http.container.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.util.Objects;

/**
 * A class that represents a route.
 */
public class Route {

    private static Method method = null;
    private final String path;
    private static Type type = null;
    private static ProxyRoute proxyRoute = null;

    /**
        * Gets the method of the route.
        *
        * @return The method of the route
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the path of the route.
        *
        * @return The path of the route
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the type of the route.
        *
        * @return The type of the route
     */
    public Type getType() {
        return type;
    }

    /**
     * Creates a new route.
     *
     * @param method The method of the route
     * @param path   The path of the route
     * @param type   The type of the route
     */
    public Route(Method method, String path, Type type, ProxyRoute proxyRoute) {
        Route.method = method;
        this.path = path;
        Route.type = type;
        Route.proxyRoute = proxyRoute;
    }

    public Route(Method method, String path, Type type) {
        Route.method = method;
        this.path = path;
        Route.type = type;
    }

    /**
     * Routes the request to the appropriate method.
     *
     * @param request The request to route
     * @return The response to send back to the client
     * @throws InvocationTargetException If the target method throws an exception
     * @throws IllegalAccessException    If the target method is inaccessible
     */
    public HttpResponse route(HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        if (getType() == Type.PROXY) {
            return proxyRoute(request);
        } else {
            return standardRoute(request);
        }
    }

    /**
     * Proxies the request to the target.
     * @param request The request to proxy
     * @return The response from the target
     */
    private static HttpResponse proxyRoute(HttpRequest request) {
        HttpClient client = HttpClient.newHttpClient();
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder();
        try {
            builder.uri(new URI(proxyRoute.target() + request.path()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        builder.method(proxyRoute.opCode().toString(), java.net.http.HttpRequest.BodyPublishers.ofString(request.body()));
        request.headers().forEach(builder::header);
        java.net.http.HttpRequest req = builder.build();
        try {
            return client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString()).body().isEmpty() ? getError(404).build() : new HttpResponseBuilder().setEntity(client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString()).body()).setStatusCode(200).build();
        } catch (IOException | InterruptedException e) {
            return getError(404).build();
        }
    }

    /**
     * Routes the request to the standard method.
     *
     * @param request The request to route
     * @return The response to send back to the client
     * @throws InvocationTargetException If the target method throws an exception
     * @throws IllegalAccessException    If the target method is inaccessible
     */
    private static HttpResponse standardRoute(HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        HttpResponse response;

        Object r = method.invoke(null, request);
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
            } catch (NullPointerException | IOException e) {
                response = getError(404).build();
            }
        } else {
            response = new HttpResponseBuilder().setStatusCode(500).build();
        }

        return response;
    }

    /**
     * Gets an error response with the specified status code. This method checks if the error page exists in the resources folder, and if it does not, it will return an error code with no entity.
     *
     * @param code The status code to get the error response for
     * @return The error response
     */
    public static HttpResponseBuilder getError(int code) {
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
    public static HttpResponseBuilder getFile(String path) throws IOException, NullPointerException {
        File f = new File(Objects.requireNonNull(Server.clazz.getResource("/static/" + path)).getFile());
        String type = Files.probeContentType(f.toPath());
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
        responseBuilder.setStatusCode(200);
        responseBuilder.addHeader("Content-Type", type);
        responseBuilder.addHeader("Content-Disposition", type.contains("text") || type == "application/javascript" ? "inline" : "attachment" + "; filename=" + path);
        responseBuilder.setEntity(f);
        return responseBuilder;
    }

    enum Type {
        PROXY,
        STANDARD
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ProxyRoute {
        String path() default "";
        String target();
        HttpOpCode opCode() default HttpOpCode.GET;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface StandardRoute {
        /**
         * The path of the route. This will be concatenated with the path of the class. <br> <br>
         * Examples: <br>
         * <br>
         * <code>If the class path is "/", and the method path is "/test", the full path will be "/test".</code>
         * <br> <br>
         * <code>If the class path is "/test", and the method path is "/test2", the full path will be "/test/test2".</code>
         * <br> <br>
         * Warning: <code>If the class path is "/", and the method path is "/test", the full path will be "//test", not "/test".</code>
         *
         * @return The path of the route
         */
        String path() default "";

        /**
         * The HTTP method of the route.
         *
         * @return The HTTP method of the route
         */
        HttpOpCode opCode() default HttpOpCode.GET;
    }


}