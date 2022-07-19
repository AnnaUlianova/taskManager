package org.anna;

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

public class EpicTest {

    private static TaskManager manager;
    private Epic epic;
    private KVServer server;

    @BeforeEach
    public void init() throws IOException, InterruptedException {
        server = new KVServer();
        server.start();
        manager = Managers.getDefault();
        epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
    }

    @AfterEach
    public void destroy() {
        server.stop();
    }

    // Epic's status tests
    @DisplayName("GIVEN an instance of Epic " +
            "WHEN no Subtasks are added " +
            "THEN status of Epic remains NEW")
    @Test
    public void test1_shouldSetStatusNewForEpicWithNoSubtasks() {
        assertEquals(epic.getStatus(), Status.NEW);
    }

    @DisplayName("GIVEN two new instances of Subtask " +
            "WHEN status for both Subtasks is set to DONE " +
            "THEN status of Epic changes to DONE")
    @Test
    public void test2_shouldSetStatusDoneForEpicWithAllDoneSubtasks() {
        // Given
        manager.createSubtask(new Subtask("Авиабилеты", "Рейс без пересадок",
                30, LocalDateTime.of(2022, Month.APRIL, 26, 23, 30)), epic.getId());
        manager.createSubtask(new Subtask("Бронь гостиницы", "Отель 3*+",
                45, LocalDateTime.of(2022, Month.APRIL, 27, 2, 0)), epic.getId());
        // When
        manager.updateSubtask(1, new Subtask("Авиабилеты", "Рейс с пересадкой в Мск",
                30, LocalDateTime.of(2022, Month.APRIL, 26, 23, 30)), Status.DONE);
        manager.updateSubtask(2, new Subtask("Бронь гостиницы", "Спа-отель",
                45, LocalDateTime.of(2022, Month.APRIL, 27, 2, 0)), Status.DONE);
        // Then
        assertEquals(epic.getStatus(), Status.DONE);
    }

    @DisplayName("GIVEN two new instances of Subtask " +
            "WHEN status of one Subtask is set to DONE " +
            "THEN status of Epic changes to IN_PROGRESS")
    @Test
    public void test3_shouldSetStatusInProgressForEpicWithNewAndDoneSubtasks() {
        // Given
        manager.createSubtask(new Subtask("Авиабилеты", "Рейс без пересадок",
                45, LocalDateTime.of(2022, Month.APRIL, 26, 23, 30)), epic.getId());
        manager.createSubtask(new Subtask("Бронь гостиницы", "Отель 3*+",
                45, LocalDateTime.of(2022, Month.APRIL, 27, 2, 0)), epic.getId());
        // When
        manager.updateSubtask(1, new Subtask("Авиабилеты", "Рейс с пересадкой в Мск",
                30, LocalDateTime.of(2022, Month.APRIL, 26, 23, 30)), Status.DONE);
        // Then
        assertEquals(epic.getStatus(), Status.IN_PROGRESS);
    }

    @DisplayName("GIVEN two new instances of Subtask " +
            "WHEN status of both Subtasks is set to IN_PROGRESS " +
            "THEN status of Epic changes to IN_PROGRESS")
    @Test
    public void test4_shouldSetStatusInProgressForEpicWithAllInProgressSubtasks() {
        // Given
        manager.createSubtask(new Subtask("Авиабилеты", "Рейс без пересадок",
                30, LocalDateTime.of(2022, Month.APRIL, 26, 23, 30)),
                epic.getId());
        manager.createSubtask(new Subtask("Бронь гостиницы", "Отель 3*+",
                45, LocalDateTime.of(2022, Month.APRIL, 27, 2, 0)),
                epic.getId());
        // When
        manager.updateSubtask(1, new Subtask("Авиабилеты", "Рейс с пересадкой в Мск",
                30, LocalDateTime.of(2022, Month.APRIL, 26, 23, 30)),
                Status.IN_PROGRESS);
        manager.updateSubtask(2, new Subtask("Бронь гостиницы", "Спа-отель",
                45, LocalDateTime.of(2022, Month.APRIL, 27, 2, 0)),
                Status.IN_PROGRESS);
        // Then
        assertEquals(epic.getStatus(), Status.IN_PROGRESS);
    }

    // Epic's time units tests
    @DisplayName("GIVEN an instance of Epic " +
            "WHEN no Subtasks are created " +
            "THEN time units for epic remains null")
    @Test
    public void test5_shouldSetNullTimeUnitsForEpicWithNoSubtasks() {
        // Then
        assertEquals(0, epic.getDuration(), "Неверная продолжительность выполнения.");
        assertNull(epic.getStartTime(), "Неверное время старта задачи.");
        assertNull(epic.getEndTime(), "Неверное время окончания выполнения задачи.");
    }

    @DisplayName("GIVEN an instance of Epic's Subtask " +
            "WHEN the Subtask without time units is created " +
            "THEN time units for epic remains null")
    @Test
    public void test6_shouldSetNullTimeUnitsForEpicWithSubtasksWithoutTimeUnits() {
        // Given
        manager.createSubtask(new Subtask("Звонок курьеру", "Перенос сроков доставки"), epic.getId());
        // Then
        assertEquals(0, epic.getDuration(), "Неверная продолжительность выполнения.");
        assertNull(epic.getStartTime(), "Неверное время старта задачи.");
        assertNull(epic.getEndTime(), "Неверное время окончания выполнения задачи.");
    }

    @DisplayName("GIVEN three new instances of Subtask " +
            "WHEN all Subtasks are created with time: S1 earliest, S2 latest " +
            "THEN Epic starts at S1 start time, ends at S2 end time, " +
            "Epic's duration is sum of all Subtask's durations")
    @Test
    public void test7_shouldSetRightDurationAndStartAndEndTimeFromSubtasks() {
        // Given
        Subtask s1 = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        Subtask s2 = new Subtask("Бронь гостиницы", "Отель 3*+", 45,
                LocalDateTime.of(2022, Month.APRIL, 27, 6, 0));
        Subtask s3 = new Subtask("Билеты в музей", "Лувр", 45,
                LocalDateTime.of(2022, Month.APRIL, 27, 2, 0));
        // When
        manager.createSubtask(s1, epic.getId());
        manager.createSubtask(s2, epic.getId());
        manager.createSubtask(s3, epic.getId());
        // Then
        assertEquals(epic.getStartTime(), s1.getStartTime(), "Неверное время старта задачи.");
        assertEquals(epic.getEndTime(), s2.getEndTime(), "Неверное время окончания выполнения задачи.");
        assertEquals(epic.getDuration(), s1.getDuration() + s2.getDuration() + s3.getDuration(),
                "Неверная продолжительность выполнения.");
    }

    @DisplayName("GIVEN two new instances of Subtask " +
            "WHEN only one Subtask is created with time " +
            "THEN Epic starts at Subtask start time, ends at Subtask end time, " +
            "Epic's duration is Subtask's duration")
    @Test
    public void test8_shouldSetRightDurationAndStartAndEndTimeFromSubtasksThatHaveTimeUnits() {
        // Given
        Subtask s1 = new Subtask("Авиабилеты", "Рейс без пересадок");
        Subtask s2 = new Subtask("Бронь гостиницы", "Отель 3*+", 45,
                LocalDateTime.of(2022, Month.APRIL, 27, 6, 0));
        // When
        manager.createSubtask(s1, epic.getId());
        manager.createSubtask(s2, epic.getId());
        // Then
        assertEquals(epic.getStartTime(), s2.getStartTime(), "Неверное время старта задачи.");
        assertEquals(epic.getEndTime(), s2.getEndTime(), "Неверное время окончания выполнения задачи.");
        assertEquals(epic.getDuration(), s2.getDuration(), "Неверная продолжительность выполнения.");
    }
}