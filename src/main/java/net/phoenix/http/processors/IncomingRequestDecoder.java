package net.phoenix.http.processors;

import net.phoenix.http.builder.HttpRequestBuilder;
import net.phoenix.http.container.HttpRequest;

public class IncomingRequestDecoder {

    public static HttpRequest processRequest(String data, String ip) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        String[] headerData = data.split("\r\n");
        String[] requestLine = headerData[0].split(" ");
        String path = requestLine[1];
        requestBuilder.setMethod(requestLine[0]);
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
