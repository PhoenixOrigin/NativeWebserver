package net.phoenix.http.container;

import java.util.HashMap;

public class HttpRequest {
    public HashMap<String, String> headers;
    public String method;
    public String path;
    public String body;
    public HashMap<String, String> params;

    public HttpRequest(HashMap<String, String> headers, String method, String path, String body, HashMap<String, String> params) {
        this.headers = headers;
        this.method = method;
        this.path = path;
        this.body = body;
        this.params = params;
    }
}
