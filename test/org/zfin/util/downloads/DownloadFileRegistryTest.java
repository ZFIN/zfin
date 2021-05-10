package org.zfin.util.downloads;

import org.zfin.util.downloads.jaxb.ColumnHeader;
import org.zfin.util.downloads.jaxb.DownloadCategory;
import org.zfin.util.downloads.jaxb.DownloadFileEntry;
import org.zfin.util.downloads.jaxb.DownloadFileRegistry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DownloadFileRegistryTest {

    private static final String DOWNLOAD_REGISTRY = "./home/WEB-INF/conf/download-registry.xml";

    public static void main(String[] args) throws JAXBException, IOException {

        List<DownloadFileEntry> downloadEntries = new ArrayList<DownloadFileEntry>();

        List<ColumnHeader> headerList = new ArrayList<ColumnHeader>(4);
        ColumnHeader headerTwo = new ColumnHeader();
        headerTwo.setColumn("Marker Symbol");
        headerTwo.setId(2);
        headerTwo.setUnique(false);
        ColumnHeader headerOne = new ColumnHeader();
        headerOne.setColumn("Marker ID");
        headerOne.setId(1);
        headerOne.setUnique(true);
        headerList.add(headerOne);
        headerList.add(headerTwo);

        // create books
        DownloadFileEntry entry = new DownloadFileEntry();
        entry.setName("The Game");
        entry.setCategory("Genetic Marker");
        entry.setDescription("Desc");
        entry.setFileName("genetic_marker");
        entry.setFileExtension("txt");
        entry.setFileFormat("gff3");
        entry.setQuery("select * from marker");
        entry.setWikiLink("Wiki LInk");
        entry.setColumnHeaderList(headerList);
        entry.setOrderIndex(10);
        downloadEntries.add(entry);

        DownloadFileEntry entryTwo = new DownloadFileEntry();
        entryTwo.setName("The Game 2");
        entryTwo.setCategory("Genetic Marker 2");
        entryTwo.setDescription("Desc 2");
        entryTwo.setFileName("genetic_marker 2");
        entryTwo.setFileExtension("txt");
        entryTwo.setQuery("select * from marker where is ");
        entryTwo.setWikiLink("Wiki LInk");
        entryTwo.setColumnHeaderList(headerList);
        entryTwo.setOrderIndex(20);
        downloadEntries.add(entryTwo);

        DownloadCategory category = new DownloadCategory();
        category.setName("Orthology");
        category.setOrderIndex(10);
        List<DownloadCategory> downloadCategories = new ArrayList<DownloadCategory>(1);
        downloadCategories.add(category);


        // create bookstore, assigning book
        DownloadFileRegistry fileRegistry = new DownloadFileRegistry();
        DownloadFileRegistry.DownloadDefinition downloadDefinition = new DownloadFileRegistry.DownloadDefinition();
        downloadDefinition.setDownloadFileEntries(downloadEntries);
        fileRegistry.setDownloadDefinition(downloadDefinition);

        DownloadFileRegistry.CategoryDefinition categoryDefinition = new DownloadFileRegistry.CategoryDefinition();
        categoryDefinition.setDownloadCategories(downloadCategories);
        fileRegistry.setCategoryDefinition(categoryDefinition);
        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(DownloadFileRegistry.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(fileRegistry, System.out);

/*
        Writer w = null;
        try {
            w = new FileWriter(DOWNLOAD_REGISTRY);
            m.marshal(bookstore, w);
        } finally {
            try {
                w.close();
            } catch (Exception e) {
            }
        }
*/

        // get variables from our xml file, created before
/*
        System.out.println();
        System.out.println("Output from our XML File: ");
        Unmarshaller um = context.createUnmarshaller();
        DownloadFileRegistry registry = (DownloadFileRegistry) um.unmarshal(new FileReader(
                DOWNLOAD_REGISTRY));
*/
        Unmarshaller um = context.createUnmarshaller();
        DownloadFileRegistry registry = (DownloadFileRegistry) um.unmarshal(new FileReader(
                DOWNLOAD_REGISTRY));

        for (int i = 0; i < registry.getDownloadFileEntryList().toArray().length; i++) {
            System.out.println("Download " + (i + 1) + ": "
                    + registry.getDownloadFileEntryList().get(i) + " from "
                    + registry.getDownloadFileEntryList().get(i));
        }

    }

}
