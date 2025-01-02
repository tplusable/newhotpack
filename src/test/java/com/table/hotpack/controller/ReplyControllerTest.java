package com.table.hotpack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.table.hotpack.dto.*;
import com.table.hotpack.service.ReplyService;
import com.table.hotpack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.reflect.Array.get;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReplyControllerTest {

    private ReplyController replyController;

    @Mock
    private ReplyService replyService;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;

    private ReplyResponse replyResponse;

    @BeforeEach
    void setUp() {
        replyService= Mockito.mock(ReplyService.class);
        replyController = new ReplyController(replyService, userService);
        objectMapper = new ObjectMapper();

        replyResponse=ReplyResponse.builder()
                .replyId(1L)
                .reply("testcontroller")
                .replyer("testUser")
                .createdAt(LocalDateTime.of(2024,12,30,15,30,0))
                .build();
    }

    @Test
    void getRepliesByArticleId_ShouldReturnReplies() {
        //given
        Page<ReplyResponse> replies = new PageImpl<>(List.of(replyResponse));
        when(replyService.findRepliesByArticleId(eq(1L), any(PageRequest.class))).thenReturn(replies);
        //when
        ResponseEntity<Page<ReplyResponse>> response=replyController.getRepliesByArticleId(1L,0,10);
        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getReply()).isEqualTo("testcontroller");
        verify(replyService, times(1)).findRepliesByArticleId(eq(1L), any(PageRequest.class));
    }

    @Test
    void addReply_ShouldCreateReply() {
        //given
        AddReplyRequest request=new AddReplyRequest(1L, 1L, "newcontrollerreplytest");
        String principalName="testUser";
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(principalName);
        when(replyService.addReply(any(AddReplyRequest.class), eq(principalName))).thenReturn(replyResponse);
        //when
        ResponseEntity<ReplyResponse> response=replyController.addReply(request, mockPrincipal);
        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getReply()).isEqualTo("testcontroller");
        verify(replyService, times(1)).addReply(any(AddReplyRequest.class), eq(principalName));
    }

    @Test
    void updateReply_ShouldUpdateReply() {
        //given
        UpdateReplyRequest request = new UpdateReplyRequest("updatecontrollerreplytest");
        when(replyService.updateReply(eq(1L), any(UpdateReplyRequest.class))).thenReturn(replyResponse);
        //when
        ResponseEntity<ReplyResponse> response=replyController.updateReply(1L, request);
        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getReply()).isEqualTo("testcontroller");
        verify(replyService, times(1)).updateReply(eq(1L), any(UpdateReplyRequest.class));
    }

    @Test
    void deleteReply_ShouldDeleteReply() {
        //given
        doNothing().when(replyService).deleteReply(eq(1L));
        //when
        ResponseEntity<Void> response=replyController.deleteReply(1L);
        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        verify(replyService, times(1)).deleteReply(eq(1L));
    }

    @Test
    void toggleLike_ShouldToggleReplyLike() {
        //given
        Long replyId=1L;
        String principalName="testUser";
        Principal mockPrincipal=mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(principalName);

        ReplyLikeResponse replyLikeResponse = ReplyLikeResponse.builder()
                .replyId(replyId)
                .totalLikes(5)
                .liked(true)
                .build();

        when(replyService.toggleLike(replyId, principalName)).thenReturn(replyLikeResponse);
        //when
        ResponseEntity<ReplyLikeResponse> response=replyController.toggleLike(replyId, mockPrincipal);
        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getReplyId()).isEqualTo(replyId);
        assertThat(response.getBody().getTotalLikes()).isEqualTo(5);
        assertThat(response.getBody().isLiked()).isTrue();
        verify(replyService, times(1)).toggleLike(replyId,principalName);
    }

    @Test
    void getLikers_ShouldReturnListOfLikers() {
        //given
        Long replyId=1L;
        List<String> likers =List.of("testUser", "AnotherUser");

        when(replyService.getLikers(replyId)).thenReturn(likers);
        //when
        ResponseEntity<List<String>> response=replyController.getLikers(replyId);
        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsExactly("testUser", "AnotherUser");
        verify(replyService,times(1)).getLikers(replyId);
    }

    @Test
    void getTopRepliesByLikes_ShouldReturnTopReplies() {
        //given
        Long articleId=1L;
        int limit=3;

        ReplyResponse reply1 =ReplyResponse.builder()
                .replyId(1L)
                .reply("Top reply 1")
                .replyer("user1")
                .totalLikes(10)
                .liked(true)
                .build();

        ReplyResponse reply2 =ReplyResponse.builder()
                .replyId(2L)
                .reply("Top reply 2")
                .replyer("user2")
                .totalLikes(8)
                .liked(false)
                .build();

        ReplyResponse reply3 =ReplyResponse.builder()
                .replyId(3L)
                .reply("Top reply 3")
                .replyer("user3")
                .totalLikes(5)
                .liked(true)
                .build();

        List<ReplyResponse> topReplies=List.of(reply1, reply2, reply3);

        when(replyService.findTopRepliesByLikes(articleId, limit)).thenReturn(topReplies);

        //when
        ResponseEntity<List<ReplyResponse>> response=replyController.getTopRepliesByLikes(articleId, limit);

        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);

        ReplyResponse firstReply=response.getBody().get(0);
        assertThat(firstReply.getReply()).isEqualTo("Top reply 1");
        assertThat(firstReply.getTotalLikes()).isEqualTo(10);
        assertThat(firstReply.isLiked()).isTrue();

        ReplyResponse secondReply = response.getBody().get(1);
        assertThat(secondReply.getReply()).isEqualTo("Top reply 2");
        assertThat(secondReply.getTotalLikes()).isEqualTo(8);
        assertThat(secondReply.isLiked()).isFalse();

        ReplyResponse thirdReply=response.getBody().get(2);
        assertThat(thirdReply.getReply()).isEqualTo("Top reply 3");
        assertThat(thirdReply.getTotalLikes()).isEqualTo(5);
        assertThat(thirdReply.isLiked()).isTrue();

        verify(replyService, times(1)).findTopRepliesByLikes(articleId, limit);
    }

}