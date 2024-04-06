package net.phoenix.http.container;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HttpResponse {
    private final Map<String, List<String>> responseHeaders;
    private final int statusCode;

    private final Optional<Object> entity;

    private HttpResponse(final Map<String, List<String>> responseHeaders, final int statusCode, final Optional<Object> entity) {
        this.responseHeaders = responseHeaders;
        this.statusCode = statusCode;
        this.entity = entity;
    }
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }
    public int getStatusCode() {
        return statusCode;
    }

    public Optional<Object> getEntity() {
        return entity;
    }

    private static List<String> buildHeaderStrings(final Map<String, List<String>> responseHeaders) {
        final List<String> responseHeadersList = new ArrayList<>();

        responseHeaders.forEach((name, values) -> {
            final StringBuilder valuesCombined = new StringBuilder();
            values.forEach(valuesCombined::append);
            valuesCombined.append(";");

            responseHeadersList.add(name + ": " + valuesCombined + "\r\n");
        });

        return responseHeadersList;
    }

    private static Optional<String> getResponseString(final Object entity) {
        if (entity instanceof String) {
            try {
                return Optional.of(entity.toString());
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();


        final int statusCode = getStatusCode();
        final String statusCodeMeaning = HttpStatusCode.STATUS_CODES.get(statusCode);

        final List<String> responseHeaders = buildHeaderStrings(getResponseHeaders());

        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusCodeMeaning).append("\r\n");

        for (String header : responseHeaders) {
            sb.append(header);
        }

        final Optional<String> entityString = getEntity().flatMap(HttpResponse::getResponseString);
        if (entityString.isPresent()) {
            final String encodedString = new String(entityString.get().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            sb.append("Content-Length: ").append(encodedString.getBytes().length).append("\r\n");
            sb.append("\r\n");
            sb.append(encodedString);
        } else {
            sb.append("\r\n");
        }

        return sb.toString();
    }

    public static class HttpStatusCode {

        public static final Map<Integer, String> STATUS_CODES = Map.of(
                200, "OK",
                400, "BAD_REQUEST",
                404, "NOT_FOUND",
                500, "INTERNAL_SERVER_ERROR"
        );
    }

    public static class Builder {
        private final Map<String, List<String>> responseHeaders;
        private int statusCode;

        private Optional<Object> entity;

        public Builder() {
            responseHeaders = new HashMap<>();
            responseHeaders.put("Server", List.of("localhost"));
            responseHeaders.put("Date", List.of(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))));

            entity = Optional.empty();
        }

        public Builder setStatusCode(final int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder addHeader(final String name, final String value) {
            responseHeaders.put(name, List.of(value));
            return this;
        }

        public Builder setEntity(final Object entity) {
            if (entity != null) {
                this.entity = Optional.of(entity);
            }
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(responseHeaders, statusCode, entity);
        }
    }
}
