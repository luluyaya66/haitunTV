package com.tv.mydiy.epg;

public class EpgProgram {
    private String title;
    private String startTime;
    private String endTime;
    private String description;
    private String channelId; // 添加频道ID字段
    
    public EpgProgram() {
        // 默认构造函数
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public void setDescription(String description) { this.description = description; }
    
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
}