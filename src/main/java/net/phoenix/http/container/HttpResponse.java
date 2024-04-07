package net.phoenix.http.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HttpResponse {
    private final Map<String, List<String>> responseHeaders;
    private final int statusCode;

    private final Optional<Object> entity;

    public HttpResponse(final Map<String, List<String>> responseHeaders, final int statusCode, final Optional<Object> entity) {
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

    public void writeInputStream(final OutputStream outputStream) throws IOException {
        if (entity.isPresent() && entity.get() instanceof InputStream entityStream) {
            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = entityStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            entityStream.close();
        }
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
}
