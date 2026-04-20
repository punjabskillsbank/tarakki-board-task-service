package com.tarakki.boardtask.repository;

import com.tarakki.common.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

}


