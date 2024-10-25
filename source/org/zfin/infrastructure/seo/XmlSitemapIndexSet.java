package org.zfin.infrastructure.seo;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlAccessorType(value = XmlAccessType.NONE)
@XmlRootElement(name = "sitemapindex")
public class XmlSitemapIndexSet {

    @XmlElements({@XmlElement(name = "url", type = XmlSitemapIndexEntry.class)})
    private final Collection<XmlSitemapIndexEntry> sitemaps = new ArrayList<>();

    public void add(String loc) {
        sitemaps.add(new XmlSitemapIndexEntry(loc));
    }

    public void addAll(List<String> locList) {
        for (String loc : locList) {
            add(loc);
        }
    }
}