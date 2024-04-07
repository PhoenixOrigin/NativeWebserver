package net.phoenix.http.processors;

import net.phoenix.http.builder.HttpRequestBuilder;
import net.phoenix.http.container.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class IncomingRequestDecoder {

    public static HttpRequest processRequest(InputStream inputStream) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        String headerTempData = contentToString(inputStream);
        String[] headerData = headerTempData.split("\r\n");
        String[] requestLine = headerData[0].split(" ");
        String path = requestLine[1];
        requestBuilder.setMethod(requestLine[0]);
        requestBuilder.setPath(path);
        for (int i = 1; i < headerData.length - 1; i++) {
            String[] header = headerData[i].split(": ");
            if(headerData[i].isEmpty()) {
                try {
                    requestBuilder.setBody(headerData[headerData.length - 1]);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
                break;
            } else {
                requestBuilder.addHeader(header[0], header[1]);
            }
        }
        if (path.contains("?")) {
            String[] pathParts = path.split("\\?");
            requestBuilder.setPath(pathParts[0]);
            String[] queryParams = pathParts[1].split("&");
            for (String queryParam : queryParams) {
                String[] queryParamParts = queryParam.split("=");
                requestBuilder.addParam(queryParamParts[0], queryParamParts[1]);
            }
        }
        return requestBuilder.build();
    }

    private static String contentToString(InputStream inputStream) {
        StringBuilder headerTempData = new StringBuilder();
        Reader reader = new InputStreamReader(inputStream);
        try {
            int c;
            while ((c = reader.read()) != -1) {
                headerTempData.append((char) c);
                if(headerTempData.toString().contains("\r\n\r\n")) {
                    String[] headers = headerTempData.toString().split("\r\n");
                    int contentLength = -1;
                    for (String header : headers) {
                        if (header.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(header.split(":")[1].trim());
                            break;
                        }
                    }
                    if (contentLength > 0) {
                        char[] bodyBuffer = new char[contentLength];
                        int bytesRead = reader.read(bodyBuffer, 0, contentLength);
                        if (bytesRead != -1) {
                            headerTempData.append(bodyBuffer, 0, bytesRead);
                        }
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return headerTempData.toString();
    }


}
