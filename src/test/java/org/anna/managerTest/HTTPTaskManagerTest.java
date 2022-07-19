package org.anna.managerTest;

import org.anna.server.KVServer;
import org.anna.taskManager.manager.taskManager.HTTPTaskManager;
import org.anna.taskManager.tasks.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class HTTPTaskManagerTest extends InMemoryTaskManagerTest {

    private KVServer server;

    @BeforeEach
    public void init() {
        try {
            server = new KVServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();
        try {
            manager = new HTTPTaskManager("8078");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void destroy() {
        server.stop();
    }

    @DisplayName("GIVEN an empty HTTPManager " +
            "WHEN the condition is loaded from the server " +
            "THEN fields of the loaded HTTPManager are empty")
    @Test
    public void test1_shouldCreateAnEmptyHTTPManagerFromTheEmptyServer() throws IOException, InterruptedException {
        // When
        HTTPTaskManager loadedManager = HTTPTaskManager.loadFromServer("8078");
        // Then
        assertTrue(loadedManager.getHistoryManager().isEmpty(), "История не пуста.");
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty(), "История не пуста.");
        assertTrue(loadedManager.getListOfTasks().isEmpty(), "Коллекция не пуста.");
        assertTrue(loadedManager.getListOfEpics().isEmpty(), "Коллекция не пуста.");
        assertTrue(loadedManager.getListOfSubtasks().isEmpty(), "Коллекция не пуста.");
    }

    @DisplayName("GIVEN an HTTPManager with one Epic and no history " +
            "WHEN the condition is loaded from the server " +
            "THEN Epic is added to the manager's collection, " +
            "other fields are empty")
    @Test
    public void test2_shouldCreateAnHTTPManagerFromTheServerWithOneEpic() throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        // When
        HTTPTaskManager loadedManager = HTTPTaskManager.loadFromServer("8078");
        // Then
        assertTrue(loadedManager.getHistoryManager().isEmpty(), "История не пуста.");
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty(), "Эпик был добавлен.");
        assertTrue(loadedManager.getListOfTasks().isEmpty(), "Коллекция не пуста.");
        assertTrue(loadedManager.getListOfSubtasks().isEmpty(), "Коллекция не пуста.");
        assertFalse(loadedManager.getListOfEpics().isEmpty(), "Задача не была добавлена.");
        assertEquals(epic, loadedManager.getListOfEpics().get(0), "Задачи не совпадают.");
    }

    @DisplayName("GIVEN an HTTPManager with Epic, it's Subtask and the history " +
            "WHEN the condition is loaded from the server " +
            "THEN Epic and Subtask are added to the manager's collections, " +
            "history of tasks is added")
    @Test
    public void test3_shouldCreateAnHTTPManagerFromTheServerWithEpicSubtaskHistory()
            throws IOException, InterruptedException {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        manager.getSubtaskById(subtask.getId());
        manager.getEpicById(epic.getId());
        // When
        HTTPTaskManager loadedManager = HTTPTaskManager.loadFromServer("8078");
        // Then
        assertTrue(loadedManager.getListOfTasks().isEmpty(), "Коллекция не пуста.");

        assertFalse(loadedManager.getListOfEpics().isEmpty(), "Задача не была добавлена.");
        assertEquals(epic, loadedManager.getListOfEpics().get(0), "Задачи не совпадают.");
        assertFalse(loadedManager.getListOfSubtasks().isEmpty(), "Задача не была добавлена.");
        assertEquals(subtask, loadedManager.getListOfSubtasks().get(0), "Задачи не совпадают.");

        assertEquals(2, loadedManager.getHistoryManager().size(), "Неверное количество задач.");
        assertTrue(loadedManager.getHistoryManager().contains(subtask), "Задача на была добавлена в историю.");
        assertEquals(subtask, loadedManager.getHistoryManager().get(0), "Неверный порядок отображения задач.");
        assertTrue(loadedManager.getHistoryManager().contains(epic), "Задача не была добавлена в историю.");
        assertEquals(epic, loadedManager.getHistoryManager().get(1), "Неверный порядок отображения задач.");

        assertEquals(1, loadedManager.getPrioritizedTasks().size(), "Неверное количество задач.");
        assertTrue(loadedManager.getPrioritizedTasks().contains(subtask), "Задача не была добавлена в сет.");
        assertFalse(loadedManager.getPrioritizedTasks().contains(epic), "Эпик был добавлен в сет.");
    }
}
