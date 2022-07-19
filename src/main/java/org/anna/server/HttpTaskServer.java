package org.anna.server;

import com.google.gson.*;
import com.sun.net.httpserver.*;
import org.anna.taskManager.manager.Managers;
import org.anna.taskManager.manager.taskManager.TaskManager;
import org.anna.taskManager.tasks.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();
    private final HttpServer httpServer;
    private final TaskManager manager;

    public TaskManager getManager() {
        return manager;
    }

    public HttpTaskServer() throws IOException, InterruptedException {
        manager = Managers.getDefault();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks/", new TasksHandler());
        httpServer.createContext("/tasks/history", new HistoryHandler());
        httpServer.createContext("/tasks/task", new TaskHandler());
        httpServer.createContext("/tasks/epic", new EpicHandler());
        httpServer.createContext("/tasks/subtask", new SubtasksHandler());
        httpServer.createContext("/tasks/subtask/epic", new EpicSubtasksHandler());

        httpServer.start();
    }

    class TasksHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String serialized = gson.toJson(manager.getPrioritizedTasks());
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(serialized.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write("Запрос с используемым методом невозможно обработать.".getBytes());
                }
            }
        }
    }

    class HistoryHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String serialized = gson.toJson(manager.getHistoryManager());
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(serialized.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write("Запрос с используемым методом невозможно обработать.".getBytes());
                }
            }
        }
    }

    class TaskHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            switch (exchange.getRequestMethod()) {
                case "GET":
                    String serialized;
                    if (query == null) {
                        serialized = gson.toJson(manager.getListOfTasks());
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(serialized.getBytes());
                        }
                    } else {
                        String[] params = query.split("=");
                        long taskId = Long.parseLong(params[1]);
                        if (manager.getListOfTasks().stream()
                                .anyMatch(Task -> Task.getId() == taskId)) {
                            serialized = gson.toJson(manager.getTaskById(taskId));
                            exchange.getResponseHeaders().add("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(serialized.getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(404, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача с запрашиваемым id не найдена.".getBytes());
                            }
                        }
                    }
                    break;
                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), CHARSET);
                    manager.createTask(gson.fromJson(body, Task.class));
                    exchange.sendResponseHeaders(201, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Задача была успешно добавлена!".getBytes());
                    }
                    break;

                case "PUT":
                    if (query != null) {
                        String[] params = query.split("&");
                        String taskId = params[0].split("=")[1];
                        String status = params[1].split("=")[1];
                        inputStream = exchange.getRequestBody();
                        body = new String(inputStream.readAllBytes(), CHARSET);
                        if (manager.getListOfTasks().stream()
                                .anyMatch(Task -> Task.getId() == Long.parseLong(taskId))
                                && Arrays.stream(Status.values()).anyMatch(st -> st.name().equals(status))) {
                            manager.updateTask(Long.parseLong(taskId), gson.fromJson(body, Task.class),
                                    Status.valueOf(status));
                            exchange.sendResponseHeaders(201, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача была успешно обновлена!".getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(400, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Измените параметры запроса.".getBytes());
                            }
                        }
                    } else {
                        exchange.sendResponseHeaders(400, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Укажите параметры запроса.".getBytes());
                        }
                    }
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.removeAllTasks();
                        exchange.sendResponseHeaders(201, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Задачи были успешно удалены!".getBytes());
                        }
                    } else {
                        String[] params = query.split("=");
                        long taskId = Long.parseLong(params[1]);
                        if (manager.getListOfTasks().stream()
                                .anyMatch(Task -> Task.getId() == taskId)) {
                            manager.removeTaskById(taskId);
                            exchange.sendResponseHeaders(201, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача была успешно удалена!".getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(404, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача с запрашиваемым id не найдена.".getBytes());
                            }
                        }
                    }
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Запрос с используемым методом невозможно обработать.".getBytes());
                    }
            }
        }
    }

    class EpicHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            switch (exchange.getRequestMethod()) {
                case "GET":
                    String serialized;
                    if (query == null) {
                        serialized = gson.toJson(manager.getListOfEpics());
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(serialized.getBytes());
                        }
                    } else {
                        String[] params = query.split("=");
                        long epicId = Long.parseLong(params[1]);
                        if (manager.getListOfEpics().stream()
                                .anyMatch(Epic -> Epic.getId() == epicId)) {
                            serialized = gson.toJson(manager.getEpicById(epicId));
                            exchange.getResponseHeaders().add("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(serialized.getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(404, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача с запрашиваемым id не найдена.".getBytes());
                            }
                        }
                    }
                    break;
                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), CHARSET);
                    manager.createEpic(gson.fromJson(body, Epic.class));
                    exchange.sendResponseHeaders(201, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Задача была успешно добавлена!".getBytes());
                    }
                    break;
                case "PUT":
                    if (query != null) {
                        String[] params = query.split("=");
                        long epicId = Long.parseLong(params[1]);
                        inputStream = exchange.getRequestBody();
                        body = new String(inputStream.readAllBytes(), CHARSET);
                        if (manager.getListOfEpics().stream()
                                .anyMatch(Epic -> Epic.getId() == epicId)) {
                            manager.updateEpic(epicId, gson.fromJson(body, Epic.class));
                            exchange.sendResponseHeaders(201, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача была успешно обновлена!".getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(400, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Измените параметры запроса.".getBytes());
                            }
                        }
                    } else {
                        exchange.sendResponseHeaders(400, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Укажите параметры запроса.".getBytes());
                        }
                    }
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.removeAllEpics();
                        exchange.sendResponseHeaders(201, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Задачи были успешно удалены!".getBytes());
                        }
                    } else {
                        String[] params = query.split("=");
                        long epicId = Long.parseLong(params[1]);
                        if (manager.getListOfEpics().stream()
                                .anyMatch(Epic -> Epic.getId() == epicId)) {
                            manager.removeEpicById(epicId);
                            exchange.sendResponseHeaders(201, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача была успешно удалена!".getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(404, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача с запрашиваемым id не найдена.".getBytes());
                            }
                        }
                    }
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Запрос с используемым методом невозможно обработать.".getBytes());
                    }
            }
        }
    }

    class SubtasksHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            switch (exchange.getRequestMethod()) {
                case "GET":
                    String serialized;
                    if (query == null) {
                        serialized = gson.toJson(manager.getListOfSubtasks());
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(serialized.getBytes());
                        }
                    } else {
                        String[] params = query.split("=");
                        long subtaskId = Long.parseLong(params[1]);
                        if (manager.getListOfSubtasks().stream()
                                .anyMatch(Subtask -> Subtask.getId() == subtaskId)) {
                            serialized = gson.toJson(manager.getSubtaskById(subtaskId));
                            exchange.getResponseHeaders().add("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(serialized.getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(404, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача с запрашиваемым id не найдена.".getBytes());
                            }
                        }
                    }
                    break;
                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), CHARSET);
                    if (query != null && manager.getListOfEpics().stream()
                            .anyMatch(Epic -> Epic.getId() == Integer.parseInt(query.split("=")[1]))) {
                        manager.createSubtask(gson.fromJson(body, Subtask.class),
                                Integer.parseInt(query.split("=")[1]));
                        exchange.sendResponseHeaders(201, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Задача была успешно добавлена!".getBytes());
                        }
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Измените параметры запроса.".getBytes());
                        }
                    }
                    break;
                case "PUT":
                    if (query != null) {
                        String[] params = query.split("&");
                        String subtaskId = params[0].split("=")[1];
                        String status = params[1].split("=")[1];
                        inputStream = exchange.getRequestBody();
                        body = new String(inputStream.readAllBytes(), CHARSET);
                        if (manager.getListOfSubtasks().stream()
                                .anyMatch(Subtask -> Subtask.getId() == Long.parseLong(subtaskId))
                                && Arrays.stream(Status.values()).anyMatch(st -> st.name().equals(status))) {
                            manager.updateSubtask(Long.parseLong(subtaskId), gson.fromJson(body, Subtask.class),
                                    Status.valueOf(status));
                            exchange.sendResponseHeaders(201, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача была успешно обновлена!".getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(400, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Измените параметры запроса.".getBytes());
                            }
                        }
                    } else {
                        exchange.sendResponseHeaders(400, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Укажите параметры запроса.".getBytes());
                        }
                    }
                    break;
                case "DELETE":
                    String queryId = exchange.getRequestURI().getQuery();
                    if (queryId == null) {
                        manager.removeAllSubtasks();
                        exchange.sendResponseHeaders(201, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Задачи были успешно удалены!".getBytes());
                        }
                    } else {
                        String[] params = queryId.split("=");
                        long subtaskId = Long.parseLong(params[1]);
                        if (manager.getListOfSubtasks().stream()
                                .anyMatch(Subtask -> Subtask.getId() == subtaskId)) {
                            manager.removeSubtaskById(subtaskId);
                            exchange.sendResponseHeaders(201, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача была успешно удалена!".getBytes());
                            }
                        } else {
                            exchange.sendResponseHeaders(404, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Задача с запрашиваемым id не найдена.".getBytes());
                            }
                        }
                    }
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Запрос с используемым методом невозможно обработать.".getBytes());
                    }
            }
        }
    }

    class EpicSubtasksHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && manager.getListOfEpics().stream()
                        .anyMatch(Epic -> Epic.getId() == Integer.parseInt(query.split("=")[1]))) {
                    String serialized =
                            gson.toJson(manager.getAllEpicSubtasks(Integer.parseInt(query.split("=")[1])));
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(serialized.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Задача с запрашиваемым id не найдена.".getBytes());
                    }
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write("Запрос с используемым методом невозможно обработать.".getBytes());
                }
            }
        }
    }

    public void stop() {
        httpServer.stop(0);
    }
}
