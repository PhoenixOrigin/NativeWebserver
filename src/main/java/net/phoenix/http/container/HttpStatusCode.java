package net.phoenix.http.container;

import java.util.Map;

public class HttpStatusCode {

    public static final Map<Integer, String> STATUS_CODES = Map.of(
            200, "OK",
            400, "BAD_REQUEST",
            404, "NOT_FOUND",
            500, "INTERNAL_SERVER_ERROR"
    );
}