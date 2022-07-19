package org.anna;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.anna.server.HttpTaskServer;
import org.anna.server.KVServer;
import org.anna.taskManager.tasks.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest {

    private HttpTaskServer server;
    private KVServer kvServer;
    private static final Gson gson = new Gson();
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void init() throws IOException, InterruptedException {
        kvServer = new KVServer();
        kvServer.start();
        server = new HttpTaskServer();
    }

    @AfterEach
    public void destroy() {
        server.stop();
        kvServer.stop();
    }

    @Test
    public void test1_shouldReturn200StatusAndEmptyBodyForGetMethodOnEmptyPrioritizedTasks()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertTrue(JsonParser.parseString(response.body()).getAsJsonArray().isEmpty());
    }

    @Test
    public void test2_shouldReturn200StatusForGetMethodOnPrioritizedTasks()
            throws IOException, InterruptedException {
        // Given
        server.getManager().createTask(new Task("Прогулка с собакой",
                "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0)));
        // When
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(1, JsonParser.parseString(response.body()).getAsJsonArray().size());
    }

    @Test
    public void test3_shouldReturn200StatusAndEmptyBodyForGetMethodOnEmptyHistory()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertTrue(JsonParser.parseString(response.body()).getAsJsonArray().isEmpty());
    }

    @Test
    public void test4_shouldReturn200StatusForGetMethodOnHistory() throws IOException, InterruptedException {
        // Given
        server.getManager().createTask(new Task("Прогулка с собакой",
                "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0)));
        server.getManager().getTaskById(0);
        // When
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(1, JsonParser.parseString(response.body()).getAsJsonArray().size());
    }

    // Task handler tests
    @Test
    public void test5_shouldReturn200StatusAndEmptyBodyForGetMethodOnEmptyTasks()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertTrue(JsonParser.parseString(response.body()).getAsJsonArray().isEmpty());
    }

    @Test
    public void test6_shouldReturn200StatusForGetMethodOnTasks()
            throws IOException, InterruptedException {
        // Given
        server.getManager().createTask(new Task("Прогулка с собакой",
                "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0)));
        server.getManager().createTask(new Task("Прогулка",
                "В парке"));
        // When
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(2, JsonParser.parseString(response.body()).getAsJsonArray().size());
    }

    @Test
    public void test7_shouldReturn404StatusForGetMethodOnTaskWithWrongId() throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/task/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(404, response.statusCode());
        assertEquals("Задача с запрашиваемым id не найдена.", response.body());
    }

    @Test
    public void test8_shouldReturn200StatusAndJsonTaskForGetMethodOnTaskWithId()
            throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        server.getManager().createTask(task);
        // When
        URI url = URI.create("http://localhost:8080/tasks/task/?id=0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long id = JsonParser.parseString(response.body()).getAsJsonObject().get("value").getAsJsonObject()
                .get("id").getAsLong();
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(id, task.getId());
    }

    @Test
    public void test9_shouldReturn201StatusForPostMethodAndAddTask() throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        // When
        URI url = URI.create("http://localhost:8080/tasks/task");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(task));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertEquals("Прогулка с собакой", server.getManager().getListOfTasks().get(0).getTitle());
        assertEquals("Задача была успешно добавлена!", response.body());
    }

    @Test
    public void test10_shouldReturn400StatusForPutMethodWithNoParamsOnTask()
            throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        // When
        URI url = URI.create("http://localhost:8080/tasks/task");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(task));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(400, response.statusCode());
        assertEquals("Укажите параметры запроса.", response.body());
    }

    @Test
    public void test11_shouldReturn400StatusForPutMethodWithWrongParamsOnTask()
            throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        // When
        URI url = URI.create("http://localhost:8080/tasks/task/?id=10&status=DONE");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(task));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(400, response.statusCode());
        assertEquals("Измените параметры запроса.", response.body());
    }

    @Test
    public void test12_shouldReturn201StatusForPutMethodAndUpdateTask()
            throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        server.getManager().createTask(task);
        Task updTask = new Task("Прогулка с собакой", "В парке", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        // When
        URI url = URI.create("http://localhost:8080/tasks/task/?id=0&status=NEW");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(updTask));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertFalse(server.getManager().getListOfTasks().contains(task));
        assertEquals("В парке", server.getManager().getListOfTasks().get(0).getDescription());
        assertEquals("Задача была успешно обновлена!", response.body());
    }

    @Test
    public void test13_shouldReturn201StatusForDeleteMethodAndDeleteAllTasks()
            throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        server.getManager().createTask(task);
        // When
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertTrue(server.getManager().getListOfTasks().isEmpty());
        assertEquals("Задачи были успешно удалены!", response.body());
    }

    @Test
    public void test14_shouldReturn201StatusForDeleteMethodAndDeleteTaskById()
            throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        server.getManager().createTask(task);
        server.getManager().getTaskById(0);
        // When
        URI url = URI.create("http://localhost:8080/tasks/task/?id=0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertTrue(server.getManager().getListOfTasks().isEmpty());
        assertEquals("Задача была успешно удалена!", response.body());
    }

    @Test
    public void test15_shouldReturn404StatusForDeleteMethodWithWrongTaskId()
            throws IOException, InterruptedException {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        server.getManager().createTask(task);
        // When
        URI url = URI.create("http://localhost:8080/tasks/task/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(404, response.statusCode());
        assertEquals("Задача с запрашиваемым id не найдена.", response.body());
    }

    // Epic handler tests
    @Test
    public void test16_shouldReturn200StatusAndEmptyBodyForGetMethodOnEmptyEpics()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertTrue(JsonParser.parseString(response.body()).getAsJsonArray().isEmpty());
    }

    @Test
    public void test17_shouldReturn200StatusForGetMethodOnEpics()
            throws IOException, InterruptedException {
        // Given
        server.getManager().createEpic(new Epic("Отпуск", "Поездка в горы в декабре"));
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(1, JsonParser.parseString(response.body()).getAsJsonArray().size());
    }

    @Test
    public void test18_shouldReturn404StatusForGetMethodOnEpicWithWrongId() throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(404, response.statusCode());
        assertEquals("Задача с запрашиваемым id не найдена.", response.body());
    }

    @Test
    public void test19_shouldReturn200StatusAndJsonEpicForGetMethodOnEpicWithId()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long id = JsonParser.parseString(response.body()).getAsJsonObject().get("value").getAsJsonObject()
                .get("id").getAsLong();
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(id, epic.getId());
    }

    @Test
    public void test20_shouldReturn201StatusForPostMethodAndAddEpic() throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(epic));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertEquals("Отпуск", server.getManager().getListOfEpics().get(0).getTitle());
        assertEquals("Задача была успешно добавлена!", response.body());
    }

    @Test
    public void test21_shouldReturn400StatusForPutMethodWithNoParamsOnEpic()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(epic));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(400, response.statusCode());
        assertEquals("Укажите параметры запроса.", response.body());
    }

    @Test
    public void tes22_shouldReturn400StatusForPutMethodWithWrongParamsOnEpic()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=10");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(epic));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(400, response.statusCode());
        assertEquals("Измените параметры запроса.", response.body());
    }

    @Test
    public void test23_shouldReturn201StatusForPutMethodAndUpdateEpic()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Epic updEpic = new Epic("Отпуск", "Поездка на море");
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=0");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(updEpic));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertFalse(server.getManager().getListOfEpics().contains(epic));
        assertEquals("Поездка на море", server.getManager().getListOfEpics().get(0).getDescription());
        assertEquals("Задача была успешно обновлена!", response.body());
    }

    @Test
    public void test24_shouldReturn201StatusForDeleteMethodAndDeleteAllEpics()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertTrue(server.getManager().getListOfEpics().isEmpty());
        assertEquals("Задачи были успешно удалены!", response.body());
    }

    @Test
    public void test25_shouldReturn201StatusForDeleteMethodAndDeleteEpicById()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        server.getManager().getEpicById(0);
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertTrue(server.getManager().getListOfEpics().isEmpty());
        assertEquals("Задача была успешно удалена!", response.body());
    }

    @Test
    public void test26_shouldReturn404StatusForDeleteMethodWithWrongEpicId()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        // When
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(404, response.statusCode());
        assertEquals("Задача с запрашиваемым id не найдена.", response.body());
    }

    // Subtask handler tests
    @Test
    public void test27_shouldReturn200StatusAndEmptyBodyForGetMethodOnEmptySubtasks()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertTrue(JsonParser.parseString(response.body()).getAsJsonArray().isEmpty());
    }

    @Test
    public void test28_shouldReturn200StatusForGetMethodOnSubtasks()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        server.getManager().createSubtask(subtask, epic.getId());
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(1, JsonParser.parseString(response.body()).getAsJsonArray().size());
    }

    @Test
    public void test29_shouldReturn404StatusForGetMethodOnSubtaskWithWrongId()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(404, response.statusCode());
        assertEquals("Задача с запрашиваемым id не найдена.", response.body());
    }

    @Test
    public void test30_shouldReturn200StatusAndJsonSubtaskForGetMethodOnSubtaskWithId()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        server.getManager().createSubtask(subtask, epic.getId());
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long id = JsonParser.parseString(response.body()).getAsJsonObject().get("value").getAsJsonObject()
                .get("id").getAsLong();
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(id, subtask.getId());
    }

    @Test
    public void test31_shouldReturn201StatusForPostMethodAndAddSubtask() throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=0");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertEquals("Авиабилеты", server.getManager().getListOfSubtasks().get(0).getTitle());
        assertEquals("Задача была успешно добавлена!", response.body());
    }

    @Test
    public void test32_shouldReturn400StatusForPutMethodWithNoParamsOnSubtask()
            throws IOException, InterruptedException {
        // Given
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(400, response.statusCode());
        assertEquals("Укажите параметры запроса.", response.body());
    }

    @Test
    public void tes33_shouldReturn400StatusForPutMethodWithWrongParamsOnSubtask()
            throws IOException, InterruptedException {
        // Given
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=10&status=NEW");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(subtask));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(400, response.statusCode());
        assertEquals("Измените параметры запроса.", response.body());
    }

    @Test
    public void test34_shouldReturn201StatusForPutMethodAndUpdateSubtask()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        server.getManager().createSubtask(subtask, epic.getId());
        Subtask updSubtask = new Subtask("Авиабилеты", "Пересадка в Париже", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=1&status=NEW");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(updSubtask));
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertFalse(server.getManager().getListOfSubtasks().contains(subtask));
        assertEquals("Пересадка в Париже", server.getManager().getListOfSubtasks().get(0).getDescription());
        assertEquals("Задача была успешно обновлена!", response.body());
    }

    @Test
    public void test35_shouldReturn201StatusForDeleteMethodAndDeleteAllSubtasks()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        server.getManager().createSubtask(subtask, epic.getId());
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertTrue(server.getManager().getListOfSubtasks().isEmpty());
        assertEquals("Задачи были успешно удалены!", response.body());
    }

    @Test
    public void test36_shouldReturn201StatusForDeleteMethodAndDeleteSubtaskById()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        server.getManager().createSubtask(subtask, epic.getId());
        server.getManager().getEpicById(0);
        server.getManager().getSubtaskById(1);
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(201, response.statusCode());
        assertTrue(server.getManager().getListOfSubtasks().isEmpty());
        assertEquals("Задача была успешно удалена!", response.body());
    }

    @Test
    public void test37_shouldReturn404StatusForDeleteMethodWithWrongSubtaskId()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        server.getManager().createSubtask(subtask, epic.getId());
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(404, response.statusCode());
        assertEquals("Задача с запрашиваемым id не найдена.", response.body());
    }

    @Test
    public void test38_shouldReturn200StatusForGetMethodAndListOdEpicsSubtasks()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        Subtask subtask1 = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        server.getManager().createSubtask(subtask1, epic.getId());
        Subtask subtask2 = new Subtask("Билеты", "В музей");
        server.getManager().createSubtask(subtask2, epic.getId());
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertEquals(2, JsonParser.parseString(response.body()).getAsJsonArray().size());
    }

    @Test
    public void test39_shouldReturn200StatusForGetMethodAndEmptyList()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        server.getManager().createEpic(epic);
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(200, response.statusCode());
        assertTrue(JsonParser.parseString(response.body()).getAsJsonArray().isEmpty());
    }

    @Test
    public void test40_shouldReturn404StatusForGetMethodAndWrongEpicId()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(404, response.statusCode());
        assertEquals("Задача с запрашиваемым id не найдена.", response.body());
    }

    @Test
    public void test41_shouldReturn405StatusForWrongMethod()
            throws IOException, InterruptedException {
        // When
        URI url = URI.create("http://localhost:8080/tasks/");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(
                new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник")));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Then
        assertEquals(405, response.statusCode());
        assertEquals("Запрос с используемым методом невозможно обработать.", response.body());
    }
}