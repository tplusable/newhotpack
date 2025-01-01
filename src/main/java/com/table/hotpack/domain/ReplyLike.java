package com.table.hotpack.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EnableJpaAuditing
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"reply_id", "user_id"}))
@Builder
public class ReplyLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long replyLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="reply_id",nullable=false)
    private Reply reply; // 추천받은 댓글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user; // 추천한 사용자(중복방지)
}
