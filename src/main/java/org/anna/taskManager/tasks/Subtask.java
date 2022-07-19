package org.anna.taskManager.tasks;

import java.time.LocalDateTime;

public class Subtask extends Task {

    protected long epicId;

    public Subtask(String title, String description, int duration, LocalDateTime startTime) {
        super(title, description, duration, startTime);
        this.type = Type.SUBTASK;
    }

    public Subtask(String title, String description) {
        super(title, description);
        this.type = Type.SUBTASK;
    }

    public long getEpicId() {
        return epicId;
    }

    public void setEpicId(long epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
