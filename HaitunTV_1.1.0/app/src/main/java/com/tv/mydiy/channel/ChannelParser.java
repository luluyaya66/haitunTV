package com.tv.mydiy.channel;

import android.util.Log;

import com.tv.mydiy.util.LruCacheUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.nio.charset.Charset;

public class ChannelParser {
    private static final String TAG = "ChannelParser";
    private static final LruCacheUtil cacheUtil = LruCacheUtil.getInstance(); // LRU缓存工具
    
    // M3U文件标识
    private static final String M3U_HEADER = "#EXTM3U";
    
    // 频道信息正则表达式
    private static final Pattern CHANNEL_INFO_PATTERN = Pattern.compile("#EXTINF:-1\\s*(?:tvg-id=\"([^\"]*)\")?\\s*(?:tvg-name=\"([^\"]*)\")?\\s*(?:tvg-logo=\"([^\"]*)\")?\\s*(?:group-title=\"([^\"]*)\")?,(.*)");
    
    // 分类标识正则表达式
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("(.+?),\\s*#genre#");
    
    /**
     * 从URL解析频道列表
     * @param url M3U文件URL
     * @return 频道组列表
     * @throws Exception 解析异常
     */
    public static List<ChannelGroup> parseFromUrl(String url) throws Exception {
        // 先尝试从缓存中获取（设置缓存时间为30分钟）
        @SuppressWarnings("unchecked")
        List<ChannelGroup> cachedResult = cacheUtil.get("channel_" + url, List.class, 30 * 60 * 1000);
        if (cachedResult != null) {
            Log.d(TAG, "从缓存中获取频道列表: " + url);
            return cachedResult;
        }

        URL m3uUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) m3uUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        // 添加User-Agent以避免被阻止
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android TV)");
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP Error: " + responseCode + " for URL: " + url);
        }
        
        InputStream inputStream = connection.getInputStream();
        
        // 检查是否是gzip压缩格式
        String contentEncoding = connection.getHeaderField("Content-Encoding");
        boolean isGzip = "gzip".equalsIgnoreCase(contentEncoding) || url.toLowerCase().endsWith(".gz");
        
        // 即使URL以.gz结尾，也要检查实际内容是否为GZIP格式
        if (isGzip) {
            try {
                // 尝试读取GZIP头部以确认是否真的是GZIP格式
                PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 2);
                byte[] header = new byte[2];
                int bytesRead = pushbackInputStream.read(header);
                if (bytesRead == 2) {
                    pushbackInputStream.unread(header, 0, bytesRead);
                    // GZIP文件头部为0x1f8b
                    if (header[0] == (byte) 0x1f && header[1] == (byte) 0x8b) {
                        inputStream = new GZIPInputStream(pushbackInputStream);
                    } else {
                        // 不是真正的GZIP文件，直接使用原始流
                        inputStream = pushbackInputStream;
                        Log.w(TAG, "File extension suggests GZIP but content is not GZIP format: " + url);
                    }
                } else {
                    inputStream = pushbackInputStream;
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to detect GZIP format, using raw stream: " + e.getMessage());
                // 如果检测失败，回退到原始流
                inputStream = connection.getInputStream();
            }
        }
        
        // 递归处理可能的嵌套GZ压缩
        inputStream = handleNestedGzip(inputStream, url);
        
        // 尝试检测字符编码并转换为字符串
        String content = detectAndConvertToString(inputStream);
        inputStream.close();
        connection.disconnect();
        
        // 检查内容是否有效
        if (content.isEmpty()) {
            Log.e(TAG, "Content is null or empty after decompression and decoding");
            throw new IOException("Content is null or empty after decompression and decoding");
        }
        
        // 使用原始内容进行解析
        List<ChannelGroup> result = parseContent(content);
        Log.d(TAG, "Successfully parsed " + result.size() + " channel groups from " + url);
        
        // 将结果缓存到LRU缓存中
        cacheUtil.put("channel_" + url, result);
        
        return result;
    }
    
    /**
     * 处理可能的嵌套GZ压缩
     * @param inputStream 输入流
     * @param url URL地址（用于日志）
     * @return 处理后的输入流
     */
    private static InputStream handleNestedGzip(InputStream inputStream, String url) {
        try {
            // 检查是否是嵌套的GZIP文件
            PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 2);
            byte[] header = new byte[2];
            int bytesRead = pushbackInputStream.read(header);
            if (bytesRead == 2) {
                pushbackInputStream.unread(header, 0, bytesRead);
                // GZIP文件头部为0x1f8b
                if (header[0] == (byte) 0x1f && header[1] == (byte) 0x8b) {
                    // 是GZIP格式，解压后再检查是否还是GZIP格式（嵌套压缩）
                    GZIPInputStream gzipInputStream = new GZIPInputStream(pushbackInputStream);
                    return handleNestedGzip(gzipInputStream, url + " (nested)");
                }
            }
            return pushbackInputStream;
        } catch (Exception e) {
            Log.w(TAG, "Error handling nested GZIP, using original stream: " + e.getMessage());
            return inputStream;
        }
    }

    /**
     * 检测输入流的字符编码并将其转换为字符串
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException IO异常
     */
    private static String detectAndConvertToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        // 获取字节数组
        byte[] bytes = result.toByteArray();

        // 尝试检测字符编码
        String charset = detectCharset(bytes);
        
        // 使用检测到的字符集解码
        return new String(bytes, Charset.forName(charset));
    }

    /**
     * 简单的字符编码检测方法
     * @param bytes 字节数组
     * @return 推测的字符集名称
     */
    private static String detectCharset(byte[] bytes) {
        // 检查BOM标记
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return "UTF-8";
        } else if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
            return "UTF-16LE";
        } else if (bytes.length >= 2 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
            return "UTF-16BE";
        }
        
        // 如果没有BOM，则假设为UTF-8
        return "UTF-8";
    }

    /**
     * 解析M3U内容
     * @param content M3U内容
     * @return 频道组列表
     */
    public static List<ChannelGroup> parseContent(String content) {
        if (content == null || content.isEmpty()) {
            Log.e(TAG, "Content is null or empty");
            return new ArrayList<>();
        }
        
        // 检查内容是否包含明显的非文本字符（控制字符等）
        if (containsBinaryData(content)) {
            Log.e(TAG, "Content appears to be binary data, not text. Skipping parsing.");
            return new ArrayList<>();
        }

        String[] lines = content.split("\n");
        
        // 检查是否是M3U格式
        boolean isM3UFormat = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#EXTM3U")) {
                isM3UFormat = true;
                break;
            }
        }
        
        if (isM3UFormat) {
            // 解析标准M3U格式
            return parseM3UFormat(lines);
        } else {
            // 解析简单TXT格式（每行一个URL）
            return parseTxtFormat(lines);
        }
    }
    
    /**
     * 检查字符串是否包含二进制数据
     * @param content 内容
     * @return 如果包含二进制数据返回true，否则返回false
     */
    private static boolean containsBinaryData(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        // 检查前1000个字符中是否包含过多的控制字符
        int controlCharCount = 0;
        int checkLength = Math.min(content.length(), 1000);
        
        for (int i = 0; i < checkLength; i++) {
            char c = content.charAt(i);
            // 检查是否为控制字符（除了常见的换行符、制表符等）
            if (c < 32 && c != '\n' && c != '\r' && c != '\t') {
                controlCharCount++;
            }
        }
        
        // 如果控制字符占比超过10%，则认为是二进制数据
        return (controlCharCount * 100.0 / checkLength) > 10;
    }

    /**
     * 解析标准M3U格式
     * @param lines 内容行数组
     * @return 频道组列表
     */
    private static List<ChannelGroup> parseM3UFormat(String[] lines) {
        List<ChannelGroup> groups = new ArrayList<>();
        ChannelGroup currentGroup = null;
        Map<String, ChannelGroup> groupMap = new HashMap<>();
        Channel currentChannel = null; // 添加当前频道引用，用于处理多线路
        Map<String, Channel> channelMap = new HashMap<>(); // 用于查找相同名称的频道

        for (String s : lines) {
            String line = s.trim();
            if (line.startsWith(M3U_HEADER)) {
                // M3U文件头 - 跳过处理
                continue;
            } else if (line.startsWith("#EXTINF:")) {
                // 频道信息行
                Matcher matcher = CHANNEL_INFO_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String tvgId = matcher.group(1);
                    String tvgName = matcher.group(2);
                    String tvgLogo = matcher.group(3);
                    String groupTitle = matcher.group(4);
                    String channelName = getChannelName(matcher.group(5), tvgName, tvgId);

                    // 查找是否已经有同名频道
                    String channelKey = (groupTitle != null ? groupTitle : "") + "_" + channelName;
                    currentChannel = channelMap.get(channelKey);

                    if (currentChannel == null) {
                        // 创建新频道
                        currentChannel = new Channel(channelName);
                        currentChannel.setTvgId(tvgId);
                        // 如果tvgName为空，将频道名称赋值给tvgName
                        if (tvgName == null || tvgName.isEmpty()) {
                            currentChannel.setTvgName(channelName);
                        } else {
                            currentChannel.setTvgName(tvgName);
                        }
                        currentChannel.setTvgLogo(tvgLogo);
                        currentChannel.setGroupTitle(groupTitle);

                        // 获取或创建分组
                        if (groupTitle == null || groupTitle.isEmpty()) {
                            groupTitle = "未分组";
                        }

                        currentGroup = groupMap.get(groupTitle);
                        if (currentGroup == null) {
                            currentGroup = new ChannelGroup(groupTitle);
                            groupMap.put(groupTitle, currentGroup);
                            groups.add(currentGroup);
                        }

                        currentGroup.addChannel(currentChannel);
                        channelMap.put(channelKey, currentChannel);
                    }
                }
            } else if (line.endsWith(",#genre#") || line.endsWith(", #genre#")) {
                // 处理分类标识行（如"央视频道,#genre#"）
                Matcher categoryMatcher = CATEGORY_PATTERN.matcher(line);
                if (categoryMatcher.matches()) {
                    String categoryName = categoryMatcher.group(1);
                    if (categoryName != null) {
                        categoryName = categoryName.trim();
                    }
                    currentGroup = new ChannelGroup(categoryName);
                    groups.add(currentGroup);
                    channelMap.clear(); // 切换分组时清空频道映射
                }
            } else if (line.startsWith("#") || line.isEmpty()) {
                // 注释行或空行，跳过
                continue;
            } else if (line.startsWith("http")) {
                // 频道URL行
                if (currentChannel != null) {
                    // 为当前频道添加源URL
                    Channel.Source source = new Channel.Source(line, "线路" + (currentChannel.getSources().size() + 1));
                    currentChannel.addSource(source);
                } else if (currentGroup != null && !currentGroup.getChannels().isEmpty()) {
                    // 如果没有当前频道（例如直接跟在分组行后面），则为最后一个频道添加源URL
                    Channel lastChannel = currentGroup.getChannels().get(currentGroup.getChannels().size() - 1);
                    // 添加源URL
                    Channel.Source source = new Channel.Source(line, "线路" + (lastChannel.getSources().size() + 1));
                    lastChannel.addSource(source);
                }
            }
        }
        
        // 输出调试信息
        Log.d(TAG, "Parsed " + groups.size() + " groups");
        for (ChannelGroup group : groups) {
            Log.d(TAG, "Group: " + group.getName() + ", Channels: " + group.getChannels().size());
            for (Channel channel : group.getChannels()) {
                Log.d(TAG, "  Channel: " + channel.getName() + ", Sources: " + channel.getSources().size());
            }
        }
        
        return groups;
    }
    
    /**
     * 从匹配器结果中获取频道名称
     * @param channelName 来自匹配器的频道名称
     * @param tvgName tvg-name属性值
     * @param tvgId tvg-id属性值
     * @return 最终的频道名称
     */
    private static String getChannelName(String channelName, String tvgName, String tvgId) {
        // 如果频道名称为空，使用tvg-name或tvg-id
        if ((channelName == null || channelName.isEmpty()) && tvgName != null && !tvgName.isEmpty()) {
            channelName = tvgName;
        }
        if ((channelName == null || channelName.isEmpty()) && tvgId != null && !tvgId.isEmpty()) {
            channelName = tvgId;
        }
        if (channelName == null || channelName.isEmpty()) {
            channelName = "未知频道";
        }
        return channelName;
    }
    
    /**
     * 解析简单TXT格式（每行一个URL）
     * @param lines 内容行数组
     * @return 频道组列表
     */
    private static List<ChannelGroup> parseTxtFormat(String[] lines) {
        List<ChannelGroup> groups = new ArrayList<>();
        ChannelGroup group = new ChannelGroup("我的收藏");
        groups.add(group);
        int channelNumber = 1;
        ChannelGroup currentGroup = group; // 添加当前分组引用
        Map<String, Channel> channelMap = new HashMap<>(); // 用于查找相同名称的频道
        
        for (String line : lines) {
            line = line.trim();
            // 跳过空行和注释行
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // 检查是否是分类标识行（如"央视频道,#genre#"）
            if (line.endsWith(",#genre#") || line.endsWith(", #genre#")) {
                Matcher categoryMatcher = CATEGORY_PATTERN.matcher(line);
                if (categoryMatcher.matches()) {
                    String categoryName = Objects.requireNonNull(categoryMatcher.group(1)).trim();
                    currentGroup = new ChannelGroup(categoryName);
                    groups.add(currentGroup);
                    channelMap.clear(); // 切换分组时清空频道映射
                }
                continue;
            }
            
            // 检查是否包含逗号分隔的频道名和URL
            if (line.contains(",")) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String channelName = parts[0].trim();
                    String channelUrl = parts[1].trim();
                    
                    // 如果URL为空则跳过
                    if (channelUrl.isEmpty()) {
                        continue;
                    }
                    
                    // 构建频道键值
                    String channelKey = currentGroup.getName() + "_" + channelName;
                    Channel channel = channelMap.get(channelKey);
                    
                    if (channel == null) {
                        // 创建新频道
                        channel = new Channel(channelName);
                        // 如果tvgName为空，将频道名称赋值给tvgName
                        if (!channelName.isEmpty()) {
                            channel.setTvgName(channelName);
                        }
                        currentGroup.addChannel(channel);
                        channelMap.put(channelKey, channel);
                    }
                    
                    // 为频道添加源
                    Channel.Source source = new Channel.Source(channelUrl, "线路" + (channel.getSources().size() + 1));
                    channel.addSource(source);
                    continue;
                }
            }
            
            // 处理单独的URL行
            if (line.startsWith("http")) {
                // 为每个URL创建一个新频道
                Channel channel = new Channel("频道 " + channelNumber);
                // 如果tvgName为空，将频道名称赋值给tvgName
                channel.setTvgName("频道 " + channelNumber);
                channelNumber++;
                
                // 添加源URL
                Channel.Source source = new Channel.Source(line, "源1");
                channel.addSource(source);
                
                currentGroup.addChannel(channel);
                String channelKey = currentGroup.getName() + "_" + channel.getName();
                channelMap.put(channelKey, channel);
            }
        }
        
        return groups;
    }
    
    /**
     * 从主地址解析频道列表，支持多个备用地址
     * @param mainUrl 主地址，包含多个备用频道清单地址
     * @return 频道组列表
     * @throws Exception 解析异常
     */
    public static List<ChannelGroup> parseFromMainUrlWithFallback(String mainUrl) throws Exception {
        // 检查mainUrl是否为null
        if (mainUrl == null) {
            Log.e(TAG, "Main URL is null in parseFromMainUrlWithFallback");
            throw new IllegalArgumentException("Main URL cannot be null");
        }
        
        // 首先从主地址获取内容
        String content = fetchContent(mainUrl);
        
        // 从内容中提取备用地址列表
        List<String> playlistUrls = extractPlaylistUrls(content, mainUrl);
        
        if (playlistUrls.isEmpty()) {
            Log.w(TAG, "未能提取到备用地址列表");
            throw new Exception("无法获取频道清单地址列表");
        }
        
        // 尝试按顺序从每个地址加载频道列表
        Exception lastException = null;
        for (String url : playlistUrls) {
            try {
                List<ChannelGroup> groups = parseFromUrl(url);
                if (!groups.isEmpty()) {
                    return groups;
                }
            } catch (Exception e) {
                lastException = e;
                // 继续尝试下一个地址
                Log.w(TAG, "从地址 " + url + " 加载频道列表失败: " + e.getMessage());
            }
        }
        
        // 如果所有地址都失败了，抛出最后一个异常
        if (lastException != null) {
            throw lastException;
        } else {
            throw new Exception("无法从任何地址加载频道列表");
        }
    }
    
    /**
     * 从URL获取内容
     * @param urlString URL地址
     * @return 内容字符串
     * @throws IOException IO异常
     */
    private static String fetchContent(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        // 添加User-Agent以避免被阻止
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android TV)");
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP Error: " + responseCode + " for URL: " + urlString);
        }
        
        InputStream inputStream = connection.getInputStream();
        
        // 检查是否是gzip压缩格式
        String contentEncoding = connection.getHeaderField("Content-Encoding");
        boolean isGzip = "gzip".equalsIgnoreCase(contentEncoding) || urlString.toLowerCase().endsWith(".gz");
        
        // 即使URL以.gz结尾，也要检查实际内容是否为GZIP格式
        if (isGzip) {
            try {
                // 尝试读取GZIP头部以确认是否真的是GZIP格式
                PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 2);
                byte[] header = new byte[2];
                int bytesRead = pushbackInputStream.read(header);
                if (bytesRead == 2) {
                    pushbackInputStream.unread(header, 0, bytesRead);
                    // GZIP文件头部为0x1f8b
                    if (header[0] == (byte) 0x1f && header[1] == (byte) 0x8b) {
                        inputStream = new GZIPInputStream(pushbackInputStream);
                    } else {
                        // 不是真正的GZIP文件，直接使用原始流
                        inputStream = pushbackInputStream;
                        Log.w(TAG, "File extension suggests GZIP but content is not GZIP format: " + urlString);
                    }
                } else {
                    inputStream = pushbackInputStream;
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to detect GZIP format, using raw stream: " + e.getMessage());
                // 如果检测失败，回退到原始流
                inputStream = connection.getInputStream();
            }
        }
        
        // 递归处理可能的嵌套GZ压缩
        inputStream = handleNestedGzip(inputStream, urlString);
        
        // 尝试检测字符编码并转换为字符串
        String content = detectAndConvertToString(inputStream);
        inputStream.close();
        connection.disconnect();
        
        return content;
    }
    
    /**
     * 从内容中提取频道清单URL列表
     * @param content 内容
     * @param sourceUrl 来源URL（用于日志）
     * @return 频道清单URL列表
     */
    public static List<String> extractPlaylistUrls(String content, String sourceUrl) {
        List<String> urls = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            Log.e(TAG, "Content is null or empty");
            return urls;
        }
        
        Log.d(TAG, "Parsing playlist URLs from content with length: " + content.length());
        
        String[] lines = content.split("\n");
        Log.d(TAG, "Total lines to process: " + lines.length);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // 移除BOM标记和其他特殊字符
            line = removeBomAndSpecialChars(line);
            Log.d(TAG, "Processing line " + i + ": '" + line + "'");
            
            // 忽略注释行和空行
            if (!line.isEmpty() && !line.startsWith("#")) {
                if (line.startsWith("http")) {
                    Log.d(TAG, "Found valid URL: " + line);
                    urls.add(line);
                } else {
                    Log.d(TAG, "Line does not start with 'http', skipping: " + line);
                }
            } else {
                Log.d(TAG, "Skipping empty or commented line: " + line);
            }
        }
        
        Log.d(TAG, "Successfully parsed " + urls.size() + " playlist URLs from " + sourceUrl);
        return urls;
    }
    
    /**
     * 移除BOM标记和其他特殊字符
     * @param line 输入行
     * @return 清理后的行
     */
    private static String removeBomAndSpecialChars(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }
        
        // 移除BOM标记和控制字符
        return line.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\uFEFF]", "");
    }
}