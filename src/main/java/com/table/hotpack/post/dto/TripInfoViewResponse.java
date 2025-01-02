package com.table.hotpack.post.dto;

import lombok.Getter;

import java.util.List;
import java.util.Map;


@Getter
public class TripInfoViewResponse {

    private String areaName;
    private String startDate;
    private String endDate;
    private Map<String, List<String>> contentIdsByDate;}
