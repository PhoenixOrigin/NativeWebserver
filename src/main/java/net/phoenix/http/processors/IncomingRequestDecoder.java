package net.phoenix.http.processors;

import net.phoenix.http.builder.HttpRequestBuilder;
import net.phoenix.http.container.HttpOpCode;
import net.phoenix.http.container.HttpRequest;

/**
 * A class that processes incoming HTTP requests. This class is used internally to process incoming HTTP requests. It is not intended for use by the end user.
 */
public class IncomingRequestDecoder {

    /**
     * Processes an incoming HTTP request. This method should not be called, edited, or otherwise used or modified by the end user in any situation.
     *
     * @param data The data to process
     * @param ip   The IP address the request has originated from
     * @return The request to process
     */
    public static HttpRequest processRequest(String data, String ip) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        String[] headerData = data.split("\r\n");
        String[] requestLine = headerData[0].split(" ");
        String path = requestLine[1];
        requestBuilder.setMethod(HttpOpCode.fromString(requestLine[0]));
        requestBuilder.setPath(path);
        for (int i = 1; i < headerData.length - 1; i++) {
            String[] header = headerData[i].split(": ");
            if (headerData[i].isEmpty()) {
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
        requestBuilder.setIp(ip);
        return requestBuilder.build();
    }

}
