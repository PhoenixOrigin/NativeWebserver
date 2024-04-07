package net.phoenix.http.builder;

import net.phoenix.http.container.HttpRequest;

import java.util.HashMap;

public class HttpRequestBuilder {
    private HashMap<String, String> headers;
    private String method;
    private String path;
    private String body;
    private HashMap<String, String> params;


    public HttpRequestBuilder() {
    }

    public HttpRequestBuilder setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestBuilder addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    public HttpRequestBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public HttpRequestBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    public HttpRequestBuilder setParams(HashMap<String, String> params) {
        this.params = params;
        return this;
    }

    public HttpRequestBuilder addParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public HttpRequest build() {
        return new HttpRequest(headers, method, path, body, params);
    }
}
