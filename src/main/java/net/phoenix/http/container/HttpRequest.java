package net.phoenix.http.container;

import java.util.HashMap;

@SuppressWarnings("unused")
public record HttpRequest (HashMap<String, String> headers, String method, String path, String body, HashMap<String, String> params, String ip) {

    @Override
    public HashMap<String, String> headers() {
        return headers;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public HashMap<String, String> params() {
        return params;
    }

    @Override
    public String ip() {
        return ip;
    }
}
