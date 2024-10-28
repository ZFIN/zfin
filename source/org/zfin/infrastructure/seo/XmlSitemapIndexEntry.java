package org.zfin.infrastructure.seo;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlRootElement(name = "sitemap")
public class XmlSitemapIndexEntry {

    @XmlElement
    private String loc;

    public XmlSitemapIndexEntry() {
    }

    public XmlSitemapIndexEntry(String loc) {
        this.loc = loc;
    }

}