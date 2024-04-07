package net.phoenix.http.container;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public record HttpResponse(Map<String, List<String>> responseHeaders, int statusCode, Optional<Object> entity) {

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

    public void writeInputStream(final AsynchronousSocketChannel channel) throws IOException, ExecutionException, InterruptedException {
        if (entity.isPresent() && entity.get() instanceof InputStream entityStream) {
            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = entityStream.read(buffer)) != -1) {
                channel.write(ByteBuffer.wrap(buffer, 0, bytesRead)).get();
            }
            entityStream.close();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        final int statusCode = statusCode();
        final String statusCodeMeaning = HttpStatusCode.STATUS_CODES.get(statusCode);

        final List<String> responseHeaders = buildHeaderStrings(responseHeaders());

        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusCodeMeaning).append("\r\n");

        for (String header : responseHeaders) {
            sb.append(header);
        }

        final Optional<String> entityString = entity().flatMap(HttpResponse::getResponseString);
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
