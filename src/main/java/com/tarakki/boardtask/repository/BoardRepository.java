package com.tarakki.boardtask.repository;

import com.tarakki.common.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByOrgId(Long orgId);
}


