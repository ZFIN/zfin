package org.zfin.util.downloads.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.zfin.AbstractSmokeTest;
import org.zfin.util.downloads.jaxb.ColumnHeader;
import org.zfin.util.downloads.jaxb.DownloadCategory;
import org.zfin.util.downloads.jaxb.DownloadFileEntry;
import org.zfin.util.downloads.jaxb.DownloadFileRegistry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadFilesSmokeTest extends AbstractSmokeTest {

    /**
     * Just pull up the most current downloads page
     */
    public void testDownloadPagePageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads");
                assertEquals("Archive:", "Archive:", page.getTitleText());
                List<?> downloadFileLink = page.getByXPath("//a[@id='antibodies.txt']");
                // exactly one link
                assertNotNull("could not find antibodies.txt download link", downloadFileLink);
                assertEquals(1, downloadFileLink.size());
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }


}
