package com.vrund.tasks;
import java.time.Instant;
record TaskView(Long id, String title, String description, boolean completed, Instant createdAt) {
    static TaskView from(Task task) {
        return new TaskView(task.id, task.title, task.description, task.completed, task.createdAt);
    }
}
