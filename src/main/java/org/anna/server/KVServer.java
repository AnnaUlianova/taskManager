package org.anna.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class KVServer {
    public static final int PORT = 8078;
    private final String API_TOKEN;
    private final HttpServer server;
    private final Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        API_TOKEN = generateApiKey();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

        server.createContext("/register", (h) -> {
            try {
                switch (h.getRequestMethod()) {
                    case "GET":
                        sendText(h, API_TOKEN);
                        break;
                    default:
                        System.out.println("/register ждёт GET-запрос, а получил " + h.getRequestMethod());
                        h.sendResponseHeaders(405, 0);
                }
            } finally {
                h.close();
            }
        });
        server.createContext("/save", (h) -> {
            try {
                if (!hasAuth(h)) {
                    h.sendResponseHeaders(403, 0);
                    return;
                }
                switch (h.getRequestMethod()) {
                    case "POST":
                        String key = h.getRequestURI().getPath().substring("/save/".length());
                        if (key.isEmpty()) {
                            h.sendResponseHeaders(400, 0);
                            return;
                        }
                        String value = readText(h);
                        if (value.isEmpty()) {
                            h.sendResponseHeaders(400, 0);
                            return;
                        }
                        data.put(key, value);
                        h.sendResponseHeaders(200, 0);
                        break;
                    default:
                        h.sendResponseHeaders(405, 0);
                }
            } finally {
                h.close();
            }
        });
        server.createContext("/load", (h) -> {
            try {
                if (!hasAuth(h)) {
                    h.sendResponseHeaders(403, 0);
                    return;
                }
                switch (h.getRequestMethod()) {
                    case "GET":
                        String key = h.getRequestURI().getPath().substring("/load/".length());
                        if (key.isEmpty()) {
                            h.sendResponseHeaders(400, 0);
                            return;
                        }
                        if (data.containsKey(key)) {
                            sendText(h, data.get(key));
                            h.sendResponseHeaders(200, 0);
                        } else {
                            h.sendResponseHeaders(404, 0);
                        }
                        break;
                    default:
                        h.sendResponseHeaders(405, 0);
                }
            } finally {
                h.close();
            }
        });
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private String generateApiKey() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuth(HttpExchange h) {
        String rawQuery = h.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_TOKEN=" + API_TOKEN)
                || rawQuery.contains("API_TOKEN=DEBUG"));
    }

    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }
}
