package org.anna.taskManager.manager.taskManager;

import org.anna.taskManager.tasks.*;
import java.util.*;

public interface TaskManager {

    ArrayList<Task> getListOfTasks();

    void removeAllTasks();

    Optional<Task> getTaskById(long id);

    void removeTaskById(long id);

    void createTask(Task task);

    void updateTask(long id, Task task, Status status);

    ArrayList<Epic> getListOfEpics();

    void removeAllEpics();

    Optional<Epic> getEpicById(long id);

    void removeEpicById(long id);

    void createEpic(Epic epic);

    void updateEpic(long id, Epic epic);

    ArrayList<Subtask> getAllEpicSubtasks(long id);

    ArrayList<Subtask> getListOfSubtasks();

    void removeAllSubtasks();

    Optional<Subtask> getSubtaskById(long id);

    void removeSubtaskById(long id);

    void createSubtask(Subtask subtask, long epicId);

    void updateSubtask(long id, Subtask subtask, Status status);

    List<Task> getHistoryManager();

    Set<Task> getPrioritizedTasks();
}