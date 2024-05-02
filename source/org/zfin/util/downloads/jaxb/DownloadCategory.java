package org.zfin.util.downloads.jaxb;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Category for download files.
 */
@XmlRootElement(name = "Category")
public class DownloadCategory {

    private String name;
    private int orderIndex;

    @XmlElement(name = "name")
    @XmlJavaTypeAdapter(CleanupAdapter.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "orderIndex")
    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
