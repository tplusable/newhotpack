package com.table.hotpack.repository;

import com.table.hotpack.domain.Reply;
import com.table.hotpack.domain.ReplyLike;
import com.table.hotpack.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long> {
    Optional<ReplyLike> findByReplyAndUser(Reply reply, User user);
    boolean existsByReplyAndUser(Reply reply, User user);
    int countByReply(Reply reply);
    @Query("SELECT rl FROM ReplyLike rl WHERE rl.reply = :reply")
    List<ReplyLike> findAllReply(Reply reply);

//    @Query("SELECT COUNT(rl) FROM ReplyLike rl WHERE rl.reply.id =:replyId")
//    int countByReplyId(@Param("replyId") Long replyId);
}
