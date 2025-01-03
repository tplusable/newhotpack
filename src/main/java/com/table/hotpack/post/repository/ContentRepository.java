package com.table.hotpack.post.repository;

import com.table.hotpack.post.dto.ContentDto;

public interface ContentRepository {
    ContentDto findContentById(String contentId);
}
