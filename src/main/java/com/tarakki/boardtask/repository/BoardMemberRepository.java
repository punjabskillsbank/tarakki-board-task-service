package com.tarakki.boardtask.repository;

import com.tarakki.boardtask.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    boolean existsByBoardIdAndMemberId(Long boardId, UUID memberId);
}
