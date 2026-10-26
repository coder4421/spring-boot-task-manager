package com.vrund.tasks;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/tasks")
class TaskController {
    private final TaskService tasks;
    TaskController(TaskService tasks) { this.tasks = tasks; }
    @GetMapping TaskService.TaskPage list(Principal user,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return tasks.list(user.getName(), page, size);
    }
    @GetMapping("/{id}") TaskView get(Principal user, @PathVariable long id) { return tasks.get(user.getName(), id); }
    @PostMapping ResponseEntity<TaskView> create(Principal user, @Valid @RequestBody TaskInput input) {
        var task = tasks.create(user.getName(), input);
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
    }
    @PutMapping("/{id}") TaskView update(Principal user, @PathVariable long id, @Valid @RequestBody TaskInput input) {
        return tasks.update(user.getName(), id, input);
    }
    @DeleteMapping("/{id}") ResponseEntity<Void> delete(Principal user, @PathVariable long id) {
        tasks.delete(user.getName(), id); return ResponseEntity.noContent().build();
    }
}
