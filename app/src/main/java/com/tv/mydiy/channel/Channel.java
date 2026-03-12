package com.tv.mydiy.channel;

import com.tv.mydiy.epg.EpgProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import androidx.annotation.NonNull;

public class Channel {
    private String name;
    private String tvgId;
    private String tvgName;
    private String tvgLogo;
    private String groupTitle;
    private final List<Source> sources;
    private int currentSourceIndex = 0; // 默认为第一个源
    private boolean isFavorite = false;
    private EpgProgram currentProgram; // 添加当前节目信息
    
    public Channel(String name) {
        this.name = name;
        this.sources = new ArrayList<>();
    }
    
    // 频道源定义
    public static class Source {
        private final String url;
        private String name;
        
        public Source(String url, String name) {
            this.url = url;
            this.name = name;
        }
        
        public String getUrl() { return url; }

        public String getName() { 
            // 如果名称为空，返回默认名称"线路X"
            if (name == null || name.isEmpty()) {
                return "线路"; // 不再包含索引，因为索引由外部管理
            }
            return name; 
        }
        
        public void setName(String name) { this.name = name; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Source source = (Source) o;
            return Objects.equals(url, source.url) &&
                   Objects.equals(name, source.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(url, name);
        }
        
        @Override
        @NonNull
        public String toString() {
            return "Source{" +
                   "url='" + url + '\'' +
                   ", name='" + name + '\'' +
                   '}';
        }
    }
    
    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getTvgId() { return tvgId; }
    public void setTvgId(String tvgId) { this.tvgId = tvgId; }
    
    public String getTvgName() { return tvgName; }
    public void setTvgName(String tvgName) { this.tvgName = tvgName; }
    
    public String getTvgLogo() { return tvgLogo; }
    public void setTvgLogo(String tvgLogo) { this.tvgLogo = tvgLogo; }
    
    public String getGroupTitle() { return groupTitle; }
    public void setGroupTitle(String groupTitle) { this.groupTitle = groupTitle; }
    
    public List<Source> getSources() { return sources; }

    public void addSource(Source source) { 
        if (source != null) {
            this.sources.add(source); 
        }
    }
    
    public int getCurrentSourceIndex() { return currentSourceIndex; }
    public void setCurrentSourceIndex(int index) { 
        if (index >= 0 && index < sources.size()) {
            this.currentSourceIndex = index; 
        }
    }
    
    public String getCurrentSourceUrl() {
        if (sources.isEmpty()) return "";
        return sources.get(currentSourceIndex).getUrl();
    }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    // EPG相关方法
    public EpgProgram getCurrentProgram() { return currentProgram; }
    public void setCurrentProgram(EpgProgram program) { this.currentProgram = program; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return currentSourceIndex == channel.currentSourceIndex &&
               isFavorite == channel.isFavorite &&
               Objects.equals(name, channel.name) &&
               Objects.equals(tvgId, channel.tvgId) &&
               Objects.equals(tvgName, channel.tvgName) &&
               Objects.equals(tvgLogo, channel.tvgLogo) &&
               Objects.equals(groupTitle, channel.groupTitle) &&
               Objects.equals(sources, channel.sources);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, tvgId, tvgName, tvgLogo, groupTitle, sources, currentSourceIndex, isFavorite);
    }
    
    @Override
    @NonNull
    public String toString() {
        return "Channel{" +
               "name='" + name + '\'' +
               ", tvgId='" + tvgId + '\'' +
               ", tvgName='" + tvgName + '\'' +
               ", tvgLogo='" + tvgLogo + '\'' +
               ", groupTitle='" + groupTitle + '\'' +
               ", sources=" + sources.size() +
               ", currentSourceIndex=" + currentSourceIndex +
               ", isFavorite=" + isFavorite +
               '}';
    }

    // 添加获取源数量的方法
    public int getSourceCount() {
        return sources.size();
    }

}