package org.zfin.infrastructure.seo;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(value = XmlAccessType.NONE)
@XmlRootElement(name = "sitemapindex")
public class XmlSitemapIndexSet {

    @XmlElements({@XmlElement(name = "sitemap", type = XmlSitemapIndexEntry.class)})
    private final List<XmlSitemapIndexEntry> sitemaps = new ArrayList<>();

    public void add(String loc) {
        sitemaps.add(new XmlSitemapIndexEntry(loc));
    }

    public void addAll(List<String> locList) {
        for (String loc : locList) {
            add(loc);
        }
    }

    public List<XmlSitemapIndexEntry> getSitemaps() {
        return sitemaps;
    }
}
