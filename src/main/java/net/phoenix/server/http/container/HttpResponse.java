package net.phoenix.server.http.container;

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

/**
 * Represents an HTTP response.
 */
public record HttpResponse(Map<String, List<String>> responseHeaders, int statusCode, Optional<Object> entity,
                           Optional<Long> inputStreamLength) {

    /**
     * Internally builds the header strings for the response.
     *
     * @param responseHeaders The headers to build
     * @return A list of strings representing the headers
     */
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

    /**
     * Gets the body of the response. If it is a string, it will be written directly with the headers. If it is an InputStream, it will be written separately.
     *
     * @param entity The entity to get the response string from
     * @return The body string
     */
    private static Optional<String> getResponseString(final Object entity) {
        if (entity instanceof String) {
            try {
                return Optional.of(entity.toString());
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }

    /**
     * Writes the InputStream to the channel. This will be done in buffers of 2048 bytes. The InputStream will be closed after writing.
     *
     * @param channel The socket connection to write the InputStream to
     * @throws IOException          If an I/O error occurs
     * @throws ExecutionException   If an exception occurs during execution
     * @throws InterruptedException If the current thread is interrupted
     */
    public void writeInputStream(final AsynchronousSocketChannel channel) throws IOException, ExecutionException, InterruptedException {
        if (entity.isPresent() && entity.get() instanceof InputStream entityStream) {
            final byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = entityStream.read(buffer)) != -1) {
                if(!channel.isOpen()) return;
                channel.write(ByteBuffer.wrap(buffer, 0, bytesRead)).get();
            }
            entityStream.close();
        }
    }

    /**
     * Gets the request as a string.
     *
     * @return The status code
     */
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
            sb.append("Content-Length: ").append(encodedString.getBytes().length + inputStreamLength.orElse(0L)).append("\r\n");
            sb.append("\r\n");
            sb.append(encodedString);
        } else {
            sb.append("\r\n");
        }

        return sb.toString();
    }
}
