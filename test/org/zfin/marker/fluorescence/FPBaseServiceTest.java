package org.zfin.marker.fluorescence;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class FPBaseServiceTest extends AbstractDatabaseTest {
    @Test
    public void testApiFetchByID() {
        FPBaseService service = new FPBaseService();
        List<FPBaseApiResultItem> response = service.lookupFPBaseProteinByID("RO9XQ");
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("RO9XQ", response.get(0).uuid());
    }

    @Test
    public void testApiFetchByName() {
        FPBaseService service = new FPBaseService();
        List<FPBaseApiResultItem> response = service.lookupFPBaseProteinByName("BP02");
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("7XHKJ", response.get(0).uuid());
    }

    @Test
    public void testApiFetchByNameContains() {
        //"SAASoti" and "V127T SAASoti"
        FPBaseService service = new FPBaseService();
        List<FPBaseApiResultItem> response = service.lookupFPBaseProteinByNameContains("SAASoti");
        assertNotNull(response);
        assertTrue(response.size() > 1);
        List<String> discoveredNames = response.stream().map(FPBaseApiResultItem::name).toList();
        assertTrue(discoveredNames.contains("V127T SAASoti"));
        assertTrue(discoveredNames.contains("SAASoti"));
    }

}
