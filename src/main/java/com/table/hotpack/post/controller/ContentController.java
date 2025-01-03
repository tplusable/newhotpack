package com.table.hotpack.post.controller;

import com.table.hotpack.post.dto.ContentDto;
import com.table.hotpack.post.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content")
public class ContentController {

    private final ContentService contentService;

    @Autowired
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 특정 콘텐츠 ID에 대한 상세 정보를 조회합니다.
     *
     * @param contentId 조회할 콘텐츠의 ID
     * @return ContentDto 객체 또는 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContentDto> getContentById(@PathVariable("id") String contentId) {
        ContentDto content = contentService.getContentById(contentId);
        if (content != null) {
            return ResponseEntity.ok(content);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
