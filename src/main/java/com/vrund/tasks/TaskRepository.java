package com.vrund.tasks;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByIdAndOwner(Long id, String owner);
    Page<Task> findAllByOwner(String owner, Pageable page);
}
