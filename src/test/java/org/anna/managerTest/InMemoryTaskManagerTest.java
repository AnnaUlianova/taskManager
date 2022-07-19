package org.anna.managerTest;

import org.anna.taskManager.manager.taskManager.InMemoryTaskManager;
import org.anna.taskManager.manager.taskManager.TaskManager;
import org.anna.taskManager.tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    protected TaskManager manager;

    @BeforeEach
    public void init() {
        manager = new InMemoryTaskManager();
    }

    @DisplayName("GIVEN instance of the Task created in the empty map " +
            "WHEN list of Tasks is called " +
            "THEN the Task is shown in the list, list's size is 1")
    @Test
    public void test1_shouldAddOneNewTaskToTheEmptyMap() {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        manager.createTask(task);
        // When
        final List<Task> tasks = manager.getListOfTasks();
        // Then
        assertFalse(tasks.isEmpty(), "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }


    @DisplayName("GIVEN instance of the Task created in the map with one Task" +
            "WHEN list of Tasks is called " +
            "THEN the Task is shown in the list, list's size is 2")
    @Test
    public void test2_shouldAddOneNewTaskToTheMapWithTask() {
        // Given
        manager.createTask(new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник",
                60, LocalDateTime.of(2022, Month.APRIL, 27, 8, 0)));
        Task task = new Task("Звонок курьеру", "Перенос сроков доставки");
        manager.createTask(task);
        // When
        final List<Task> tasks = manager.getListOfTasks();
        // Then
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(1), "Задачи не совпадают.");
    }

    @DisplayName("GIVEN instance of the Epic created in the empty map " +
            "WHEN list of Epics is called " +
            "THEN the Epic is shown in the list, list's size is 1")
    @Test
    public void test3_shouldAddOneNewEpicToTheEmptyMap() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        // When
        final List<Epic> epics = manager.getListOfEpics();
        // Then
        assertFalse(epics.isEmpty(), "Задачи на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.get(0), "Задачи не совпадают.");
    }

    @DisplayName("GIVEN instance of the Epic created in the map with one Epic" +
            "WHEN list of Epics is called " +
            "THEN the Epic is shown in the list, list's size is 2")
    @Test
    public void test4_shouldAddOneNewEpicToTheMapWithEpic() {
        // Given
        manager.createEpic(new Epic("Отпуск", "Поездка в горы в декабре"));
        Epic epic = new Epic("Ремонт", "Кухня и гостиная");
        manager.createEpic(epic);
        // When
        final List<Epic> epics = manager.getListOfEpics();
        // Then
        assertEquals(2, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.get(1), "Задачи не совпадают.");
    }

    @DisplayName("GIVEN instance of the Epic and it's Subtask created in the empty map " +
            "WHEN list of Subtasks is called " +
            "THEN the Subtask is shown in the list, list's size is 1")
    @Test
    public void test5_shouldAddOneNewSubtaskToTheEmptyMap() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        // When
        final List<Subtask> subtasks = manager.getListOfSubtasks();
        // Then
        assertFalse(subtasks.isEmpty(), "Задачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");
    }

    @DisplayName("GIVEN instance of the Subtask created in the map with one Subtask" +
            "WHEN list of Subtasks is called " +
            "THEN the Subtask is shown in the list, list's size is 2")
    @Test
    public void test6_shouldAddOneNewSubtaskToTheMapWithSubtask() {
        // Given
        Epic epic = new Epic("Ремонт", "Кухня и гостиная");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask("Звонок курьеру", "Перенос сроков доставки", 10,
                LocalDateTime.of(2022, Month.APRIL, 27, 14, 0)), epic.getId());
        Subtask subtask = new Subtask("Смета расходов", "Составить план по накоплениям", 120,
                LocalDateTime.of(2022, Month.APRIL, 28, 14, 0));
        manager.createSubtask(subtask, epic.getId());
        // When
        final List<Subtask> subtasks = manager.getListOfSubtasks();
        // Then
        assertEquals(2, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.get(1), "Задачи не совпадают.");
    }

    @DisplayName("GIVEN instance of the Subtask created in the empty map " +
            "WHEN create method is called with the wrong Epic id " +
            "THEN the Subtask is not added")
    @Test
    public void test7_shouldNotAddNewSubtaskWithTheWrongEpicId() {
        // Given
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        // When
        manager.createSubtask(subtask, 10);
        // Then
        assertTrue(manager.getSubtaskById(subtask.getId()).isEmpty(),
                "Задача была добавлена для неверного/несуществующего эпика.");
    }

    @DisplayName("GIVEN empty lists " +
            "WHEN attempting to get all tasks from each collection " +
            "THEN each list is returned empty")
    @Test
    public void test8_shouldReturnAnEmptyListForTheEmptyTaskMap() {
        // Then
        assertAll(
                () -> assertTrue(manager.getListOfTasks().isEmpty()),
                () -> assertTrue(manager.getListOfEpics().isEmpty()),
                () -> assertTrue(manager.getListOfSubtasks().isEmpty())
        );
    }

    @DisplayName("GIVEN empty lists " +
            "WHEN attempting to remove all tasks from each collection " +
            "THEN each list is returned empty")
    @Test
    public void test9_shouldRemainEmptyAfterRemovingMethodOnTheEmptyMap() {
        // When
        manager.removeAllTasks();
        manager.removeAllEpics();
        manager.removeAllSubtasks();
        // Then
        assertAll(
                () -> assertTrue(manager.getListOfTasks().isEmpty()),
                () -> assertTrue(manager.getListOfEpics().isEmpty()),
                () -> assertTrue(manager.getListOfSubtasks().isEmpty())
        );
    }

    @DisplayName("GIVEN instance of the Subtask created " +
            "WHEN attempting to remove all Tasks from the collection " +
            "THEN Task's list is returned empty")
    @Test
    public void test10_shouldRemoveAllTasksFromTheMapWithTasks() {
        // Given
        manager.createTask(new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник",
                60, LocalDateTime.of(2022, Month.APRIL, 27, 8, 0)));
        // When
        manager.removeAllTasks();
        // Then
        assertTrue(manager.getListOfTasks().isEmpty(), "Задачи на удаляются.");
    }

    @DisplayName("GIVEN instances of Epic and it's Subtask created " +
            "WHEN attempting to remove all Subtasks " +
            "THEN Subtasks list is returned empty, " +
            "all the links in epics are deleted")
    @Test
    public void test11_shouldRemoveAllSubtasksFromTheMapWithSubtasks() {
        // Given
        Epic epic = new Epic("Ремонт", "Кухня и гостиная");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask("Звонок курьеру", "Перенос сроков доставки", 10,
                LocalDateTime.of(2022, Month.APRIL, 27, 14, 0)), epic.getId());
        // When
        manager.removeAllSubtasks();
        // Then
        assertTrue(manager.getListOfSubtasks().isEmpty(), "Подзадачи на удаляются.");
        for (Epic epicTask : manager.getListOfEpics()) {
            assertTrue(epicTask.getSubtasksIdArray().isEmpty(),
                    "У эпика осталась ссылка на несуществующую подзадачу.");
        }
    }

    @DisplayName("GIVEN instances of Epic and it's Subtask created " +
            "WHEN attempting to remove all Epics " +
            "THEN Subtasks and Epics lists are returned empty")
    @Test
    public void test12_shouldRemoveAllEpicsAndSubtasksFromTheMaps() {
        // Given
        Epic epic = new Epic("Ремонт", "Кухня и гостиная");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask("Звонок курьеру", "Перенос сроков доставки", 10,
                LocalDateTime.of(2022, Month.APRIL, 27, 14, 0)), epic.getId());
        // When
        manager.removeAllEpics();
        // Then
        assertTrue(manager.getListOfEpics().isEmpty(), "Эпики на удаляются.");
        assertTrue(manager.getListOfSubtasks().isEmpty(), "При удалении эпиков подзадачи на удаляются.");
    }

    @DisplayName("GIVEN empty collections " +
            "WHEN attempting to get a task with the wrong id " +
            "THEN empty optional is returned")
    @Test
    public void test13_shouldReturnEmptyOptionalForTheEmptyMapOrWrongId() {
        // Then
        assertAll(
                () -> assertTrue(manager.getTaskById(10).isEmpty()),
                () -> assertTrue(manager.getEpicById(10).isEmpty()),
                () -> assertTrue(manager.getSubtaskById(10).isEmpty())
        );
    }

    @DisplayName("GIVEN instance of the Task created " +
            "WHEN attempting to get a Task with the existing id " +
            "THEN optional of the Task is returned, " +
            "Task is added to the end of the history")
    @Test
    public void test14_shouldReturnOptionalOfTheCreatedTaskAndAddTaskToTheHistory() {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        manager.createTask(task);
        // When
        manager.getTaskById(task.getId());
        // Then
        assertTrue(manager.getTaskById(task.getId()).isPresent(), "Задача на возвращается.");
        Task savedTask = manager.getTaskById(task.getId()).get();
        assertEquals(savedTask, task, "Задачи не совпадают.");
        assertEquals(1, manager.getHistoryManager().size(), "История пуста.");
        assertTrue(manager.getHistoryManager().contains(savedTask), "Задача не была добавлена в историю.");
        assertEquals(task, manager.getHistoryManager().get(manager.getHistoryManager().size() - 1),
                "Задача не в конце списка.");
    }

    @DisplayName("GIVEN instance of the Epic created " +
            "WHEN attempting to get an Epic with the existing id " +
            "THEN optional of the Epic is returned, " +
            "Epic is added to the end of the history")
    @Test
    public void test15_shouldReturnOptionalOfTheCreatedEpicAndAddEpicToTheHistory() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        // When
        manager.getEpicById(epic.getId());
        // Then
        assertTrue(manager.getEpicById(epic.getId()).isPresent(), "Задача на возвращается.");
        Epic savedEpic = manager.getEpicById(epic.getId()).get();
        assertEquals(savedEpic, epic, "Задачи не совпадают.");
        assertEquals(1, manager.getHistoryManager().size(), "История пуста.");
        assertTrue(manager.getHistoryManager().contains(savedEpic), "Задача не была добавлена в историю.");
        assertEquals(epic, manager.getHistoryManager().get(manager.getHistoryManager().size() - 1),
                "Задача не в конце списка.");
    }

    @DisplayName("GIVEN instances of the Epic and it's Subtask created " +
            "WHEN attempting to get a Subtask with the existing id " +
            "THEN optional of the Subtask is returned, " +
            "Subtask is added to the end of the history")
    @Test
    public void test16_shouldReturnOptionalOfTheCreatedSubtaskAndAddSubtaskToTheHistory() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createEpic(epic);
        manager.createSubtask(subtask, epic.getId());
        // When
        manager.getSubtaskById(subtask.getId());
        // Then
        assertTrue(manager.getSubtaskById(subtask.getId()).isPresent(), "Задача на возвращается.");
        Subtask savedSubtask = manager.getSubtaskById(subtask.getId()).get();
        assertEquals(savedSubtask, subtask, "Задачи не совпадают.");
        assertEquals(1, manager.getHistoryManager().size(), "История пуста.");
        assertTrue(manager.getHistoryManager().contains(subtask), "Задача не была добавлена в историю.");
        assertEquals(subtask, manager.getHistoryManager().get(manager.getHistoryManager().size() - 1),
                "Задача не в конце списка.");
    }

    @DisplayName("GIVEN instance of the Task created and added to the history " +
            "WHEN attempting to remove a Task with the existing id " +
            "THEN Task is removed from the collection and the history")
    @Test
    public void test17_shouldRemoveCreatedTaskFromTheMapAndTheHistory() {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        manager.createTask(task);
        manager.getTaskById(task.getId());
        // When
        manager.removeTaskById(task.getId());
        // Then
        assertFalse(manager.getListOfTasks().contains(task), "Задача не была удалена из коллекции.");
        assertFalse(manager.getHistoryManager().contains(task), "Задача не была удалена из истории.");
    }

    @DisplayName("GIVEN instances of the Epic and it's Subtask created and added to the history " +
            "WHEN attempting to remove an Epic with the existing id " +
            "THEN Epic and it's Subtask are removed from the collections and the history")
    @Test
    public void test18_shouldRemoveCreatedEpicFromTheMapAndTheHistory() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());
        ArrayList<Subtask> subList = manager.getAllEpicSubtasks(epic.getId());
        // When
        manager.removeEpicById(epic.getId());
        // Then
        assertFalse(manager.getListOfEpics().contains(epic), "Задача не была удалена из коллекции.");
        assertFalse(manager.getHistoryManager().contains(epic), "Задача не была удалена из истории.");
        assertFalse(manager.getListOfSubtasks().stream().anyMatch(subList::contains),
                "Подзадачи эпика не были удалены из коллекции.");
        assertFalse(manager.getHistoryManager().stream().anyMatch(subList::contains),
                "Подзадачи эпика не были удалены из истории.");
    }

    @DisplayName("GIVEN instances of the Epic and it's Subtask created and added to the history " +
            "WHEN attempting to remove a Subtask with the existing id " +
            "THEN Subtask is removed from the collections, it's Epic's list and the history")
    @Test
    public void test19_shouldRemoveCreatedSubtaskFromTheMapAndTheHistory() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());
        // When
        manager.removeSubtaskById(subtask.getId());
        // Then
        assertFalse(manager.getListOfSubtasks().contains(subtask), "Задача не была удалена из коллекции.");
        assertFalse(manager.getHistoryManager().contains(subtask), "Задача не была удалена из истории.");
        assertFalse(epic.getSubtasksIdArray().contains(subtask.getId()),
                "Задача не была уделена из списка подзадач эпика.");
    }

    @DisplayName("GIVEN empty collections " +
            "WHEN attempting to remove all types of tasks " +
            "THEN empty lists are got")
    @Test
    public void test20_shouldRemainConditionsOfTheMapAndTheHistoryForTheEmptyMap() {
        // When
        manager.removeTaskById(1);
        manager.removeEpicById(2);
        manager.removeSubtaskById(0);
        // Then
        assertAll(
                () -> assertEquals(0, manager.getListOfTasks().size(), "Неверное количество задач."),
                () -> assertEquals(0, manager.getListOfEpics().size(), "Неверное количество задач."),
                () -> assertEquals(0, manager.getListOfSubtasks().size(), "Неверное количество задач."),
                () -> assertEquals(0, manager.getHistoryManager().size(), "Неверное количество задач.")
        );
    }

    @DisplayName("GIVEN instances of Task, Epic and it's Subtask created and added to the history " +
            "WHEN attempting to remove all types of tasks with the wrong id " +
            "THEN tasks are not deleted")
    @Test
    public void test21_shouldRemainConditionsOfTheMapAndTheHistoryForTheWrongId() {
        // Given
        manager.createTask(new Task("Звонок курьеру", "Перенос сроков доставки", 10,
                LocalDateTime.of(2022, Month.APRIL, 27, 14, 0)));
        manager.createEpic(new Epic("Ремонт", "Кухня и гостиная"));
        manager.createSubtask(new Subtask("Смета расходов", "Составить план по накоплениям", 120,
                LocalDateTime.of(2022, Month.APRIL, 28, 14, 0)), 1);
        manager.getTaskById(0);
        manager.getEpicById(1);
        manager.getSubtaskById(2);
        int beforeHistorySize  = manager.getHistoryManager().size();
        // When
        manager.removeTaskById(10);
        manager.removeEpicById(20);
        manager.removeSubtaskById(30);
        // Then
        assertAll(
                () -> assertEquals(1, manager.getListOfTasks().size(), "Неверное количество задач."),
                () -> assertEquals(1, manager.getListOfEpics().size(), "Неверное количество задач."),
                () -> assertEquals(1, manager.getListOfSubtasks().size(),
                        "Неверное количество задач."),
                () -> assertEquals(beforeHistorySize, manager.getHistoryManager().size(),
                        "Неверное количество задач.")
        );
    }

    @DisplayName("GIVEN instance of Task created " +
            "WHEN attempting to update the Task " +
            "THEN previous Task is replaced")
    @Test
    public void test22_shouldReplaceCreatedTaskById() {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        manager.createTask(task);
        Task newTask = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник",
                60, LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        // When
        manager.updateTask(task.getId(), newTask, Status.DONE);
        // Then
        assertEquals(1, manager.getListOfTasks().size(), "Неверное количество задач.");
        assertFalse(manager.getListOfTasks().contains(task), "Доступна старая версия задачи.");
        assertTrue(manager.getListOfTasks().contains(newTask), "Новая версия задачи не была добавлена.");
    }

    @DisplayName("GIVEN instance of Epic created " +
            "WHEN attempting to update the Epic " +
            "THEN previous Epic is replaced, Subtask links are reassigned")
    @Test
    public void test23_shouldReplaceCreatedEpicByIdAndGetItsSubtasks() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        manager.createSubtask(new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30)), epic.getId());
        Epic newEpic = new Epic("Отпуск", "Поездка на море");
        // When
        manager.updateEpic(epic.getId(), newEpic);
        // Then
        assertEquals(1, manager.getListOfEpics().size(), "Неверное количество задач.");
        assertFalse(manager.getListOfEpics().contains(epic), "Доступна старая версия задачи.");
        assertTrue(manager.getListOfEpics().contains(newEpic), "Новая версия задачи не была добавлена.");
        assertEquals(epic.getSubtasksIdArray(), newEpic.getSubtasksIdArray(),
                "Подзадачи не были переназначены");
    }

    @DisplayName("GIVEN instances of Epic and it's Subtask created " +
            "WHEN attempting to update the Subtask " +
            "THEN previous Subtask is replaced, Epic's link is reassigned")
    @Test
    public void test24_shouldReplaceCreatedSubtaskByIdAndGetItsEpicId() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        Subtask newSubtask = new Subtask("Авиабилеты", "Пересадка в Лондоне", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        // When
        manager.updateSubtask(subtask.getId(), newSubtask, Status.IN_PROGRESS);
        // Then
        assertEquals(1, manager.getListOfSubtasks().size(), "Неверное количество задач.");
        assertFalse(manager.getListOfSubtasks().contains(subtask), "Доступна старая версия задачи.");
        assertTrue(manager.getListOfSubtasks().contains(newSubtask), "Новая версия задачи не была добавлена.");
        assertEquals(subtask.getEpicId(), newSubtask.getEpicId(), "Эпик не был переназначен.");
    }

    @DisplayName("GIVEN empty collections " +
            "WHEN attempting to update tasks " +
            "THEN empty lists are got")
    @Test
    public void test25_shouldRemainConditionsOfTheMapForTheEmptyMap() {
        // When
        manager.updateSubtask(2, new Subtask("Смета расходов", "Составить план по накоплениям",
                        120, LocalDateTime.of(2022, Month.APRIL, 28, 14, 0)),
                Status.IN_PROGRESS);
        manager.updateEpic(1, new Epic("Ремонт", "Кухня и гостиная"));
        manager.updateTask(0, new Task("Звонок курьеру", "Перенос сроков доставки", 10,
                LocalDateTime.of(2022, Month.APRIL, 27, 14, 0)), Status.DONE);
        // Then
        assertAll(
                () -> assertEquals(0, manager.getListOfTasks().size(), "Неверное количество задач."),
                () -> assertEquals(0, manager.getListOfEpics().size(), "Неверное количество задач."),
                () -> assertEquals(0, manager.getListOfSubtasks().size(), "Неверное количество задач.")
        );
    }

    @DisplayName("GIVEN instances of Task, Epic and it's Subtask created " +
            "WHEN attempting to update tasks with wrong id " +
            "THEN tasks and size of the lists remain")
    @Test
    public void test26_shouldRemainConditionsOfTheMapForTheWrongId() {
        // Given
        Task task = new Task("Прогулка с собакой", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 27, 8, 0));
        manager.createTask(task);
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        // When
        manager.updateSubtask(3, new Subtask("Смета расходов", "Составить план по накоплениям",
                        120, LocalDateTime.of(2022, Month.APRIL, 28, 14, 0)),
                Status.IN_PROGRESS);
        manager.updateEpic(0, new Epic("Ремонт", "Кухня и гостиная"));
        manager.updateTask(1, new Task("Звонок курьеру", "Перенос сроков доставки", 10,
                LocalDateTime.of(2022, Month.APRIL, 27, 14, 0)), Status.DONE);
        // Then
        assertAll(
                () -> assertEquals(1, manager.getListOfTasks().size(), "Неверное количество задач."),
                () -> assertEquals(1, manager.getListOfEpics().size(), "Неверное количество задач."),
                () -> assertEquals(1, manager.getListOfSubtasks().size(), "Неверное количество задач."),
                () -> assertTrue(manager.getListOfTasks().contains(task),
                        "Прежняя задача осталась без изменений."),
                () -> assertTrue(manager.getListOfEpics().contains(epic),
                        "Прежняя задача осталась без изменений."),
                () -> assertTrue(manager.getListOfSubtasks().contains(subtask),
                        "Прежняя задача осталась без изменений.")
        );
    }

    @DisplayName("GIVEN empty collections " +
            "WHEN attempting to get Epic's subtasks with wrong id " +
            "THEN an empty list is returned")
    @Test
    public void test27_shouldReturnAnEmptyListForTheWrongEpicId() {
        // Then
        assertTrue(manager.getAllEpicSubtasks(5).isEmpty());
    }

    @DisplayName("GIVEN instances of Epic and it's Subtask created " +
            "WHEN attempting to get Epic's subtasks " +
            "THEN a list with one Task is returned")
    @Test
    public void test28_shouldReturnAListOfSubtasksForTheEpic() {
        // Given
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 26, 23, 30));
        manager.createSubtask(subtask, epic.getId());
        // When
        ArrayList<Subtask> subArr = manager.getAllEpicSubtasks(epic.getId());
        // Then
        assertEquals(1, subArr.size(), "Неверное количество задач.");
        assertEquals(subtask, subArr.get(0), "Задачи не совпадают.");
    }

    @DisplayName("GIVEN instances of Task created " +
            "WHEN attempting to create a Task within duration of the Task " +
            "THEN the Task is not created ")
    @Test
    public void test29_shouldNotCreateTaskWithinDurationOfAnotherTask() {
        // Given
        Task t1 = new Task("Прогулка", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 28, 12, 0));
        manager.createTask(t1);
        // When
        Task t2 = new Task("Покупка", "В пятёрочке", 15,
                LocalDateTime.of(2022, Month.APRIL, 28, 12, 20));
        manager.createTask(t2);
        // Then
        assertEquals(1, manager.getListOfTasks().size(), "Неверное количество задач.");
        assertFalse(manager.getListOfTasks().contains(t2), "Была добавлена задача с неправильным временем.");
    }

    @DisplayName("GIVEN instance of Task created " +
            "WHEN attempting to create a Task within duration of the Task " +
            "THEN the Task is not created ")
    @Test
    public void test30_shouldNotCreateTaskWithinDurationOfAnotherTask() {
        // Given
        Task t1 = new Task("Прогулка", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 28, 12, 0));
        manager.createTask(t1);
        // When
        Task t2 = new Task("Покупка", "В пятёрочке", 15,
                LocalDateTime.of(2022, Month.APRIL, 28, 12, 20));
        manager.createTask(t2);
        // Then
        assertEquals(1, manager.getListOfTasks().size(), "Неверное количество задач.");
        assertFalse(manager.getListOfTasks().contains(t2), "Была добавлена задача с неправильным временем.");
    }

    @DisplayName("GIVEN instances of Task created " +
            "WHEN attempting to update a Task within duration of the Task " +
            "THEN the Task is not updated ")
    @Test
    public void test31_shouldNotUpdateTaskWithinDurationOfAnotherTask() {
        // Given
        Task t1 = new Task("Прогулка", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 28, 12, 0));
        Task t2 = new Task("Покупка", "В пятёрочке", 15,
                LocalDateTime.of(2022, Month.APRIL, 28, 15, 0));
        manager.createTask(t1);
        manager.createTask(t2);
        // When
        manager.updateTask(t2.getId(), new Task("Покупка", "В пятёрочке", 15,
                LocalDateTime.of(2022, Month.APRIL, 28, 12, 30)), Status.NEW);
        // Then
        assertTrue(manager.getListOfTasks().contains(t2), "Задача отсутствует.");
        assertEquals(t2, manager.getListOfTasks().get(1), "Задача была изменена.");
    }

    @DisplayName("GIVEN instances of Task created " +
            "WHEN attempting to update a Task with the Task with no time units " +
            "THEN the Task is updated ")
    @Test
    public void test32_shouldUpdateTaskWithTaskWithNoTimeUnits() {
        // Given
        Task t1 = new Task("Прогулка", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 28, 12, 0));
        Task t2 = new Task("Покупка", "В пятёрочке", 15,
                LocalDateTime.of(2022, Month.APRIL, 28, 15, 0));
        manager.createTask(t1);
        manager.createTask(t2);
        // When
        Task t3 = new Task("Покупка", "В пятёрочке");
        manager.updateTask(t2.getId(), t3, Status.NEW);
        // Then
        assertTrue(manager.getListOfTasks().contains(t3), "Задача отсутствует.");
        assertEquals(t3, manager.getListOfTasks().get(1), "Задача не была изменена.");
    }

    @DisplayName("GIVEN instances of Task and Subtask created " +
            "WHEN attempting to get order prioritized by time with no null fields " +
            "THEN all tasks are shown in the start time ascending order ")
    @Test
    public void test33_shouldReturnAllTasksInTheAscendingOrder() {
        // Given
        Task t1 = new Task("Прогулка", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 28, 15, 0));
        Task t2 = new Task("Покупка", "В пятёрочке", 15,
                LocalDateTime.of(2022, Month.APRIL, 28, 11, 0));
        manager.createTask(t1);
        manager.createTask(t2);
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask s1 = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 27, 23, 30));
        manager.createSubtask(s1, epic.getId());
        // When
        ArrayList<Task> tasks = new ArrayList<>(manager.getPrioritizedTasks());
        // Then
        assertEquals(3, tasks.size(), "Неверное количество задач.");
        assertEquals(s1, tasks.get(0), "Порядок задач не соответствует желаемому результату.");
        assertEquals(t2, tasks.get(1), "Порядок задач не соответствует желаемому результату.");
        assertEquals(t1, tasks.get(2), "Порядок задач не соответствует желаемому результату.");
    }

    @DisplayName("GIVEN instances of Task and Subtask created " +
            "WHEN attempting to get order prioritized by time with some null fields " +
            "THEN all tasks are shown in the start time ascending order " +
            "with tasks with tasks with null values at the end")
    @Test
    public void test33_shouldReturnAllTasksInTheAscendingOrderWithNullTaskValuesAtTheEnd() {
        // Given
        Task t1 = new Task("Прогулка", "Поводок за дверью, не забыть намордник", 60,
                LocalDateTime.of(2022, Month.APRIL, 28, 15, 0));
        Task t2 = new Task("Покупка", "В пятёрочке");
        Task t3 = new Task("Кодинг", "Доделать проект");
        manager.createTask(t1);
        manager.createTask(t2);
        manager.createTask(t3);
        Epic epic = new Epic("Отпуск", "Поездка в горы в декабре");
        manager.createEpic(epic);
        Subtask s1 = new Subtask("Авиабилеты", "Рейс без пересадок", 30,
                LocalDateTime.of(2022, Month.APRIL, 27, 23, 30));
        manager.createSubtask(s1, epic.getId());
        // When
        ArrayList<Task> tasks = new ArrayList<>(manager.getPrioritizedTasks());
        // Then
        assertEquals(4, tasks.size(), "Неверное количество задач.");
        assertEquals(s1, tasks.get(0), "Порядок задач не соответствует желаемому результату.");
        assertEquals(t1, tasks.get(1), "Порядок задач не соответствует желаемому результату.");
        assertEquals(t2, tasks.get(2), "Порядок задач не соответствует желаемому результату.");
        assertEquals(t3, tasks.get(3), "Порядок задач не соответствует желаемому результату.");
    }
}

