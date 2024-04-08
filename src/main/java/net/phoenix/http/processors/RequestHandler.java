package net.phoenix.http.processors;

import net.phoenix.Server;
import net.phoenix.http.builder.HttpResponseBuilder;
import net.phoenix.http.container.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class that handles incoming requests from the socket. This class is for internal use and should not be modified or used by the end user.
 */
public class RequestHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Invoked when a connection is established and ready to accept I/O operations.
     *
     * @param result     The result of the I/O operation.
     * @param attachment The object attached to the I/O operation when it was initiated.
     */
    @Override
    public void completed(AsynchronousSocketChannel result, Object attachment) {
        if (Server.socket.isOpen()) {
            Server.socket.accept(null, this);
        }
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(buffer, null, new CompletionHandler<>() {
            /**
             * Process the incoming data that has completed
             * @param r
             *          The result of the I/O operation.
             * @param attachment
             *          The object attached to the I/O operation when it was initiated.
             */
            @Override
            public void completed(Integer r, Object attachment) {
                buffer.flip();
                byte[] data = new byte[buffer.limit()];
                buffer.get(data);
                buffer.clear();
                String request = new String(data, StandardCharsets.UTF_8);
                HttpResponse response;
                try {
                    response = IncomingRequest.processRequest(IncomingRequestDecoder.processRequest(request, result.getRemoteAddress().toString()));
                } catch (IOException e) {
                    Server.logger.logError("Failed to process request due to: " + e.getMessage());
                    throw new RuntimeException(e);
                }
                try {
                    long dataLength = response.responseHeaders().get("Content-Length") != null ? Long.parseLong(response.responseHeaders().get("Content-Length").get(0)) : 0;
                    scheduleTimeout(result, Math.max((int) (dataLength / 51.2), 5000));
                    result.write(ByteBuffer.wrap(response.toString().getBytes(StandardCharsets.UTF_8))).get();
                    response.writeInputStream(result);
                } catch (IOException e) {
                    Server.logger.logError("Failed to write response to client due to: " + e.getMessage());
                    throw new RuntimeException(e);
                } catch (InterruptedException | ExecutionException ignored) {
                }

                try {
                    result.close();
                } catch (IOException e) {
                    Server.logger.logError("Failed to close connection due to: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            /**
             * Invoked when an I/O operation fails.
             * @param exc
             *          The exception that caused the I/O operation to fail.
             * @param attachment
             *          The object attached to the I/O operation when it was initiated.
             */
            @Override
            public void failed(Throwable exc, Object attachment) {
                Server.logger.logError("Failed to read request from client due to: " + exc.getMessage());
            }
        });
    }

    /**
     * Invoked when an I/O operation fails.
     *
     * @param exc        The exception that caused the I/O operation to fail.
     * @param attachment The object attached to the I/O operation when it was initiated.
     */
    @Override
    public void failed(Throwable exc, Object attachment) {
        Server.logger.logError("Failed to accept connection due to: " + exc.getMessage());
    }

    /**
     * Schedules a timeout for the socket channel.
     *
     * @param socketChannel The socket channel to schedule the timeout for
     * @param timeout       The timeout duration
     */
    private void scheduleTimeout(AsynchronousSocketChannel socketChannel, int timeout) {
        executor.schedule(() -> {
            try {
                HttpResponse timeoutResponse = new HttpResponseBuilder().setStatusCode(408).build();
                ByteBuffer responseBuffer = ByteBuffer.wrap(timeoutResponse.toString().getBytes(StandardCharsets.UTF_8));
                socketChannel.write(responseBuffer).get();
                Server.logger.logError("Response write operation timed out from client: " + socketChannel.getRemoteAddress().toString());
                socketChannel.close();
            } catch (IOException e) {
                Server.logger.logError("Failed to close connection due to: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (ExecutionException | InterruptedException e) {
                Server.logger.logError("Failed to write response to client due to: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, timeout, TimeUnit.MILLISECONDS);
    }
}