package org.anna.taskManager.manager.taskManager;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.anna.server.KVTaskClient;
import org.anna.taskManager.tasks.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HTTPTaskManager extends FileBackedTasksManager {

    private final KVTaskClient client;
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public HTTPTaskManager(String serverPort) throws IOException, InterruptedException {
        super(serverPort);
        client = new KVTaskClient(serverPort);
    }

    public KVTaskClient getClient() {
        return client;
    }

    @Override
    void save() {
        client.put("tasks", gson.toJson(tasks));
        client.put("epics", gson.toJson(epics));
        client.put("subtasks", gson.toJson(subtasks));
        client.put("history", gson.toJson(getHistoryManager()));
        client.put("prioritizedTasks", gson.toJson(getPrioritizedTasks()));
    }

    public static HTTPTaskManager loadFromServer(String serverPort) throws IOException, InterruptedException {
        HTTPTaskManager manager = new HTTPTaskManager(serverPort);

        manager.tasks = gson.fromJson(manager.getClient().load("tasks"),
                    new TypeToken<HashMap<Long, Task>>() {}.getType());
        manager.epics = gson.fromJson(manager.getClient().load("epics"),
                    new TypeToken<HashMap<Long, Epic>>() {}.getType());
        manager.subtasks = gson.fromJson(manager.getClient().load("subtasks"),
                    new TypeToken<HashMap<Long, Subtask>>() {}.getType());

        JsonElement jsonHistory = JsonParser.parseString(manager.getClient().load("history"));
        if (jsonHistory.isJsonArray()) {
            JsonArray jsonArray = jsonHistory.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject object = element.getAsJsonObject();
                String type = object.get("type").getAsString();

                if (Type.TASK.name().equals(type)) {
                    manager.historyManager.add(gson.fromJson(object, Task.class));
                } else if (Type.EPIC.name().equals(type)) {
                    manager.historyManager.add(gson.fromJson(object, Epic.class));
                } else {
                    manager.historyManager.add(gson.fromJson(object, Subtask.class));
                }
            }
        }

        JsonElement jsonPrioritizedTasks = JsonParser.parseString(manager.getClient().load("prioritizedTasks"));
        if (jsonPrioritizedTasks.isJsonArray()) {
            JsonArray jsonArray = jsonPrioritizedTasks.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject object = element.getAsJsonObject();
                String type = object.get("type").getAsString();

                if (Type.TASK.name().equals(type)) {
                    manager.prioritizedTasks.add(gson.fromJson(object, Task.class));
                } else if (Type.EPIC.name().equals(type)) {
                    manager.prioritizedTasks.add(gson.fromJson(object, Epic.class));
                } else {
                    manager.prioritizedTasks.add(gson.fromJson(object, Subtask.class));
                }
            }
        }
        return manager;
    }

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(localDateTime.format(formatter));
            }
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            } else {
                return LocalDateTime.parse(jsonReader.nextString(), formatter);
            }
        }
    }
}
