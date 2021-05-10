package org.zfin.gbrowse;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.marker.Marker;

import static org.junit.Assert.assertNotNull;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;


public class GBrowseServiceTest extends AbstractDatabaseTest {


    @Test
    public void checkBrowseImageForSNP() {
        String id = "ZDB-SNP-060626-105";
        Marker snp = getMarkerRepository().getMarkerByID(id);
        Marker marker = GBrowseService.getGbrowseTrackingGene(snp);
        assertNotNull(marker);
    }

}


