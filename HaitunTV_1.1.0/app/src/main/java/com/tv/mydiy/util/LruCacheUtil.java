package com.tv.mydiy.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * LRU缓存工具类
 * 提供统一的缓存管理机制
 */
public class LruCacheUtil {
    private static LruCacheUtil instance;
    private final LruCache<String, Object> memoryCache;
    
    // 添加缓存时间记录
    private final Map<String, Long> cacheTimestamps;
    
    // 缓存过期时间（毫秒），设置为1小时
    private static final long CACHE_EXPIRY_TIME = 60 * 60 * 1000; // 1小时
    
    // 私有构造函数
    private LruCacheUtil() {
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        // 设置缓存大小为最大可用内存的1/8
        int cacheSize = maxMemory / 8;
        
        memoryCache = new LruCache<String, Object>(cacheSize) {
            @Override
            protected int sizeOf(String key, Object value) {
                // 更准确地计算缓存对象的大小
                if (value instanceof String) {
                    return ((String) value).getBytes().length;
                } else if (value instanceof byte[]) {
                    return ((byte[]) value).length;
                } else if (value instanceof Bitmap) {
                    // 计算Bitmap占用的内存
                    Bitmap bitmap = (Bitmap) value;
                    return bitmap.getByteCount();
                } else if (value instanceof Drawable) {
                    // 对于Drawable，尝试获取其Bitmap并计算大小
                    return 10000; // 估计大小
                } else {
                    // 对于其他对象，返回默认大小1
                    return 1;
                }
            }
        };
        
        cacheTimestamps = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized LruCacheUtil getInstance() {
        if (instance == null) {
            instance = new LruCacheUtil();
        }
        return instance;
    }
    
    /**
     * 添加缓存
     */
    public void put(String key, Object value) {
        if (key != null && value != null) {
            memoryCache.put(key, value);
            cacheTimestamps.put(key, System.currentTimeMillis());
        }
    }
    
    /**
     * 获取缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, CACHE_EXPIRY_TIME);
    }
    
    /**
     * 获取缓存，支持自定义过期时间
     * @param key 缓存键
     * @param clazz 类型
     * @param expiryTime 过期时间（毫秒），小于等于0表示永不过期
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz, long expiryTime) {
        if (key != null) {
            // 检查是否过期
            if (expiryTime > 0) {
                Long timestamp = cacheTimestamps.get(key);
                if (timestamp != null && (System.currentTimeMillis() - timestamp) > expiryTime) {
                    // 缓存已过期，移除
                    remove(key);
                    return null;
                }
            }
            
            Object value = memoryCache.get(key);
            if (clazz.isInstance(value)) {
                return (T) value;
            }
        }
        return null;
    }
    
    /**
     * 移除缓存
     */
    public void remove(String key) {
        if (key != null) {
            memoryCache.remove(key);
            cacheTimestamps.remove(key);
        }
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        memoryCache.evictAll();
        cacheTimestamps.clear();
    }
    
    /**
     * 获取缓存大小
     */
    public int size() {
        return memoryCache.size();
    }

    /**
     * 安全关闭资源
     * @param closeable 可关闭的对象
     */
    public static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (java.io.IOException ignored) {
                // 忽略关闭异常
            }
        }
    }
}