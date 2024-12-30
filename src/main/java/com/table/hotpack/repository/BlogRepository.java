package com.table.hotpack.repository;

import com.table.hotpack.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Article, Long> {
    List<Article> findByAuthor(String author);
}
