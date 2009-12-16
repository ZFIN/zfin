package org.zfin.framework.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.framework.CacheItem;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;

import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class CacheController extends AbstractCommandController {

    public CacheController() {
        setCommandClass(CacheForm.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        CacheForm form = (CacheForm) command;
        CacheManager manager = CacheManager.getInstance();
        String[] cacheNames = manager.getCacheNames();
        form.setCacheNames(cacheNames);
        List<CacheItem> list = new ArrayList<CacheItem>();
        for (String regionName : cacheNames) {
            Cache cache = manager.getCache(regionName);
            CacheItem item = new CacheItem();
            item.setCache(cache);
            if (form.isShowObjects() && form.getRegionName().equals(regionName)) {
                form.setRegionCache(cache);
            }
            if (form.isShowSize()) {
                if (StringUtils.isEmpty(form.getRegionName()))
                    item.setMemorySize(cache.calculateInMemorySize());
                else if (form.getRegionName() != null && form.getRegionName().equals(regionName))
                    item.setMemorySize(cache.calculateInMemorySize());
            }
            list.add(item);
        }
        form.setCaches(list);
        return new ModelAndView("cache-viewer", LookupStrings.FORM_BEAN, form);
    }

}
