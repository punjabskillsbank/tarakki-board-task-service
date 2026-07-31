package com.tarakki.boardtask.repository;

import com.tarakki.boardtask.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByOrgId(Long orgId);

}


