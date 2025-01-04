package com.table.hotpack.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDto {
    private String title;
    private String tel;
    private String addr1;
    private String firstimage;
    private String mapx;
    private String mapy;
    private String contentid;
    private String homepage;
    private String overview;
}
