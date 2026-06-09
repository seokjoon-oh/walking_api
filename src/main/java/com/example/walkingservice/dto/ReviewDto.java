package com.example.walkingservice.dto;

public class ReviewDto {
    private String walkId;
    private String userId;
    private Integer rating;
    private String content;

    public String getWalkId() {
        return walkId;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }
}