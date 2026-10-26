package com.vrund.tasks;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "tasks", indexes = @Index(name = "idx_tasks_owner", columnList = "owner"))
class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false, length = 64, updatable = false) String owner;
    @Column(nullable = false, length = 140) String title;
    @Column(nullable = false, length = 2000) String description;
    @Column(nullable = false) boolean completed;
    @Column(nullable = false, updatable = false) Instant createdAt;
    protected Task() {}
    Task(String owner, TaskInput input) {
        this.owner = owner; this.createdAt = Instant.now(); update(input);
    }
    void update(TaskInput input) {
        title = input.title().strip();
        description = input.description() == null ? "" : input.description().strip();
        completed = input.completed();
    }
}
