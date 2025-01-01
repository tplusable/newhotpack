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

}