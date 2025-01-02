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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.as;
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

        //Mock Repository 동작 정의
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    // 초기화 작업 (모든 테스트 메서드 전에 실행)
    @BeforeEach
    void setUpSecurityContext() {
        // Mock Authentication 객체 생성
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext=Mockito.mock(SecurityContext.class);

        //Mock SecurityContext 동작 정의
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail()); // 현재 사용자의 이메일 반환(테스트에서 사용할 사용자 이메일)

        // SecurityContextHolder에 설정
        SecurityContextHolder.setContext(securityContext);
    }

    // 테스트 완료 후 정리 작업(모든 테스트 메서드 후에 실행)
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // SecurityContext 초기화
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

    @Test
    void findTopRepliesByLikes_ShouldReturnTopReplies() {
        //given
        int limit=3;
        Reply reply1=Reply.builder()
                .replyer("user1")
                .reply("Top reply 1")
                .build();
        Reply reply2=Reply.builder()
                .replyer("user2")
                .reply("Top reply 2")
                .build();
        Reply reply3=Reply.builder()
                .replyer("user3")
                .reply("Top reply 3")
                .build();

        when(replyRepository.findTopRepliesByLikes(article.getId(), PageRequest.of(0, limit)))
                .thenReturn(List.of(reply1, reply2, reply3));

        when(replyLikeRepository.countByReply(reply1)).thenReturn(10);
        when(replyLikeRepository.countByReply(reply2)).thenReturn(8);
        when(replyLikeRepository.countByReply(reply3)).thenReturn(5);

        when(replyLikeRepository.existsByReplyAndUser(reply1,user)).thenReturn(true);
        when(replyLikeRepository.existsByReplyAndUser(reply2, user)).thenReturn(false);
        when(replyLikeRepository.existsByReplyAndUser(reply3, user)).thenReturn(true);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        //when
        List<ReplyResponse> topReplies=replyService.findTopRepliesByLikes(article.getId(), limit);

        //then
        assertThat(topReplies).hasSize(3);

        ReplyResponse firstReply = topReplies.get(0);
        assertThat(firstReply.getReply()).isEqualTo("Top reply 1");
        assertThat(firstReply.getTotalLikes()).isEqualTo(10);
        assertThat(firstReply.isLiked()).isTrue();

        ReplyResponse secondReply = topReplies.get(1);
        assertThat(secondReply.getReply()).isEqualTo("Top reply 2");
        assertThat(secondReply.getTotalLikes()).isEqualTo(8);
        assertThat(secondReply.isLiked()).isFalse();

        ReplyResponse thirdReply = topReplies.get(2);
        assertThat(thirdReply.getReply()).isEqualTo("Top reply 3");
        assertThat(thirdReply.getTotalLikes()).isEqualTo(5);
        assertThat(thirdReply.isLiked()).isTrue();

        verify(replyRepository, times(1)).findTopRepliesByLikes(article.getId(), PageRequest.of(0, limit));
        verify(replyLikeRepository, times(1)).countByReply(reply1);
        verify(replyLikeRepository, times(1)).countByReply(reply2);
        verify(replyLikeRepository, times(1)).countByReply(reply3);
        verify(replyLikeRepository, times(1)).existsByReplyAndUser(reply1, user);
        verify(replyLikeRepository, times(1)).existsByReplyAndUser(reply2, user);
        verify(replyLikeRepository, times(1)).existsByReplyAndUser(reply3, user);
    }

}