package com.tarakki.boardtask.repository;

import com.tarakki.common.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Modifying
    @Query("DELETE FROM Board b WHERE b.boardId = :boardId")
    int deleteBoardById(@Param("boardId") Long boardId);
}


