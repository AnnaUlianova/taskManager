package org.anna.managerTest;

import org.anna.taskManager.manager.taskManager.FileBackedTasksManager;
import org.anna.taskManager.tasks.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {

    @DisplayName("GIVEN an empty fileManager " +
            "WHEN the condition is loaded from the empty file " +
            "THEN fields of the loaded fileManager are empty")
    @Test
    public void test1_shouldCreateAnEmptyFileManagerFromTheEmptyFile() throws IOException {
        // Given
        String path1 = "src/main/resources/test1.csv";
        manager = new FileBackedTasksManager(path1);
        // When
        FileBackedTasksManager loadedManager =
                FileBackedTasksManager.loadFromFile(new File(path1));
        // Then
        assertTrue(loadedManager.getHistoryManager().isEmpty(), "История не пуста.");
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty(), "История не пуста.");
        assertTrue(loadedManager.getListOfTasks().isEmpty(), "Коллекция не пуста.");
        assertTrue(loadedManager.getListOfEpics().isEmpty(), "Коллекция не пуста.");
        assertTrue(loadedManager.getListOfSubtasks().isEmpty(), "Коллекция не пуста.");
    }

    @DisplayName("GIVEN a fileManager with one Epic and no history " +
            "WHEN the condition is loaded from the file " +
            "THEN Epic is added to the manager's collection, " +
            "other fields are empty")
    @Test
    public void test2_shouldCreateAFileManagerFromTheFileWithOneEpic() throws IOException {
        // Given
        String path2 = "src/main/resources/test2.csv";
        manager = new FileBackedTasksManager(path2);
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        // When
        FileBackedTasksManager loadedManager =
                FileBackedTasksManager.loadFromFile(new File(path2));
        // Then
        assertTrue(loadedManager.getHistoryManager().isEmpty(), "История не пуста.");
        assertTrue(loadedManager.getListOfTasks().isEmpty(), "Коллекция не пуста.");
        assertTrue(loadedManager.getListOfSubtasks().isEmpty(), "Коллекция не пуста.");
        assertFalse(loadedManager.getListOfEpics().isEmpty(), "Задача не была добавлена.");
        assertEquals(epic, loadedManager.getListOfEpics().get(0), "Задачи не совпадают.");
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty(), "Эпик был добавлен.");
    }

    @DisplayName("GIVEN a fileManager with Epic, it's Subtask and the history " +
            "WHEN the condition is loaded from the file " +
            "THEN Epic Subtask are added to the manager's collections, " +
            "history of tasks is added")
    @Test
    public void test3_shouldCreateAFileManagerFromTheFileWithEpicSubtaskHistory() throws IOException {
        // Given
        String path3 = "src/main/resources/test3.csv";
        manager = new FileBackedTasksManager(path3);
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        manager.getSubtaskById(subtask.getId());
        manager.getEpicById(epic.getId());
        // When
        FileBackedTasksManager loadedManager =
                FileBackedTasksManager.loadFromFile(new File(path3));
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
    }
}
