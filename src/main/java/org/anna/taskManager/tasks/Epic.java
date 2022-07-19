package org.anna.taskManager.tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Long> subtasksIdArray;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        subtasksIdArray = new ArrayList<>();
        this.type = Type.EPIC;
    }

    public ArrayList<Long> getSubtasksIdArray() {
        return subtasksIdArray;
    }

    public void setSubtasksIdArray(ArrayList<Long> subtasksIdArray) {
        this.subtasksIdArray = subtasksIdArray;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime
                + '}';
    }
}
