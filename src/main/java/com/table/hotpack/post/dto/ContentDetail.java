package com.table.hotpack.post.dto;

public class ContentDetail {
    private String title;
    private String overview;
    private String homepage;
    private String address;

    // 생성자, getter, setter 등
    public ContentDetail(String title, String overview, String homepage, String address) {
        this.title = title;
        this.overview = overview;
        this.homepage = homepage;
        this.address = address;
    }
}
