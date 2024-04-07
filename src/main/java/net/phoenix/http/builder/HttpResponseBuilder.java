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

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "UnusedReturnValue"})
public class HttpResponseBuilder {

    private Map<String, List<String>> responseHeaders;
    private int statusCode;

    private Optional<Object> entity;

    public HttpResponseBuilder() {
        responseHeaders = new HashMap<>();
        responseHeaders.put("Server", List.of("localhost"));
        responseHeaders.put("Date", List.of(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))));

        entity = Optional.empty();
    }

    public HttpResponseBuilder setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public HttpResponseBuilder setHeaders(final Map<String, List<String>> headers) {
        responseHeaders = headers;
        responseHeaders.put("Date", List.of(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))));
        if (!responseHeaders.containsKey("Server"))
            responseHeaders.put("Server", List.of("localhost"));
        return this;
    }

    public HttpResponseBuilder addHeader(final String name, final String value) {
        responseHeaders.put(name, List.of(value));
        return this;
    }

    public HttpResponseBuilder setEntity(final Object entity) {
        if (entity != null) {
            this.entity = Optional.of(entity);
        }
        return this;
    }

    public HttpResponseBuilder setEntity(final InputStream entity) {
        if (entity != null) {
            this.entity = Optional.of(entity);
        }
        return this;
    }

    public HttpResponse build() {
        return new HttpResponse(responseHeaders, statusCode, entity);
    }

}
