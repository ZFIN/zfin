package org.zfin.marker.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.framework.presentation.LookupEntry;

import java.util.List;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class MarkerRepositoryConstructTest extends AbstractDatabaseTest {

    private Logger logger = LogManager.getLogger(MarkerRepositoryConstructTest.class);

    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

    @Test
    public void testConstructComponentsFetch() {
        List<LookupEntry> results = getMarkerRepository().getConstructComponentsForString("ab", "ZDB-PUB-250106-1");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testConstructComponentsFetchIncludesVocabTerms() {
        List<LookupEntry> results = getMarkerRepository().getConstructComponentsForString("la", "ZDB-PUB-250106-1");
        assertNotNull(results);
        assertFalse(results.isEmpty());

        boolean containsVocabTerm = results.stream()
                .anyMatch(entry -> entry.getId().startsWith("ZDB-CV-"));

        assertTrue("Expected results to contain at least one vocabulary term", containsVocabTerm);
    }


}
