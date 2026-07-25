package com.tarakki.boardtask.repository;

import com.tarakki.boardtask.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByBoardId(Long boardId);

    Optional<Task> findByTaskIdAndBoardId(Long taskId, Long boardId);
}
