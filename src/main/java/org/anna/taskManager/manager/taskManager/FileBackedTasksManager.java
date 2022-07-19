package org.anna.taskManager.manager.taskManager;

import org.anna.customExceptions.ManagerSaveException;
import org.anna.taskManager.manager.historyManager.HistoryManager;
import org.anna.taskManager.tasks.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.*;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private final String path;

    public FileBackedTasksManager(String path) {
        this.path = path;
    }

    public static FileBackedTasksManager loadFromFile(File file) throws IOException {
        FileBackedTasksManager manager = new FileBackedTasksManager("src/main/resources/test.csv");
        String str;
        try (BufferedReader buffer = new BufferedReader(
                new FileReader(file.getPath()))) {
            buffer.readLine();
            while ((str = buffer.readLine()) != null) {
                if (!str.isEmpty()) {
                    String[] taskFields = str.split(",");
                    switch (Type.valueOf(taskFields[1])) {
                        case TASK:
                            manager.tasks.put(Long.valueOf(taskFields[0]), manager.taskFromString(str));
                            break;
                        case EPIC:
                            manager.epics.put(Long.valueOf(taskFields[0]), (Epic) manager.taskFromString(str));
                            break;
                        case SUBTASK:
                            manager.subtasks.put(Long.valueOf(taskFields[0]),
                                    (Subtask) manager.taskFromString(str));
                            break;
                    }
                    continue;
                }
                str = buffer.readLine();
                if (str != null) {
                    for (Long id : fromString(str)) {
                        if (manager.tasks.containsKey(id)) {
                            manager.historyManager.add(manager.tasks.get(id));
                        } else if (manager.epics.containsKey(id)) {
                            manager.historyManager.add(manager.epics.get(id));
                        } else {
                            manager.historyManager.add(manager.subtasks.get(id));
                        }
                    }
                }
            }
        }
        return manager;
    }

    void save() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", "id", "type", "name", "status", "description", "duration",
                "startTime", "epic/subtasks"));
        sb.append("\n");
        for (Task task: tasks.values()) {
            sb.append(taskToString(task));
            sb.append("\n");
        }
        for (Epic epic: epics.values()) {
            sb.append(taskToString(epic));
            sb.append("\n");
        }
        for (Subtask subtask: subtasks.values()) {
            sb.append(taskToString(subtask));
            sb.append("\n");
        }
        sb.append("\n");
        sb.append(toString(historyManager));

        if (!Files.exists(Paths.get(path))) {
            try {
                Files.createFile(Paths.get(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (Writer fileWriter = new FileWriter(path)) {
            fileWriter.write(sb.toString());
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    private String taskToString(Task task) {
        String startTime = String.valueOf(task.getStartTime());
        String result = String.join(",", Long.toString(task.getId()), task.getType().name(), task.getTitle(),
                task.getStatus().name(), task.getDescription(), String.valueOf(task.getDuration()), startTime);
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.join(",", result, Long.toString(subtask.getEpicId()));
        } else if (task instanceof Epic) {
            StringBuilder sb = new StringBuilder();
            Epic epic = (Epic) task;
            for (Long id : epic.getSubtasksIdArray()) {
                sb.append(id);
            }
            return String.join(",", result, sb.toString());
        } else {
            return result;
        }
    }

    private Task taskFromString(String value) {
        String[] fields = value.split(",");
        Task task;
        switch (Type.valueOf(fields[1])) {
            case TASK:
                task = new Task(fields[2], fields[4], Integer.parseInt(fields[5]), LocalDateTime.parse(fields[6]));
                break;
            case EPIC:
                task = new Epic(fields[2], fields[4]);
                break;
            case SUBTASK:
                task = new Subtask(fields[2], fields[4], Integer.parseInt(fields[5]), LocalDateTime.parse(fields[6]));
                break;
            default:
                task = null;
                break;
        }
        if (task != null) {
            task.setId(Long.parseLong(fields[0]));
            task.setType(Type.valueOf(fields[1]));
            task.setStatus(Status.valueOf(fields[3]));
        }
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            subtask.setEpicId(Long.parseLong(fields[7]));
            return subtask;
        } else if (task instanceof Epic) {
            Epic epic = (Epic) task;
            epic.setDuration(Integer.parseInt(fields[5]));
            if ("null".equals(fields[6])) {
                epic.setStartTime(null);
            } else {
                epic.setStartTime(LocalDateTime.parse(fields[6]));
            }
            ArrayList<Long> subId = new ArrayList<>();
            if (fields.length > 7) {
                String[] str = fields[7].split("");
                for (String s : str) {
                    subId.add(Long.parseLong(s));
                }
                epic.setSubtasksIdArray(subId);
            }
            return epic;
        } else {
            return task;
        }
    }

    private static String toString(HistoryManager manager) {
        return manager.getHistory().stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static List<Long> fromString(String value) {
        return Stream.of(value.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    // Task's methods
    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeTaskById(long id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(long id, Task task, Status status) {
        super.updateTask(id, task, status);
        save();
    }

    @Override
    public Optional<Task> getTaskById(long id) {
        final Optional<Task> result = super.getTaskById(id);
        save();
        return result;
    }

    // Epic's methods
    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeEpicById(long id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpic(long id, Epic epic) {
        super.updateEpic(id, epic);
        save();
    }

    @Override
    public Optional<Epic> getEpicById(long id) {
        final Optional<Epic> result = super.getEpicById(id);
        save();
        return result;
    }

    // Subtask's methods
    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeSubtaskById(long id) {
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask, long epicId) {
        super.createSubtask(subtask, epicId);
        save();
    }

    @Override
    public void updateSubtask(long id, Subtask subtask, Status status) {
        super.updateSubtask(id, subtask, status);
        save();
    }

    @Override
    public Optional<Subtask> getSubtaskById(long id) {
        final Optional<Subtask> result = super.getSubtaskById(id);
        save();
        return result;
    }
}
