package com.table.hotpack.post.service;

import com.table.hotpack.post.dto.ContentDto;

public interface ContentService {
    /**
     * 특정 콘텐츠 ID에 대한 상세 정보를 조회합니다.
     *
     * @param contentId 조회할 콘텐츠의 ID
     * @return ContentDto 객체 또는 null
     */
    ContentDto getContentById(String contentId);
}
