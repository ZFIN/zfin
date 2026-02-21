package org.zfin.mapping.presentation;

import lombok.Data;
import org.zfin.jbrowse.presentation.JBrowse2Image;

@Data
public class BrowserLink implements Comparable<BrowserLink> {
    private String url;
    private String name;
    private int order;

    private JBrowse2Image genomeBrowserImage;

    @Override
    public int compareTo(BrowserLink o) {
        return o.getOrder() - this.order;
    }
}
