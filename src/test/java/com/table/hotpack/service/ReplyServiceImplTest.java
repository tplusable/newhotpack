package com.table.hotpack.service;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Reply;
import com.table.hotpack.domain.ReplyLike;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.AddReplyRequest;
import com.table.hotpack.dto.ReplyLikeResponse;
import com.table.hotpack.dto.ReplyResponse;
import com.table.hotpack.dto.UpdateReplyRequest;
import com.table.hotpack.repository.ArticleRepository;
import com.table.hotpack.repository.ReplyLikeRepository;
import com.table.hotpack.repository.ReplyRepository;
import com.table.hotpack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReplyServiceImplTest {

    @InjectMocks
    private ReplyServiceImpl replyService;

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReplyLikeRepository replyLikeRepository;

    private Article article;
    private User user;
    private Reply reply;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        //데이터 초기화
        article = Article.builder()
                .author("test@test.com")
                .nickname("testnickname")
                .title("testtitle")
                .content("testcontent")
                .build();

        user= User.builder()
                .email("test@test.com")
                .name("testname")
                .password("testpassword")
                .nickname("testnickname")
                .roles(Set.of("ROLE_USER"))
                .build();

        reply= Reply.builder()
                .replyer(user.getNickname())
                .reply("testreply").build();
    }

    @Test
    void findReplyliesByArticleId_SholdReturnReplies() {
        //given
        PageRequest pagable=PageRequest.of(0,10);
        Page<Reply> replies = new PageImpl<>(List.of(reply));
        when(replyRepository.findByArticleIdWithPaging(reply.getReplyId(), pagable)).thenReturn(replies);
        //when
        Page<ReplyResponse> result=replyService.findRepliesByArticleId(reply.getReplyId(), pagable);
        //then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getReply()).isEqualTo("testreply");
        verify(replyRepository, times(1)).findByArticleIdWithPaging(reply.getReplyId(), pagable);

    }

    @Test
    void findReplyById_ShouldReturnReply() {
        //given
        when(replyRepository.findById(reply.getReplyId())).thenReturn(Optional.of(reply));
        //when
        ReplyResponse result=replyService.findReplyById(reply.getReplyId());
        //then
        assertThat(result.getReply()).isEqualTo("testreply");
        verify(replyRepository, times(1)).findById(reply.getReplyId());
    }

    @Test
    void findReplyById_ShouldThrowException_WhenNotFound() {
        //given
        when(replyRepository.findById(reply.getReplyId())).thenReturn(Optional.empty());
        //when & then
        assertThrows(IllegalArgumentException.class, () -> replyService.findReplyById(reply.getReplyId()));
    }

    @Test
    void addReply_ShouldSaveReply() {
        //given
        AddReplyRequest request =new AddReplyRequest(article.getId(), user.getId(), "testnewreply");
        when(articleRepository.findById(article.getId())).thenReturn(Optional.of(article));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);
        //when
        ReplyResponse result= replyService.addReply(request, user.getUsername());
        //then
        assertThat(result.getReply()).isEqualTo("testnewreply");
        verify(articleRepository, times(1)).findById(article.getId());
        verify(userRepository, times(1)).findById(user.getId());
        verify(replyRepository, times(1)).save(any(Reply.class));

    }

    @Test
    void updateReply_ShouldUpdateReply() {
        //given
        UpdateReplyRequest request=new UpdateReplyRequest("testupdatereply");
        when(replyRepository.findById(reply.getReplyId())).thenReturn(Optional.of(reply));
        //when
        ReplyResponse result = replyService.updateReply(reply.getReplyId(), request);
        //then
        assertThat(result.getReply()).isEqualTo("testupdatereply");
        verify(replyRepository, times(1)).findById(reply.getReplyId());
    }

    @Test
    void deleteReply_ShouldDeleteReply() {
        //given
        when(replyRepository.findById(reply.getReplyId())).thenReturn(Optional.of(reply));
        //when
        replyService.deleteReply(reply.getReplyId());
        //then
        verify(replyRepository, times(1)).delete(reply);
    }

    @Test
    void toggleLike_ShouldAddLike_WhenNotLiked() {
        //given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(replyRepository.findById(reply.getReplyId())).thenReturn(Optional.of(reply));
        when(replyLikeRepository.findByReplyAndUser(reply, user)).thenReturn(Optional.empty());
        when(replyLikeRepository.countByReply(reply)).thenReturn(1);
        when(replyLikeRepository.existsByReplyAndUser(reply, user)).thenReturn(true);
        //when
        ReplyLikeResponse result = replyService.toggleLike(reply.getReplyId(), user.getEmail());
        //then
        assertThat(result.getReplyId()).isEqualTo(reply.getReplyId());
        assertThat(result.getTotalLikes()).isEqualTo(1);
        assertThat(result.isLiked()).isTrue();
        verify(replyLikeRepository, times(1)).save(any(ReplyLike.class));
    }

    @Test
    void getLikers_ShouldReturnListOfUsernames() {
        //given
        ReplyLike replyLike1 = ReplyLike.builder().reply(reply).user(user).build();
        User anotherUser= User.builder().email("another@test.com").nickname("AnotherUser").build();
        ReplyLike replyLike2 = ReplyLike.builder().reply(reply).user(anotherUser).build();

        when(replyRepository.findById(reply.getReplyId())).thenReturn(Optional.of(reply));
        when(replyLikeRepository.findAllReply(reply)).thenReturn(List.of(replyLike1, replyLike2));

        //when
        List<String> likers=replyService.getLikers(reply.getReplyId());

        //then
        assertThat(likers).containsExactly(user.getNickname(), anotherUser.getNickname());
        verify(replyLikeRepository, times(1)).findAllReply(reply);
    }

}