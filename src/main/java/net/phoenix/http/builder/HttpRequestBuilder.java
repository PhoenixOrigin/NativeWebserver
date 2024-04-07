package net.phoenix.http.builder;

import net.phoenix.http.container.HttpOpCode;
import net.phoenix.http.container.HttpRequest;

import java.util.HashMap;

/**
 * A builder for HttpRequest objects. This class is used internally to process and serialize HTTP requests. It is not intended for use by the end user.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class HttpRequestBuilder {
    private HashMap<String, String> headers;
    private HttpOpCode method;
    private String path;
    private String body;
    private HashMap<String, String> params;
    private String ip;

    /**
     * Creates a new HttpRequestBuilder.
     */
    public HttpRequestBuilder() {
    }

    /**
     * Sets the headers of the HttpRequest.
     * @param headers The headers to change override the current headers with
     * @return This object for chaining.
     */
    public HttpRequestBuilder setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Adds a header to the HttpRequest.
     * @param key The name of the header
     * @param value The value of the header
     * @return This object for chaining.
     */
    public HttpRequestBuilder addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    /**
     * Sets the method of the HttpRequest.
     * @param method The HTTP OP Code to set
     * @return This object for chaining.
     */
    public HttpRequestBuilder setMethod(HttpOpCode method) {
        this.method = method;
        return this;
    }

    /**
     * Sets the path of the HttpRequest.
     * @param path The path queried
     * @return This object for chaining.
     */
    public HttpRequestBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the body of the HttpRequest.
     * @param body The body of the request
     * @return This object for chaining.
     */
    public HttpRequestBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the url parameters of the HttpRequest.
     * @param params The parameters to set
     * @return This object for chaining.
     */
    public HttpRequestBuilder setParams(HashMap<String, String> params) {
        this.params = params;
        return this;
    }

    /**
     * Adds a url parameter to the HttpRequest.
     * @param key The name of the parameter
     * @param value The value of the parameter
     * @return This object for chaining.
     */
    public HttpRequestBuilder addParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    /**
     * Sets the IP of the HttpRequest.
     * @param ip The IP address the request originated from
     * @return This object for chaining.
     */
    public HttpRequestBuilder setIp(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * Builds the HttpRequest object.
     * @return The HttpRequest object
     */
    public HttpRequest build() {
        return new HttpRequest(headers, method, path, body, params, ip);
    }
}
