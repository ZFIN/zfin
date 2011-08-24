package org.zfin.uniquery;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;
import org.zfin.AbstractDatabaseTest;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class SequenceIdListTest extends AbstractDatabaseTest {

    private final Logger LOG = Logger.getLogger(SequenceIdListTest.class);

    @Test
    public void getUrlsForSequenceViewPages() {
        SequenceIdList list = new SequenceIdList();
        CompositeConfiguration entityUrlMapping = new CompositeConfiguration();
        entityUrlMapping.addConfiguration(new SystemConfiguration());
        File mappingFile = FileUtil.createFile("server_apps", "quicksearch", GenerateEntityDetailPageUrls.entityMappingFileName);
        try {
            entityUrlMapping.addConfiguration(new PropertiesConfiguration(mappingFile.getAbsolutePath()));
        } catch (ConfigurationException e) {
            LOG.error("error during configuration file initialization");
        }

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
