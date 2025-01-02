package com.table.hotpack.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="reply")
@EntityListeners(AuditingEntityListener.class)
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reply_id", updatable = false)
    private Long replyId;

    @Column(name = "replyer", nullable = false)
    private String replyer;

    @Column(name="reply", nullable = false)
    private String reply;

    @CreatedDate
    @Column
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // email이 저장되는 필드
    @Column(name = "author", nullable = false)
    private String author;

    @OneToMany(mappedBy = "reply", cascade =CascadeType.ALL, orphanRemoval = true)
    private List<ReplyLike> replyLikes =new ArrayList<>();

    public void update(String reply) {
        this.reply= reply;
    }

    public int getLikeCount() {
        return replyLikes.size();
    }
}
