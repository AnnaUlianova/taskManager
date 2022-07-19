package org.anna.taskManager.manager.taskManager;

import org.anna.taskManager.manager.Managers;
import org.anna.taskManager.manager.historyManager.*;
import org.anna.taskManager.tasks.*;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private long taskId;

    protected HashMap<Long, Task> tasks;
    public HashMap<Long, Epic> epics;
    public HashMap<Long, Subtask> subtasks; // TODO
    protected HistoryManager historyManager;
    protected Set<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));
    }

    public ArrayList<Task> getHistoryManager() {
        return historyManager.getHistory();
    }

    // Task's methods
    @Override
    public ArrayList<Task> getListOfTasks() {
        if (tasks != null) {
            return new ArrayList<>(tasks.values());
        }
        return new ArrayList<>();
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        prioritizedTasks.removeIf(task -> task.getType() == Type.TASK);
    }

    @Override
    public Optional<Task> getTaskById(long id) {
        if (!tasks.containsKey(id)) {
            return Optional.empty();
        }
        historyManager.add(tasks.get(id));
        return Optional.of(tasks.get(id));
    }

    @Override
    public void removeTaskById(long id) {
        if (tasks.containsKey(id)) {
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void createTask(Task task) {
        if (timeIsAvailable(task)) {
            task.setId(taskId);
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
            taskId++;
        }
    }

    @Override
    public void updateTask(long id, Task task, Status status) {
        boolean afterTimeCheck = false;
        boolean beforeTimeCheck = false;
        if (tasks.containsKey(id) && task.getStartTime() != null &&
                tasks.get(id).getStartTime() != null) {
            afterTimeCheck = task.getStartTime().isAfter(tasks.get(id).getStartTime()) ||
                    task.getStartTime().equals(tasks.get(id).getStartTime());
            beforeTimeCheck = task.getEndTime().isBefore(tasks.get(id).getEndTime()) ||
                    task.getEndTime().equals(tasks.get(id).getEndTime());
        }
        if (tasks.containsKey(id) && (timeIsAvailable(task)
                || afterTimeCheck && beforeTimeCheck)) {
            task.setId(id);
            task.setStatus(status);
            tasks.put(id, task);
            prioritizedTasks.add(task);
        }
    }

    // Epic's methods
    @Override
    public ArrayList<Epic> getListOfEpics() {
        if (epics != null) {
            return new ArrayList<>(epics.values());
        }
        return new ArrayList<>();
    }

    @Override
    public void removeAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Optional<Epic> getEpicById(long id) {
        if (!epics.containsKey(id)) {
            return Optional.empty();
        }
        historyManager.add(epics.get(id));
        return Optional.of(epics.get(id));
    }

    @Override
    public void removeEpicById(long id) {
        if (epics.containsKey(id)) {
            historyManager.remove(id);
            ArrayList<Long> subIdList = epics.get(id).getSubtasksIdArray();
            for (Long subId : subIdList) {
                if (historyManager.getHistory().contains(subtasks.get(subId))) {
                    historyManager.remove(subId);
                }
            }

            subtasks.entrySet().removeIf(e -> e.getValue().getEpicId() == id);
            epics.remove(id);
        }
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(taskId);
        epics.put(epic.getId(), epic);
        taskId++;
    }

    @Override
    public void updateEpic(long id, Epic epic) {
        if (epics.containsKey(id)) {
            epic.setId(id);
            ArrayList<Long> prevEpicSubId = epics.get(id).getSubtasksIdArray();
            epic.setSubtasksIdArray(prevEpicSubId);
            epics.put(id, epic);
        }
    }

    @Override
    public ArrayList<Subtask> getAllEpicSubtasks(long id) {
        if (epics.containsKey(id)) {
            ArrayList<Subtask> epicsSub = new ArrayList<>();
            for (Long subId : epics.get(id).getSubtasksIdArray()) {
                epicsSub.add(subtasks.get(subId));
            }
            return epicsSub;
        }
        return new ArrayList<>();
    }

    // Subtask's methods
    @Override
    public ArrayList<Subtask> getListOfSubtasks() {
        if (subtasks != null) {
            return new ArrayList<>(subtasks.values());
        }
        return new ArrayList<>();
    }

    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.setSubtasksIdArray(new ArrayList<>());
            checkEpicStartAndEndTime(epic);
        }
        subtasks.clear();
        prioritizedTasks.removeIf(subtask -> subtask.getType() == Type.SUBTASK);
    }

    @Override
    public Optional<Subtask> getSubtaskById(long id) {
        if (!subtasks.containsKey(id)) {
            return Optional.empty();
        }
        historyManager.add(subtasks.get(id));
        return Optional.of(subtasks.get(id));
    }

    @Override
    public void removeSubtaskById(long id) {
        if (subtasks.containsKey(id)) {
            Epic epic = epics.get(subtasks.get(id).getEpicId());
            prioritizedTasks.remove(subtasks.get(id));
            subtasks.remove(id);
            epic.getSubtasksIdArray().remove(id);
            checkEpicStatus(epic);
            checkEpicStartAndEndTime(epic);
            historyManager.remove(id);
        }
    }

    /**
     * Для существующей главной задачи создаёт подзадачу, назначая уникальный идентификатор,
     * добавляет её в коллекцию подзадач, а также в список подзадач главной задачи.
     *
     * @param subtask Новый объект для создания подзадачи.
     * @param epicId Ссылка на идентификатор главной задачи, в которой требуется создать подзадачу.
     */
    @Override
    public void createSubtask(Subtask subtask, long epicId) {
        if (epics.containsKey(epicId) && timeIsAvailable(subtask)) {
            subtask.setId(taskId);
            subtask.setEpicId(epicId);
            subtasks.put(subtask.getId(), subtask);
            epics.get(epicId).getSubtasksIdArray().add(subtask.getId());
            checkEpicStartAndEndTime(epics.get(epicId));
            prioritizedTasks.add(subtask);
            taskId++;
        }
    }

    /**
     * Находит ссылку на главную задачу у подзадачи, подлежащей изменению, заменяет её на новый экземпляр
     * в коллекции подзадач, а также корректирует список подзадач связанной с ним главной задачи,
     * вызывает проверку главной задачи на предмет изменения её статуса.
     *
     * @param id Идентификатор задачи, которая подлжит замене.
     * @param subtask Новый объект подзадачи.
     * @param status Статус нового объекта подзадачи
     *               (не указывается при создании экземпляра класса т.к. подразумевается,
     *               что у новой задачи он по умолчанию равен NEW).
     * @see #updateTask(long, Task, Status)
     */
    @Override
    public void updateSubtask(long id, Subtask subtask, Status status) {
        boolean afterTimeCheck = false;
        boolean beforeTimeCheck = false;
        if (subtasks.containsKey(id) && subtask.getStartTime() != null &&
                subtasks.get(id).getStartTime() != null) {
            afterTimeCheck = subtask.getStartTime().isAfter(subtasks.get(id).getStartTime()) ||
                    subtask.getStartTime().equals(subtasks.get(id).getStartTime());
            beforeTimeCheck = subtask.getEndTime().isBefore(subtasks.get(id).getEndTime()) ||
                    subtask.getEndTime().equals(subtasks.get(id).getEndTime());
        }
        if (subtasks.containsKey(id) && (timeIsAvailable(subtask)
                || afterTimeCheck && beforeTimeCheck)) {
            subtask.setId(id);
            subtask.setStatus(status);
            long previousEpicId = subtasks.get(id).getEpicId();
            subtask.setEpicId(previousEpicId);
            subtasks.put(id, subtask);
            checkEpicStatus(epics.get(subtask.getEpicId()));
            checkEpicStartAndEndTime(epics.get(subtask.getEpicId()));
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    private boolean timeIsAvailable(Task task) {
        if (task.getStartTime() == null) {
            return true;
        }
        return getPrioritizedTasks().stream()
                .filter(createdTask -> createdTask.getStartTime() != null)
                .allMatch(createdTask -> task.getEndTime().isBefore(createdTask.getStartTime())
                || task.getStartTime().isAfter(createdTask.getEndTime()));
    }

    private void checkEpicStatus(Epic epic) {
        boolean isCompleted = getAllEpicSubtasks(epic.getId()).stream()
                .allMatch(task -> task.getStatus().equals(Status.DONE));
        boolean isInProgress = getAllEpicSubtasks(epic.getId()).stream()
                .anyMatch(task -> task.getStatus().equals(Status.IN_PROGRESS)
                        || task.getStatus().equals(Status.DONE));
        if (isCompleted) {
            epic.setStatus(Status.DONE);
        } else if (isInProgress) {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void checkEpicStartAndEndTime(Epic epic) {
        List<Subtask> epicSubs = getAllEpicSubtasks(epic.getId());
        epicSubs.removeIf(subtask -> subtask.getStartTime() == null);

        if (epicSubs.isEmpty()) {
            return;
        }
        int sum = 0;
        LocalDateTime min = epicSubs.get(0).getStartTime();
        LocalDateTime max = epicSubs.get(0).getEndTime();
        for (Subtask subtask : epicSubs) {
            if (subtask.getStartTime().isBefore(min)) {
                min = subtask.getStartTime();
            }
            if (subtask.getEndTime().isAfter(max)) {
                max = subtask.getEndTime();
            }
            sum += subtask.getDuration();
        }
        epic.setDuration(sum);
        epic.setStartTime(min);
        epic.setEndTime(max);
    }

}
