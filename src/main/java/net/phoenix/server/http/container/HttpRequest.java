package net.phoenix.server.http.container;

import java.util.HashMap;

/**
 * Represents an HTTP request.
 */
@SuppressWarnings("unused")
public record HttpRequest(HashMap<String, String> headers, HttpOpCode method, String path, String body,
                          HashMap<String, String> params, String ip) {

    /**
     * Gets the headers of the request.
     *
     * @return The headers of the request
     */
    @Override
    public HashMap<String, String> headers() {
        return headers;
    }

    /**
     * Gets the method of the request.
     *
     * @return The HTTP method of the request
     */
    @Override
    public HttpOpCode method() {
        return method;
    }

    /**
     * Gets the path of the request.
     *
     * @return The path of the request
     */
    @Override
    public String path() {
        return path;
    }

    /**
     * Gets the body of the request.
     *
     * @return The body of the request
     */
    @Override
    public String body() {
        return body;
    }

    /**
     * Gets the parameters of the request.
     *
     * @return The parameters of the request
     */
    @Override
    public HashMap<String, String> params() {
        return params;
    }

    /**
     * Gets the IP address of the request.
     *
     * @return The IP address the request has originated from
     */
    @Override
    public String ip() {
        return ip;
    }
}
