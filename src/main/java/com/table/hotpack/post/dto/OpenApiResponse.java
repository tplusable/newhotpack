package com.table.hotpack.post.dto;

import java.util.List;

public class OpenApiResponse {
    private List<Item> item;

    // getter, setter
    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item;
    }

    public static class Item {
        private String contentid;
        private String contenttypeid;
        private String title;
        private String homepage;
        private String mapx;
        private String mapy;

        public Item(String contentid, String contenttypeid) {
            this.contentid = contentid;
            this.contenttypeid = contenttypeid;
        }

        // Getter and Setter methods
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }

        public String getMapx() {
            return mapx;
        }

        public void setMapx(String mapx) {
            this.mapx = mapx;
        }

        public String getMapy() {
            return mapy;
        }

        public void setMapy(String mapy) {
            this.mapy = mapy;
        }
    }
}
