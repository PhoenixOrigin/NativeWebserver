package net.phoenix.http.container;

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

    private final String method;

    HttpOpCode() {
        this.method = this.name();
    }

    @Override
    public String toString() {
        return method;
    }
}
