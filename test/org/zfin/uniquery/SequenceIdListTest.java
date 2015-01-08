package org.zfin.uniquery;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.util.FileUtil;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SequenceIdListTest extends AbstractDatabaseTest {

    private final Logger LOG = Logger.getLogger(SequenceIdListTest.class);

    @Test
    public void getUrlsForSequenceViewPages() throws ConfigurationException {
        SequenceIdList list = new SequenceIdList();
        CompositeConfiguration entityUrlMapping = new CompositeConfiguration();
        entityUrlMapping.addConfiguration(new SystemConfiguration());
        File mappingFile = FileUtil.createFile("server_apps", "quicksearch", GenerateEntityDetailPageUrls.entityMappingFileName);
        entityUrlMapping.addConfiguration(new PropertiesConfiguration(mappingFile.getAbsolutePath()));
        list.setEntityUrlMapping(entityUrlMapping);
        List<String> ids = list.getUrlList(20);
        assertNotNull(ids);
        assertEquals(20, ids.size());
        String idOne = ids.get(0);
        int indexOfLastSlash = idOne.lastIndexOf("/");
        String queryString = idOne.substring(indexOfLastSlash);
        assertTrue(!queryString.contains("="));
    }

}
