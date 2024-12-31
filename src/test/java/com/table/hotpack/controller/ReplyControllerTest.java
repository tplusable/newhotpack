package com.table.hotpack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.table.hotpack.dto.AddReplyRequest;
import com.table.hotpack.dto.ReplyResponse;
import com.table.hotpack.dto.UpdateReplyRequest;
import com.table.hotpack.service.ReplyService;
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
    private ReplyService replyService;
    private ObjectMapper objectMapper;

    private ReplyResponse replyResponse;

    @BeforeEach
    void setUp() {
        replyService= Mockito.mock(ReplyService.class);
        replyController = new ReplyController(replyService);
        objectMapper = new ObjectMapper();

        replyResponse=ReplyResponse.builder()
                .replyId(1L)
                .reply("testcontroller")
                .replyer("testUser")
                .createAt(LocalDateTime.of(2024,12,30,15,30,0))
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
        when(replyService.addReply(any(AddReplyRequest.class))).thenReturn(replyResponse);
        //when
        ResponseEntity<ReplyResponse> response=replyController.addReply(request);
        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getReply()).isEqualTo("testcontroller");
        verify(replyService, times(1)).addReply(any(AddReplyRequest.class));
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

}