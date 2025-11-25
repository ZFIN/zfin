package org.zfin.task;

import lombok.SneakyThrows;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.zfin.publication.PublicationType.JOURNAL;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.util.ZfinSystemUtils.envTrue;

public class CheckFeatureGeneAttributionTask extends AbstractScriptWrapper {

    private boolean dryRun = true;
    private BufferedWriter csvWriter;
    private File outputFile;

    public static void main(String[] args) throws IOException {
        CheckFeatureGeneAttributionTask task = new CheckFeatureGeneAttributionTask();
        task.runTask();
    }

    @SneakyThrows
    private void runTask() {
        initTask();

        List<Feature> features = getFeatureRepository().getFeaturesByType(FeatureTypeEnum.TRANSGENIC_INSERTION);
        for (Feature feature : features) {
            checkFeature(feature);
        }
        csvWriter.close();
        System.out.println("Wrote results to " + outputFile.getAbsolutePath());
    }

    @SneakyThrows
    private void initTask() {
        initAll();
        outputFile = File.createTempFile("checkFeatureGeneAttributionTask_", ".csv");
        csvWriter = new BufferedWriter(new FileWriter(outputFile));
        if (envTrue("COMMIT_CHANGES")) {
            System.out.println("COMMIT_CHANGES is set, will commit changes to the database.");
            dryRun = false;
        } else {
            System.out.println("Dry run mode, will NOT commit changes to the database.");
            System.out.println("Set COMMIT_CHANGES environment variable to commit changes.");
            dryRun = true;
        }
    }

    private void checkFeature(Feature feature) {
        Set<Marker> genes = feature.getAffectedGenes();
        Set<Publication> pubs = getPublicationIdsForFeature(feature);
        for(Marker gene : genes) {
            for (Publication pub : pubs) {
                addAttributionIfNeeded(gene, pub, feature);
            }
        }
    }

    private void addAttributionIfNeeded(Marker gene, Publication pub, Feature feature) {
        InfrastructureRepository infrastructureRepository = getInfrastructureRepository();
        if (!JOURNAL.equals(pub.getType())) {
            System.out.println("Skipping publication " + pub.getZdbID() + " of type " + pub.getType() + " for gene " + gene.getAbbreviation());
            return;
        }
        if (infrastructureRepository.getRecordAttribution(gene.zdbID, pub.getZdbID(), RecordAttribution.SourceType.STANDARD) == null) {
            System.out.println("Adding attribution for gene " + gene.getAbbreviation() + " for publication " + pub.getZdbID() + " based on feature " + feature.getAbbreviation());
            writeCsvLine(gene, pub, feature, csvWriter);
            if (dryRun) {
                return;
            }
            infrastructureRepository.insertRecordAttribution(gene.zdbID, pub.getZdbID());
            infrastructureRepository.insertUpdatesTable(gene.zdbID, "record attribution", pub.getZdbID(), "Added direct attribution to gene related to feature");
        }
    }

    private static void writeCsvLine(Marker gene, Publication pub, Feature feature, BufferedWriter writer) {
        try {
            writer.write(gene.getZdbID() + "," + pub.getZdbID() + "," + feature.getZdbID() + "," + gene.getAbbreviation() + "," + feature.getAbbreviation());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Publication> getPublicationIdsForFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select p.publication 
             from PublicationAttribution p 
             where p.dataZdbID = :featureZdbID 
            """;
        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameter("featureZdbID", feature.getZdbID());
        List<Publication> resultList = query.list();
        return new TreeSet<>(resultList);
    }
}
