package net.phoenix.http.container;

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

    @Override
    public String toString() {
        return this.name();
    }
}
