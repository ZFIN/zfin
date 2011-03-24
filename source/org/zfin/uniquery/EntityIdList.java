package org.zfin.uniquery;

import org.apache.commons.configuration.CompositeConfiguration;

import java.util.List;

/**
 * Create a list of ids that identify an entity detail page
 * used for indexing purposes.
 */
public interface EntityIdList {

    List<String> getUrlList(int numberOfRecords);

    void setEntityUrlMapping(CompositeConfiguration entityUrlMapping);

}
