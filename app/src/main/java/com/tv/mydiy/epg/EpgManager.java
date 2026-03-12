package com.tv.mydiy.epg;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;

import com.tv.mydiy.util.LogUtil;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.network.NetworkManager;

public class EpgManager {
    private static final String TAG = "EpgManager";
    private static final String TIME_FORMAT_REGEX = "^(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2}) .*";
    private final Map<String, EpgProgram> epgDataCache = new HashMap<>();
    private final Map<String, List<EpgProgram>> channelPrograms = new HashMap<>();
    private final Map<String, String> channelNameMap = new HashMap<>();
    private final Map<String, EpgProgram> epgDataMap = new HashMap<>();
    private final String epgListUrl;
    private final List<String> epgUrls = new ArrayList<>();
    private final NetworkManager networkManager;

    public EpgManager(Context context) {
        this.epgListUrl = context.getString(com.tv.mydiy.R.string.epg_list_url);
        this.networkManager = new NetworkManager();
        initializeDefaultEpgUrls();
    }

    private void initializeDefaultEpgUrls() {
        epgUrls.add("https://epg.112114.xyz/pp.xml");
        LogUtil.d(TAG, "Initialized with " + epgUrls.size() + " default EPG URLs");
    }

    public void fetchEpgUrlsFromRemote(EpgUrlsCallback callback) {
        new FetchEpgUrlsTask(callback, networkManager).execute(epgListUrl);
    }

    public void fetchEpgDataFromRemote(String epgUrl, EpgDataCallback callback) {
        if (epgUrl != null && (epgUrl.startsWith("<?xml") || epgUrl.startsWith("<"))) {
            LogUtil.d(TAG, "EPG参数看起来像是XML数据而不是URL，直接返回数据");
            if (callback != null) {
                callback.onEpgDataFetched(epgUrl);
            }
            return;
        }
        
        new FetchEpgDataTask(callback, networkManager).execute(epgUrl);
    }

    private void parseEpgData(String epgData) {
        epgDataCache.clear();
        channelPrograms.clear();
        channelNameMap.clear();
        epgDataMap.clear();
        
        if (epgData == null || epgData.isEmpty()) {
            return;
        }

        if (!epgData.trim().startsWith("<?xml")) {
            if (epgData.trim().startsWith("<")) {
                String trimmedData = epgData.trim();
                if (!trimmedData.endsWith(">")) {
                    return;
                }
                
                epgData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tv>" + epgData + "</tv>";
            } else {
                return;
            }
        }
        
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(epgData));
            
            int eventType = parser.getEventType();
            String currentChannelId = null;
            String currentChannelName;
            EpgProgram currentProgram = null;
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("channel".equals(tagName)) {
                            currentChannelId = parser.getAttributeValue(null, "id");
                        } else if ("display-name".equals(tagName) && currentChannelId != null) {
                            currentChannelName = parser.nextText();
                            if (currentChannelName != null) {
                                channelNameMap.put(currentChannelId, currentChannelName);
                            }
                        } else if ("programme".equals(tagName)) {
                            String channelId = parser.getAttributeValue(null, "channel");
                            String start = parser.getAttributeValue(null, "start");
                            String stop = parser.getAttributeValue(null, "stop");
                            
                            if (channelId == null || start == null) {
                                break;
                            }
                            
                            currentProgram = new EpgProgram();
                            currentProgram.setChannelId(channelId);
                            currentProgram.setStartTime(start);
                            currentProgram.setEndTime(stop);
                        } else if ("title".equals(tagName) && currentProgram != null) {
                            String title = parser.nextText();
                            currentProgram.setTitle(title);
                        } else if ("desc".equals(tagName) && currentProgram != null) {
                            String desc = parser.nextText();
                            currentProgram.setDescription(desc);
                        }
                        break;
                        
                    case XmlPullParser.END_TAG:
                        if ("channel".equals(tagName)) {
                            currentChannelId = null;
                        } else if ("programme".equals(tagName) && currentProgram != null) {
                            if (currentProgram.getChannelId() != null) {
                                List<EpgProgram> programs = channelPrograms.get(currentProgram.getChannelId());
                                if (programs == null) {
                                    programs = new ArrayList<>();
                                    channelPrograms.put(currentProgram.getChannelId(), programs);
                                }
                                programs.add(currentProgram);
                                
                                if (programs.size() == 1) {
                                    epgDataMap.put(currentProgram.getChannelId(), currentProgram);
                                }
                            }
                            currentProgram = null;
                        }
                        break;
                }
                
                eventType = parser.next();
            }
            
            LogUtil.d(TAG, "EPG数据解析完成，共解析 " + channelPrograms.size() + " 个频道");
            
        } catch (Exception e) {
            LogUtil.e(TAG, "Error parsing EPG data", e);
        }
    }

    public EpgProgram getCurrentProgram(String channelId) {
        if (channelId == null) {
            return null;
        }
        
        long currentTime = System.currentTimeMillis();
        
        List<EpgProgram> programs = channelPrograms.get(channelId);
        if (programs != null && !programs.isEmpty()) {
            EpgProgram currentProgram = findCurrentProgram(programs, currentTime);
            if (currentProgram != null) {
                return currentProgram;
            }
        }
        
        for (Map.Entry<String, String> entry : channelNameMap.entrySet()) {
            String epgChannelId = entry.getKey();
            String epgChannelName = entry.getValue();
            
            if (channelId.equals(epgChannelName)) {
                List<EpgProgram> programsList = channelPrograms.get(epgChannelId);
                if (programsList != null && !programsList.isEmpty()) {
                    EpgProgram currentProgram = findCurrentProgram(programsList, currentTime);
                    if (currentProgram != null) {
                        return currentProgram;
                    }
                }
            }
            
            if (normalizeChannelName(channelId).equals(normalizeChannelName(epgChannelName))) {
                List<EpgProgram> programsList = channelPrograms.get(epgChannelId);
                if (programsList != null && !programsList.isEmpty()) {
                    EpgProgram currentProgram = findCurrentProgram(programsList, currentTime);
                    if (currentProgram != null) {
                        return currentProgram;
                    }
                }
            }
        }
        
        String channelName = channelNameMap.get(channelId);
        if (channelName != null) {
            List<EpgProgram> programsList = channelPrograms.get(channelId);
            if (programsList != null && !programsList.isEmpty()) {
                return findCurrentProgram(programsList, currentTime);
            }
        }
        
        return null;
    }
    
    private EpgProgram findCurrentProgram(List<EpgProgram> programs, long currentTime) {
        for (EpgProgram program : programs) {
            long startTime = parseEpgTimeToMillisOptimized(program.getStartTime());
            long endTime = parseEpgTimeToMillisOptimized(program.getEndTime());
            
            if (startTime <= currentTime && currentTime <= endTime) {
                return program;
            }
        }
        
        EpgProgram nextProgram = null;
        for (EpgProgram program : programs) {
            long startTime = parseEpgTimeToMillisOptimized(program.getStartTime());
            if (startTime > currentTime) {
                if (nextProgram == null || startTime < parseEpgTimeToMillisOptimized(nextProgram.getStartTime())) {
                    nextProgram = program;
                }
            }
        }
        
        if (nextProgram == null && !programs.isEmpty()) {
            nextProgram = programs.get(programs.size() - 1);
        }
        
        return nextProgram;
    }

    private final Map<String, Long> timeCache = new HashMap<>();
    
    private long parseEpgTimeToMillisOptimized(String epgTime) {
        if (epgTime == null || epgTime.isEmpty()) {
            return 0;
        }
        
        Long cached = timeCache.get(epgTime);
        if (cached != null) {
            return cached;
        }
        
        try {
            String trimmedTime = epgTime.trim();
            
            String timePart;
            String timezonePart = null;
            
            if (trimmedTime.contains(" ")) {
                String[] parts = trimmedTime.split("\\s+");
                timePart = parts[0];
                if (parts.length > 1) {
                    timezonePart = parts[1];
                }
            } else {
                timePart = trimmedTime;
            }
            
            if (timePart.length() >= 14) {
                String year = timePart.substring(0, 4);
                String month = timePart.substring(4, 6);
                String day = timePart.substring(6, 8);
                String hour = timePart.substring(8, 10);
                String minute = timePart.substring(10, 12);
                String second = timePart.substring(12, 14);
                
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(java.util.Calendar.YEAR, Integer.parseInt(year));
                calendar.set(java.util.Calendar.MONTH, Integer.parseInt(month) - 1);
                calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
                calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(minute));
                calendar.set(java.util.Calendar.SECOND, Integer.parseInt(second));
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                
                if (timezonePart != null && timezonePart.length() >= 5) {
                    try {
                        char sign = timezonePart.charAt(0);
                        int tzHour = Integer.parseInt(timezonePart.substring(1, 3));
                        int tzMinute = Integer.parseInt(timezonePart.substring(3, 5));
                        int tzOffsetMillis = (tzHour * 60 + tzMinute) * 60 * 1000;
                        
                        if (sign == '-') {
                            tzOffsetMillis = -tzOffsetMillis;
                        }
                        
                        java.util.TimeZone localTz = java.util.TimeZone.getDefault();
                        int localOffset = localTz.getOffset(calendar.getTimeInMillis());
                        
                        calendar.add(java.util.Calendar.MILLISECOND, -tzOffsetMillis + localOffset);
                    } catch (Exception e) {
                        LogUtil.w(TAG, "Failed to parse timezone offset: " + timezonePart, e);
                    }
                }
                
                long result = calendar.getTimeInMillis();
                timeCache.put(epgTime, result);
                
                return result;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Error parsing EPG time: " + epgTime, e);
        }
        
        return 0;
    }
    
    private String normalizeChannelName(String channelName) {
        if (channelName == null) {
            return null;
        }
        
        String normalized = channelName.replaceAll("-", "");
        normalized = normalized.trim();
        
        return normalized;
    }

    public List<EpgProgram> getAllProgramsForChannel(Channel channel) {
        List<EpgProgram> programs = new ArrayList<>();
        
        String channelId = channel.getTvgId();
        if (channelId == null || channelId.isEmpty()) {
            channelId = channel.getName();
        }
        
        List<EpgProgram> channelProgramsList = channelPrograms.get(channelId);
        if (channelProgramsList != null) {
            programs.addAll(channelProgramsList);
            return programs;
        }
        
        for (Map.Entry<String, String> entry : channelNameMap.entrySet()) {
            String epgChannelId = entry.getKey();
            String epgChannelName = entry.getValue();
            
            if (channelId.equals(epgChannelName)) {
                List<EpgProgram> progList = channelPrograms.get(epgChannelId);
                if (progList != null) {
                    programs.addAll(progList);
                    return programs;
                }
            }
            
            if (normalizeChannelName(channelId).equals(normalizeChannelName(epgChannelName))) {
                List<EpgProgram> progList = channelPrograms.get(epgChannelId);
                if (progList != null) {
                    programs.addAll(progList);
                    return programs;
                }
            }
        }
        
        return programs;
    }

    public void loadEpgData(OnEpgLoadListener listener) {
        fetchEpgUrlsFromRemote(urls -> {
            if (urls != null && !urls.isEmpty()) {
                LogUtil.d(TAG, "成功获取到 " + urls.size() + " 个EPG地址");
                for (String epgUrl : urls) {
                    if (epgUrl != null && !epgUrl.isEmpty()) {
                        LogUtil.d(TAG, "尝试加载EPG数据: " + epgUrl);
                        fetchEpgDataFromRemote(epgUrl, epgData -> {
                            if (epgData != null && !epgData.isEmpty()) {
                                LogUtil.d(TAG, "成功获取EPG数据，长度: " + epgData.length());
                                parseEpgData(epgData);
                                
                                if (!epgDataMap.isEmpty()) {
                                    LogUtil.d(TAG, "EPG数据解析成功，共解析 " + epgDataMap.size() + " 个频道");
                                    if (listener != null) {
                                        listener.onEpgLoaded();
                                    }
                                } else {
                                    LogUtil.w(TAG, "EPG数据解析失败，未找到任何频道信息");
                                }
                            } else {
                                LogUtil.w(TAG, "EPG数据为空: " + epgUrl);
                            }
                        });
                        break;
                    }
                }
                if (listener != null) {
                    listener.onEpgLoadFailed("所有EPG地址均加载失败");
                }
            } else {
                LogUtil.w(TAG, "未能获取EPG地址列表，尝试使用默认EPG地址");
                fetchEpgDataFromRemote(epgListUrl, epgData -> {
                    if (epgData != null && !epgData.isEmpty()) {
                        LogUtil.d(TAG, "成功获取EPG数据，长度: " + epgData.length());
                        parseEpgData(epgData);
                        
                        if (!epgDataMap.isEmpty()) {
                            LogUtil.d(TAG, "EPG数据解析成功，共解析 " + epgDataMap.size() + " 个频道");
                            if (listener != null) {
                                listener.onEpgLoaded();
                            }
                        } else {
                            LogUtil.w(TAG, "EPG数据解析失败，未找到任何频道信息");
                            tryDefaultEpgUrls(listener);
                        }
                    } else {
                        LogUtil.w(TAG, "EPG数据为空，尝试默认EPG地址");
                        tryDefaultEpgUrls(listener);
                    }
                });
            }
        });
    }
    
    private void tryDefaultEpgUrls(OnEpgLoadListener listener) {
        LogUtil.d(TAG, "尝试默认EPG地址: " + epgUrls.size() + " 个可用地址");
        for (String defaultUrl : epgUrls) {
            LogUtil.d(TAG, "尝试默认EPG地址: " + defaultUrl);
            fetchEpgDataFromRemote(defaultUrl, epgData -> {
                if (epgData != null && !epgData.isEmpty()) {
                    LogUtil.d(TAG, "成功获取默认EPG数据，长度: " + epgData.length());
                    parseEpgData(epgData);
                    
                    if (!epgDataMap.isEmpty()) {
                        LogUtil.d(TAG, "默认EPG数据解析成功，共解析 " + epgDataMap.size() + " 个频道");
                        if (listener != null) {
                            listener.onEpgLoaded();
                        }
                        return;
                    } else {
                        LogUtil.w(TAG, "默认EPG数据解析失败，未找到任何频道信息");
                    }
                } else {
                    LogUtil.w(TAG, "默认EPG数据为空: " + defaultUrl);
                }
                
                if (listener != null) {
                    listener.onEpgLoadFailed("所有EPG地址均加载失败");
                }
            });
            break;
        }
    }

    public void clearEpgData() {
        epgDataCache.clear();
        channelPrograms.clear();
        channelNameMap.clear();
        epgDataMap.clear();
        timeCache.clear();
        LogUtil.d(TAG, "EPG数据已清除");
    }

    public interface OnEpgLoadListener {
        void onEpgLoaded();
        void onEpgLoadFailed(String error);
    }

    public interface EpgUrlsCallback {
        void onEpgUrlsFetched(List<String> urls);
    }

    public interface EpgDataCallback {
        void onEpgDataFetched(String epgData);
    }

    private static class FetchEpgUrlsTask extends AsyncTask<String, Void, List<String>> {
        private final EpgUrlsCallback callback;
        private final NetworkManager networkManager;

        public FetchEpgUrlsTask(EpgUrlsCallback callback, NetworkManager networkManager) {
            this.callback = callback;
            this.networkManager = networkManager;
        }

        @Override
        protected List<String> doInBackground(String... urls) {
            List<String> result = new ArrayList<>();
            
            try {
                if (urls.length > 0) {
                    String content = networkManager.fetchUrlContent(urls[0]);
                    if (content != null && !content.isEmpty()) {
                        String[] lines = content.split("\n");
                        for (String line : lines) {
                            if (!line.trim().isEmpty()) {
                                result.add(line.trim());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Error fetching EPG URLs: " + e.getMessage());
            }
            
            LogUtil.d(TAG, "Fetched " + result.size() + " EPG URLs");
            return result;
        }

        @Override
        protected void onPostExecute(List<String> urls) {
            if (callback != null) {
                callback.onEpgUrlsFetched(urls);
            }
        }
    }

    private static class FetchEpgDataTask extends AsyncTask<String, Void, String> {
        private final EpgDataCallback callback;
        private final NetworkManager networkManager;

        public FetchEpgDataTask(EpgDataCallback callback, NetworkManager networkManager) {
            this.callback = callback;
            this.networkManager = networkManager;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if (params.length > 0) {
                    String epgUrl = params[0];
                    
                    if (epgUrl == null || epgUrl.isEmpty()) {
                        LogUtil.e(TAG, "EPG URL is null or empty");
                        return "";
                    }

                    if (epgUrl.startsWith("<?xml") || epgUrl.startsWith("<")) {
                        LogUtil.d(TAG, "EPG URL appears to be XML data, returning as-is");
                        return epgUrl;
                    }

                    LogUtil.d(TAG, "正在请求EPG数据: " + epgUrl);
                    String content = networkManager.fetchUrlContent(epgUrl);
                    
                    if (content != null && !content.isEmpty()) {
                        LogUtil.d(TAG, "Fetched EPG data with length: " + content.length());
                        return content;
                    } else {
                        LogUtil.w(TAG, "EPG data is empty for URL: " + epgUrl);
                    }
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Error fetching EPG data from " + params[0], e);
            }
            
            return "";
        }

        @Override
        protected void onPostExecute(String epgData) {
            if (callback != null) {
                callback.onEpgDataFetched(epgData);
            }
        }
    }
    
    public static String formatEpgTimeToHM(String epgTime) {
        if (epgTime == null || epgTime.isEmpty()) {
            return "";
        }
        
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(TIME_FORMAT_REGEX);
            java.util.regex.Matcher matcher = pattern.matcher(epgTime);
            
            if (matcher.matches()) {
                String hour = matcher.group(4);
                String minute = matcher.group(5);
                return hour + "：" + minute;
            }
            
            String trimmedTime = epgTime.trim();
            if (trimmedTime.length() >= 12) {
                String hour = trimmedTime.substring(8, 10);
                String minute = trimmedTime.substring(10, 12);
                return hour + "：" + minute;
            }
            
        } catch (Exception e) {
            LogUtil.e(TAG, "解析EPG时间格式时出错: " + e.getMessage() + " 时间字符串: " + epgTime);
        }
        
        return "";
    }
    
    public static String formatProgramInfo(EpgProgram program) {
        if (program == null) {
            return "";
        }
        
        String title = program.getTitle();
        if (title != null && !title.isEmpty()) {
            return title;
        }
        
        return "";
    }
}
