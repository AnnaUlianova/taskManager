package org.anna.taskManager.manager.historyManager;

import org.anna.taskManager.tasks.Task;
import java.util.ArrayList;

public interface HistoryManager {

    ArrayList<Task> getHistory();

    void add(Task task);

    void remove(long id);
}