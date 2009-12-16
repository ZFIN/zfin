package org.zfin.framework.presentation;

import net.sf.ehcache.Cache;
import org.zfin.framework.CacheItem;

import java.util.List;

/**
 * Serves the cache viewer page
 */
public class CacheForm {

    private String[] cacheNames;
    private List<CacheItem> cacheItems;
    private String action;
    private String regionName;
    public static final String ACTION_SHOW_OBJECTS = "show-objects";
    public static final String ACTION_SHOW_SIZE = "show-size";
    private Cache regionCache;

    public String[] getCacheNames() {
        return cacheNames;
    }

    public void setCacheNames(String[] cacheNames) {
        this.cacheNames = cacheNames;
    }

    public List<CacheItem> getCaches() {
        return cacheItems;
    }

    public void setCaches(List<CacheItem> caches) {
        this.cacheItems = caches;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public boolean isShowObjects() {
        return action != null && action.equals(ACTION_SHOW_OBJECTS);
    }

    public boolean isShowSize() {
        return action != null && action.equals(ACTION_SHOW_SIZE);
    }

    public void setRegionCache(Cache regionCache) {
        this.regionCache = regionCache;
    }

    public Cache getRegionCache() {
        return regionCache;
    }

    public List<CacheItem> getCacheItems() {
        return cacheItems;
    }

    public void setCacheItems(List<CacheItem> cacheItems) {
        this.cacheItems = cacheItems;
    }
}
