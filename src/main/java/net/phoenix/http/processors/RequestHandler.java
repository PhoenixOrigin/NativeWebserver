package net.phoenix.http.processors;

import net.phoenix.Server;
import net.phoenix.http.container.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class RequestHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    @Override
    public void completed(AsynchronousSocketChannel result, Object attachment) {
        if (Server.socket.isOpen()) {
            Server.socket.accept(null, this);
        }
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(buffer, null, new CompletionHandler<>() {
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
                    result.write(ByteBuffer.wrap(response.toString().getBytes(StandardCharsets.UTF_8))).get();
                    response.writeInputStream(result);
                } catch (IOException | ExecutionException | InterruptedException e) {
                    Server.logger.logError("Failed to write response to client due to: " + e.getMessage());
                    throw new RuntimeException(e);
                }

                try {
                    result.close();
                } catch (IOException e) {
                    Server.logger.logError("Failed to close connection due to: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                Server.logger.logError("Failed to read request from client due to: " + exc.getMessage());
            }
        });
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        Server.logger.logError("Failed to accept connection due to: " + exc.getMessage());
    }
}