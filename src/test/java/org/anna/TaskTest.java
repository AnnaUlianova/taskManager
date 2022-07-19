package org.anna;

import org.anna.server.KVServer;
import org.anna.taskManager.manager.Managers;
import org.anna.taskManager.manager.taskManager.TaskManager;
import org.anna.taskManager.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    private static TaskManager manager;
    private KVServer server;

    @BeforeEach
    public void init() throws IOException, InterruptedException {
        server = new KVServer();
        server.start();
        manager = Managers.getDefault();
    }

    @AfterEach
    public void destroy() {
        server.stop();
    }

    @DisplayName("GIVEN a new instance of Task " +
            "WHEN the Task is created " +
            "THEN Task's end time is start time plus duration")
    @Test
    public void test1_shouldCountEndTimeAsStartTimePlusDuration() {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        // When
        manager.createTask(task);
        // Then
        assertEquals(task.getEndTime(), task.getStartTime().plusMinutes(60),
                "Неверное время окончания выполнения задачи.");
    }

    @DisplayName("GIVEN an instance of Task " +
            "WHEN the Task is created " +
            "THEN time units for epic remains null")
    @Test
    public void test2_shouldSetNullTimeUnitsForTask() {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник");
        // When
        manager.createTask(task);
        // Then
        assertEquals(0, task.getDuration(), "Неверная продолжительность выполнения.");
        assertNull(task.getStartTime(), "Неверное время старта задачи.");
        assertNull(task.getEndTime(), "Неверное время окончания выполнения задачи.");
    }
}
