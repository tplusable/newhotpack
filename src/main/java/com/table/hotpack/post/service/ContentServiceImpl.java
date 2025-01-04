package com.table.hotpack.post.service;

import com.table.hotpack.post.dto.ContentDto;
import com.table.hotpack.post.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;

    @Autowired
    public ContentServiceImpl(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public ContentDto getContentById(String contentId) {
        return contentRepository.findContentById(contentId);
    }
}
