package com.vrund.tasks;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
@Service
@Transactional
class TaskService {
    private final TaskRepository tasks;
    TaskService(TaskRepository tasks) { this.tasks = tasks; }
    record TaskPage(List<TaskView> items, long total, int page, int pages) {}
    @Transactional(readOnly = true)
    TaskPage list(String owner, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 0 and size between 1 and 100");
        var result = tasks.findAllByOwner(owner, PageRequest.of(page, size, Sort.by("id").descending()));
        return new TaskPage(result.map(TaskView::from).getContent(), result.getTotalElements(), page, result.getTotalPages());
    }
    @Transactional(readOnly = true)
    TaskView get(String owner, long id) { return TaskView.from(owned(owner, id)); }
    TaskView create(String owner, TaskInput input) { return TaskView.from(tasks.save(new Task(owner, input))); }
    TaskView update(String owner, long id, TaskInput input) {
        var task = owned(owner, id); task.update(input); return TaskView.from(task);
    }
    void delete(String owner, long id) { tasks.delete(owned(owner, id)); }
    private Task owned(String owner, long id) {
        return tasks.findByIdAndOwner(id, owner).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
