package org.anna.managerTest;

import org.anna.server.KVServer;
import org.anna.taskManager.manager.Managers;
import org.anna.taskManager.manager.taskManager.TaskManager;
import org.anna.taskManager.tasks.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    private static TaskManager manager;
    private Task task1;
    private Task task2;
    private Epic epic1;
    private Epic epic2;
    private Subtask subtask1;
    private Subtask subtask2;
    private KVServer server;

    @BeforeEach
    public void init() throws IOException, InterruptedException {
        server = new KVServer();
        server.start();
        manager = Managers.getDefault();
        task1 = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        manager.createTask(task1);
        task2 = new Task("Звонок курьеру", "Перенос сроков доставки", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 10, 0));
        manager.createTask(task2);
        epic1 = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic1);
        subtask1 = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask1, epic1.getId());
        epic2 = new Epic("Ремонт", "Кухня и гостиная");
        manager.createEpic(epic2);
        subtask2 = new Subtask("Смета расходов", "Составить план по накоплениям", 120,
                LocalDateTime.of(2022, Month.APRIL, 28, 14, 0));
        manager.createSubtask(subtask2, epic2.getId());
    }

    @AfterEach
    public void destroy() {
        server.stop();
    }


    @DisplayName("GIVEN lists of created tasks " +
            "WHEN no tasks are called " +
            "THEN history is empty")
    @Test
    public void test1_shouldReturnEmptyListForEmptyHistory() {
        // Then
        assertTrue(manager.getHistoryManager().isEmpty(), "История не пустая.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN one Task is called " +
            "THEN the Task is added, size of the history is 1")
    @Test
    public void test2_shouldReturnListWithOneTask() {
        // When
        manager.getTaskById(task1.getId());
        // Then
        assertEquals(1, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertEquals(task1, manager.getHistoryManager().get(0),
                "Порядок задач не соответствует желаемому результату.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN unique Task, Epic and Subtask are called " +
            "THEN all tasks are added, size of the history is 3")
    @Test
    public void test3_shouldReturnListWithTasksOfThreeDifferentTypes() {
        // When
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());
        // Then
        assertEquals(3, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertEquals(task1, manager.getHistoryManager().get(0),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(epic1, manager.getHistoryManager().get(1),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(subtask1, manager.getHistoryManager().get(2),
                "Порядок задач не соответствует желаемому результату.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN the same Task is called three times " +
            "THEN the Task is added once, size of the history is 1")
    @Test
    public void test4_shouldReturnListWithOneTaskForItsSingleRepetition() {
        // When
        manager.getTaskById(task1.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task1.getId());
        // Then
        assertEquals(1, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertEquals(task1, manager.getHistoryManager().get(0),
                "Порядок задач не соответствует желаемому результату.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN the same Task is called twice in the beginning " +
            "THEN this Task is added once, size of the history is 2")
    @Test
    public void test5_shouldReturnListWithTwoTasksForRepetitionAtTheBeginning() {
        // When
        manager.getTaskById(task1.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        // Then
        assertEquals(2, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertEquals(task1, manager.getHistoryManager().get(0),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(task2, manager.getHistoryManager().get(1),
                "Порядок задач не соответствует желаемому результату.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN the same Epic is called twice in the end " +
            "THEN this Epic is added once, size of the history is 2")
    @Test
    public void test6_shouldReturnListWithTwoTasksForRepetitionAtTheEnd() {
        // When
        manager.getEpicById(epic2.getId());
        manager.getEpicById(epic1.getId());
        manager.getEpicById(epic1.getId());
        // Then
        assertEquals(2, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertEquals(epic2, manager.getHistoryManager().get(0),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(epic1, manager.getHistoryManager().get(1),
                "Порядок задач не соответствует желаемому результату.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN 6 unique tasks are called multiple times " +
            "THEN each task is added once, size of the history is 6")
    @Test
    public void test7_shouldReturnListWithSixTasksForMultipleRepetitionsInTheMiddle() {
        // When
        manager.getEpicById(epic2.getId());
        manager.getTaskById(task1.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getTaskById(task2.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getEpicById(epic1.getId());
        // Then
        assertEquals(6, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertEquals(epic2, manager.getHistoryManager().get(0),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(task1, manager.getHistoryManager().get(1),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(task2, manager.getHistoryManager().get(2),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(subtask2, manager.getHistoryManager().get(3),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(subtask1, manager.getHistoryManager().get(4),
                "Порядок задач не соответствует желаемому результату.");
        assertEquals(epic1, manager.getHistoryManager().get(5),
                "Порядок задач не соответствует желаемому результату.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN the task is removed from the beginning of 2 task's history " +
            "THEN size of the history is 1")
    @Test
    public void test8_shouldRemoveTaskAtTheBeginning() {
        // When
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.removeTaskById(task1.getId());
        // Then
        assertEquals(1, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertFalse(manager.getHistoryManager().contains(task1), "Задача не была удалена.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN the task is removed from the end of 2 task's history " +
            "THEN size of the history is 1")
    @Test
    public void test9_shouldRemoveTaskAtTheEnd() {
        // When
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.removeEpicById(epic1.getId());
        // Then
        assertEquals(1, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertFalse(manager.getHistoryManager().contains(epic1), "Задача не была удалена.");
    }

    @DisplayName("GIVEN lists of created tasks " +
            "WHEN the task is removed from the middle of 3 task's history " +
            "THEN size of the history is 2")
    @Test
    public void test10_shouldRemoveTaskInTheMiddle() {
        // When
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getTaskById(task2.getId());
        manager.removeEpicById(epic1.getId());
        // Then
        assertEquals(2, manager.getHistoryManager().size(), "Неверное количество задач.");
        assertFalse(manager.getHistoryManager().contains(epic1), "Задача не была удалена.");
    }
}
