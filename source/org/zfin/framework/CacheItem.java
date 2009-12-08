package org.zfin.framework;

import net.sf.ehcache.Cache;


/**
 *
 */
public class CacheItem {

    private String name;
    private Cache cache;
    private long memorySize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    public long getMemorySizeKB(){
        return memorySize / 1024;
    }
}
