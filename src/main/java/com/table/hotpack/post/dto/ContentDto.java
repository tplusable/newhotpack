package com.table.hotpack.post.dto;

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

    // 기본 생성자
    public ContentDto() {}

    // 모든 필드를 초기화하는 생성자
    public ContentDto(String title, String tel, String addr1, String firstimage, String mapx, String mapy, String contentid, String homepage, String overview) {
        this.title = title;
        this.tel = tel;
        this.addr1 = addr1;
        this.firstimage = firstimage;
        this.mapx = mapx;
        this.mapy = mapy;
        this.contentid = contentid;
        this.homepage = homepage;
        this.overview = overview;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getAddr1() { return addr1; }
    public void setAddr1(String addr1) { this.addr1 = addr1; }

    public String getFirstimage() { return firstimage; }
    public void setFirstimage(String firstimage) { this.firstimage = firstimage; }

    public String getMapx() { return mapx; }
    public void setMapx(String mapx) { this.mapx = mapx; }

    public String getMapy() { return mapy; }
    public void setMapy(String mapy) { this.mapy = mapy; }

    public String getContentid() { return contentid; }
    public void setContentid(String contentid) { this.contentid = contentid; }

    public String getHomepage() { return homepage; }
    public void setHomepage(String homepage) { this.homepage = homepage; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
}
