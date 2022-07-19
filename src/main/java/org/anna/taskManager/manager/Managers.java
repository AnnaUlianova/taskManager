package org.anna.taskManager.manager;

import org.anna.taskManager.manager.historyManager.*;
import org.anna.taskManager.manager.taskManager.*;

import java.io.IOException;

public class Managers {

    private Managers() {}

    public static TaskManager getDefault() throws IOException, InterruptedException {
        return new HTTPTaskManager("8078");
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
