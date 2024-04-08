package net.phoenix.server.http.container;

/**
 * An enumeration of HTTP operation codes.
 */
@SuppressWarnings("unused")
public enum HttpOpCode {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT;

    /**
     * Converts a string to an HttpOpCode.
     *
     * @param method The string to attempt to convert into a OpCode
     * @return The HttpOpCode
     * @throws IllegalArgumentException If the string is not a valid (or just not contained in this library) HTTP method
     */
    public static HttpOpCode fromString(String method) {
        return switch (method) {
            case "GET" -> GET;
            case "POST" -> POST;
            case "PUT" -> PUT;
            case "DELETE" -> DELETE;
            case "PATCH" -> PATCH;
            case "HEAD" -> HEAD;
            case "OPTIONS" -> OPTIONS;
            case "TRACE" -> TRACE;
            case "CONNECT" -> CONNECT;
            default -> throw new IllegalArgumentException("Invalid HTTP method: " + method);
        };
    }

    @Override
    public String toString() {
        return this.name();
    }
}
