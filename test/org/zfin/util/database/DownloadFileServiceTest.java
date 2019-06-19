package org.zfin.util.database;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.util.downloads.DownloadFileService;

import static org.junit.Assert.assertNotNull;


public class DownloadFileServiceTest extends AbstractDatabaseTest {

    @Test
    public void getMostRecentMatchingDate() {
        DownloadFileService service = new DownloadFileService();
        String unloadDate = service.getMatchingIndexDirectory();
        while(true){

        }

        //assertNotNull(unloadDate);
    }
}

