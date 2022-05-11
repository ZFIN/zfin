package org.zfin.mapping.importer;

import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerService;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.mapping.GenomeLocation.Source.AGP_LOAD;
import static org.zfin.repository.RepositoryFactory.getLinkageRepository;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

public class ImportCloneAssemblyTask extends AbstractScriptWrapper {

    private String inputFilename;
    private String inputFileChecksum;

    public static void main(String[] args) {
        ImportCloneAssemblyTask task = new ImportCloneAssemblyTask();
        task.runTask();
    }

    public void runTask() {
        initAll();
        getInputFilenameOrDie();
        List<AGPEntry> agpEntries = loadAgpEntriesFromFile();
        saveAgpEntriesToDatabase(agpEntries);
        filterAgpEntriesAndStoreGenomeLocation(agpEntries);
    }

    private void getInputFilenameOrDie() {
        if (System.getenv("AGP_FILE") != null) {
            inputFilename = System.getenv("AGP_FILE");
        } else {
            printUsage();
            System.exit(1);
        }
    }

    private List<AGPEntry> loadAgpEntriesFromFile() {
        List<AGPEntry> data = new ArrayList<>();
        try {
            System.out.println("Loading " + inputFilename);

            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            File file = new File(inputFilename);
            FileInputStream fis = new FileInputStream(file);
            DigestInputStream dis = new DigestInputStream(fis, messageDigest);
            InputStreamReader isr = new InputStreamReader(dis);
            BufferedReader input = new BufferedReader(isr);

            data = input.lines()
                        .map(AGPEntry::new)
                        .collect(Collectors.toList());
            input.close();
            byte[] digest = dis.getMessageDigest().digest();
            String digestString = Base64.getEncoder().encodeToString(digest);
            LOG.debug("Finished reading " + inputFilename + " with SHA: " + digestString);
            this.inputFileChecksum = digestString;
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.err.println("Error loading file");
            System.exit(1);
        }
        return data;
    }

    private void saveAgpEntriesToDatabase(List<AGPEntry> agpEntries) {
        Transaction transaction = HibernateUtil.createTransaction();
        getLinkageRepository().deleteAllAGPEntries();
        getLinkageRepository().deleteAllGenomeLocationsBySource(AGP_LOAD);

        for(AGPEntry entry : agpEntries) {
            getLinkageRepository().saveAGPEntry(entry);
        }
        transaction.commit();
    }

    private void filterAgpEntriesAndStoreGenomeLocation(List<AGPEntry> agpEntries) {
        Transaction transaction = HibernateUtil.createTransaction();
        for(AGPEntry entry : agpEntries) {
            DBLink dbLink;
            if (entry.componentID == null) {
                continue;
            }
            if (entry.chromosome.contains("_alt")) {
                continue;
            }
            String componentID = entry.componentID.replaceAll("\\.\\d+$", "");
            List<DBLink> dbLinks = getSequenceRepository()
                    .getDBLinksForAccession(componentID)
                    .stream()
                    .filter(entity -> MarkerService.isOfTypeClone(entity.getDataZdbID()))
                    .collect(Collectors.toList());
            if (dbLinks.size() == 0) {
                LOG.debug("No DBLink Found for " + componentID);
                continue;
            } else {
                if (dbLinks.size() > 1) {
                    LOG.debug("Multiple DBLinks Found for " + componentID);
                }
                dbLink = dbLinks.get(0);
                LOG.debug("Adding dbLink: " + dbLink.getDataZdbID() + ", " + componentID);
            }
            Marker marker = RepositoryFactory.getMarkerRepository().getMarker(dbLink.getDataZdbID());
            MarkerGenomeLocation mgl = new MarkerGenomeLocation();
            mgl.setMarker(marker);
            mgl.setAssembly(GenomeLocation.GRCZ11);
            mgl.setAccessionNumber(entry.componentID);
            mgl.setChromosome(entry.chromosome.replaceAll("chr", ""));
            mgl.setStart(entry.objectStart);
            mgl.setEnd(entry.objectEnd);
            mgl.setSource(AGP_LOAD);
            mgl.setDetailedSource("GRCz11.agp:checksum:" + this.inputFileChecksum);
            RepositoryFactory.getLinkageRepository().saveMarkerGenomeLocation(mgl);
        }
        transaction.commit();
    }

    public static void printUsage() {
        System.err.println("Call with AGP file environment variable AGP_FILE");
    }
}
