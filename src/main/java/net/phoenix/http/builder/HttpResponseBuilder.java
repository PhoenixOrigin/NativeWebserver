package net.phoenix.http.builder;

import net.phoenix.http.container.HttpResponse;

import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A builder for HttpResponse objects. This class is used to create responses for HTTP requests. This can be used by the end user to create custom responses.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "UnusedReturnValue"})
public class HttpResponseBuilder {
    private Map<String, List<String>> responseHeaders;
    private int statusCode;
    private Optional<Object> entity;

    /**
     * Creates a new HttpResponseBuilder.
     */
    public HttpResponseBuilder() {
        responseHeaders = new HashMap<>();
        responseHeaders.put("Server", List.of("localhost"));
        responseHeaders.put("Date", List.of(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))));

        entity = Optional.empty();
    }

    /**
     * Sets the status code of the HttpResponse.
     * @param statusCode The status code that should be returned to the client
     * @return This object for chaining.
     */
    public HttpResponseBuilder setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Sets the headers of the HttpResponse. If the headers already contain a Date header, it will be overridden with the current date. If the headers do not contain a Server header, it will be added with the value "localhost".
     * @param headers The headers to override the current headers with
     * @return This object for chaining.
     */
    public HttpResponseBuilder setHeaders(final Map<String, List<String>> headers) {
        responseHeaders = headers;
        responseHeaders.put("Date", List.of(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))));
        if (!responseHeaders.containsKey("Server"))
            responseHeaders.put("Server", List.of("localhost"));
        return this;
    }

    /**
     * Adds a header to the HttpResponse.
     * @param name The name of the header
     * @param value The value of the header
     * @return This object for chaining.
     */
    public HttpResponseBuilder addHeader(final String name, final String value) {
        responseHeaders.put(name, List.of(value));
        return this;
    }

    /**
     * Sets the entity of the HttpResponse. This entity will be served as a string to the client.
     * @param entity The entity to return to the client
     * @return This object for chaining.
     */
    public HttpResponseBuilder setEntity(final String entity) {
        if (entity != null) {
            this.entity = Optional.of(entity);
        }
        return this;
    }

    /**
     * Sets the entity of the HttpResponse. This entity will be fed to the client in buffers of 2048 bytes.
     * @param entity The entity to return to the client
     * @return This object for chaining.
     */
    public HttpResponseBuilder setEntity(final InputStream entity) {
        if (entity != null) {
            this.entity = Optional.of(entity);
        }
        return this;
    }

    /**
     * Builds the HttpResponse object.
     * @return A new HttpResponse object
     */
    public HttpResponse build() {
        return new HttpResponse(responseHeaders, statusCode, entity);
    }

}
