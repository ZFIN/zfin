package org.zfin.util.downloads.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Remove trailing and prepending white spaces including new lines.
 */
public class CleanupAdapter extends XmlAdapter<String, String> {

    @Override
    public String marshal(String text) {
        return text.trim();
    }

    @Override
    public String unmarshal(String text) throws Exception {
        return text.trim();
    }
}
