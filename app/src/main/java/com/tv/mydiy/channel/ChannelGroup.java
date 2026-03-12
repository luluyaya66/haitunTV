package com.tv.mydiy.channel;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChannelGroup {
    private String name;
    private final List<Channel> channels;
    private boolean isExpanded = true; // 默认展开
    
    public ChannelGroup(String name) {
        this.name = name;
        this.channels = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Channel> getChannels() {
        return channels;
    }
    
    public void addChannel(Channel channel) {
        if (channel != null) {
            this.channels.add(channel);
        }
    }
    
    public int getChannelCount() {
        return channels.size();
    }
    
    public Channel getChannel(int index) {
        if (index >= 0 && index < channels.size()) {
            return channels.get(index);
        }
        return null;
    }
    
    public boolean isExpanded() {
        return isExpanded;
    }
    
    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
    
    // 添加containsChannel方法
    public boolean containsChannel(Channel channel) {
        if (channel != null) {
            return channels.contains(channel);
        }
        return false;
    }
    
    // 添加getChannelIndex方法
    public int getChannelIndex(Channel channel) {
        if (channel != null) {
            return channels.indexOf(channel);
        }
        return -1;
    }
    
    // 添加removeChannel方法
    public void removeChannel(Channel channel) {
        if (channel != null) {
            channels.remove(channel);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelGroup that = (ChannelGroup) o;
        return isExpanded == that.isExpanded &&
               Objects.equals(name, that.name) &&
               Objects.equals(channels, that.channels);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, channels, isExpanded);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "ChannelGroup{" +
               "name='" + name + '\'' +
               ", channels=" + channels.size() +
               ", isExpanded=" + isExpanded +
               '}';
    }
}