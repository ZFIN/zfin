package org.zfin.infrastructure.seo;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlAccessorType(value = XmlAccessType.NONE)
@XmlRootElement(name = "urlset")
public class XmlUrlSet {

    private final String siteUrl;

    @XmlElements({@XmlElement(name = "url", type = XmlUrl.class)})
    private final List<XmlUrl> xmlUrls = new ArrayList<>();

    public XmlUrlSet() {
        this.siteUrl = null;
    }

    public XmlUrlSet(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public void add(String loc, XmlUrl.Priority priority) {
        xmlUrls.add(new XmlUrl(siteUrl + loc, priority));
    }

    public void addAll(List<String> zdbIDList, XmlUrl.Priority medium) {
        for (String zdbID : zdbIDList) {
            add("/" + zdbID, medium);
        }
    }

    public int size() {
        return xmlUrls.size();
    }

    public List<XmlUrl> getXmlUrls() {
        return xmlUrls;
    }
}