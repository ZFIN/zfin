package org.zfin.uniquery;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.log4j.Logger;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
abstract public class AbstractEntityIdList implements EntityIdList {

    protected static Logger LOG = Logger.getLogger(AbstractEntityIdList.class);
    protected CompositeConfiguration entityUrlMapping;


    protected AbstractEntityIdList(CompositeConfiguration entityUrlMapping) {
        this.entityUrlMapping = entityUrlMapping;
    }

    public void setEntityUrlMapping(CompositeConfiguration entityUrlMapping) {
        this.entityUrlMapping = entityUrlMapping;
    }

    /**
     * Use this constructor if you do not use the composite Configuration.
     */
    protected AbstractEntityIdList() {
    }

    public List<String> convertIdsIntoUrls(List<String> allMarkers) {
        List<String> urlList = new ArrayList<String>(allMarkers.size());
        for (String id : allMarkers) {
            String typeName = null;
            ActiveData.Type type = null;
            try {
                typeName = ActiveData.validateID(id).name();
            } catch (Exception e) {
                // ignore exception as this call is used to find out if it is a valid data
            }
            // try ActiveSource
            if (typeName == null) {
                typeName = ActiveSource.validateID(id).name();
            }
            if (typeName == null)
                throw new RuntimeException("No active Data / Source entity found for " + id);

            String individualUrl = getIndividualUrl(id, typeName);
            if (individualUrl != null)
                urlList.add(individualUrl);

        }
        return urlList;
    }

    protected String getIndividualUrl(String id, String typeName) {
        String url = entityUrlMapping.getString(typeName);
        if (url == null) {
            LOG.warn("No url mapping found for id " + id);
            return null;
        }
        if (!url.endsWith("="))
            url += "=";
        if (!url.startsWith("/"))
            url += "/";
        return ZfinPropertiesEnum.NON_SECURE_HTTP.toString() + ZfinPropertiesEnum.DOMAIN_NAME + url + id;
    }
}
