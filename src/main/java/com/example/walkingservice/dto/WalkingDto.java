package com.example.walkingservice.dto;

public class WalkingDto {
    private String id;
    private String courseId;
    private String trailName;
    private String courseName;
    private String courseDesc;
    private String districtName;
    private String difficulty;
    private Double courseKm;
    private Integer hour;
    private Integer minute;
    private Integer minutes;
    private String water;
    private String toilet;
    private String address;
    private Double lat;
    private Double lng;

    public WalkingDto(String id, String courseId, String trailName, String courseName,
                      String courseDesc, String districtName, String difficulty,
                      Double courseKm, Integer hour, Integer minute, Integer minutes,
                      String water, String toilet, String address, Double lat, Double lng) {
        this.id = id;
        this.courseId = courseId;
        this.trailName = trailName;
        this.courseName = courseName;
        this.courseDesc = courseDesc;
        this.districtName = districtName;
        this.difficulty = difficulty;
        this.courseKm = courseKm;
        this.hour = hour;
        this.minute = minute;
        this.minutes = minutes;
        this.water = water;
        this.toilet = toilet;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getTrailName() { return trailName; }
    public String getCourseName() { return courseName; }
    public String getCourseDesc() { return courseDesc; }
    public String getDistrictName() { return districtName; }
    public String getDifficulty() { return difficulty; }
    public Double getCourseKm() { return courseKm; }
    public Integer getHour() { return hour; }
    public Integer getMinute() { return minute; }
    public Integer getMinutes() { return minutes; }
    public String getWater() { return water; }
    public String getToilet() { return toilet; }
    public String getAddress() { return address; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
}