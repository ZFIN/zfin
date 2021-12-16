package org.zfin.feature.repair;

import org.apache.log4j.*;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zfin.marker.service.MarkerAttributionService.getTargetedGenes;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

public class FeatureAttributionRepair extends AbstractScriptWrapper {

    private static final Level logLevel = Level.INFO;
    private BufferedWriter outputWriter;
    private Logger logger;
    private static final String DEFAULT_OUTPUT_PATH = "FeatureAttributionRepairReport.log";
    private static final String DEFAULT_SQL_OUTPUT_PATH = "FeatureAttributionRepairReport.sql";
    private HashMap<String, Boolean> cachedEntries;

    public static void main(String[] args) {
        FeatureAttributionRepair repair = new FeatureAttributionRepair();
        try {
            repair.scanThroughPublicationsAndOutputFixes();
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file.");
        }
        System.exit(0);
    }

    public FeatureAttributionRepair() {
        initAll();
        initLogger();
        initCache();
        initSqlWriter();
    }

    private void initCache() {
        cachedEntries = new HashMap<String, Boolean>();
    }

    private void initSqlWriter() {
        String outputPath = System.getProperty("sqlFile", DEFAULT_SQL_OUTPUT_PATH);
        if (outputPath.equals("")) {
            //Seems we get empty string if property isn't specified. Maybe due to gradle configuration in console.gradle?
            outputPath = DEFAULT_SQL_OUTPUT_PATH;
        }

        outputWriter = null;
        try {
            outputWriter = new BufferedWriter(new FileWriter(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void scanThroughPublicationsAndOutputFixes() throws IOException {
        List<Publication> publications = getPublications();
        publications = filterToPublicationsBefore(publications);
        publications = filterToPublicationsAfter(publications);

        logger.info("Processing " + publications.size() + " publications");

        int count = 0;
        for(Publication publication : publications) {
            count++;
            //one update per percent
            if (count % (publications.size() / 100) == 0) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                logger.info("" + ((count * 100) / publications.size()) + "% (" + publication.getZdbID() + ") at " + timestamp );
            }

            // get all alleles for the publication
            //   For  open publications with attributed features, genes with is_allele relationship should be directly attributed.
            List<Feature> features = getFeatureRepository().getFeaturesByPublication(publication.getZdbID());
            for(Feature feature : features) {
                Set<FeatureMarkerRelationship> featureMarkerRelations = feature.getFeatureMarkerRelations();
                for(FeatureMarkerRelationship relationship : featureMarkerRelations) {
                    if (relationship.getType() == FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF) {
                        attributeGenesToPublication(publication, Collections.singleton(relationship.getMarker()), feature.getAbbreviation(), feature.getZdbID(), "through is_allele_of relationship");
                    } else {
                        logger.debug("Skipping attribution due to relationship being: " + relationship.getType() + " " + relationship.getMarker().getZdbID() + " and " + feature.getDisplayAbbreviation());
                    }
                }
            }

            // get all markers for the publication
            List<Marker> attributedStrs = getPublicationRepository().getSTRByPublication(publication.getZdbID());
            for(Marker marker : attributedStrs) {
                Set<Marker> targetedGenes = new HashSet<>(getTargetedGenes(marker));
                //   For open publications, STRs with >1  target relationship should be ignored
                if (targetedGenes.size() == 1) {
                    attributeGenesToPublication(publication, targetedGenes, marker.getAbbreviation(), marker.getZdbID(), " is STR with one targeted gene");
                } else if (targetedGenes.size() > 1) {
                    logger.debug("Skipping attribution due to STR targeting multiple genes: " + marker.getAbbreviation() );
                } else {
                    logger.debug("Skipping attribution due to STR targeting zero genes?  " + marker.getAbbreviation() );
                }
            }

        }
    }

    private List<Publication> getPublications() {
        return getPublicationRepository().getAllOpenPublicationsOfJournalType(PublicationType.JOURNAL);
    }

    private List<Publication> filterToPublicationsBefore(List<Publication> publications) {

        //check for filter
        String cutoff = System.getProperty("publicationsBefore", "");
        if (cutoff.equals("")) {
            logger.info("No filter for publicationsBefore");
            return publications;
        }

        //check the cutoff matches expected regex
        String patternString = "^ZDB-PUB-\\d+";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(cutoff);
        if (!matcher.find()) {
            logger.error("Bad pattern for publicationsBefore. Expected ZDB-PUB-... , got: " + cutoff + ". Running without filter");
            return publications;
        }

        logger.info("Filtering on publications before or equal to " + cutoff + ".");
        List<Publication> filteredPublications = new ArrayList<>();
        for(Publication publication : publications) {
            if (publication.getZdbID().compareToIgnoreCase(cutoff) <= 0) {
                filteredPublications.add(publication);
            }
        }

        return filteredPublications;
    }

    private List<Publication> filterToPublicationsAfter(List<Publication> publications) {

        //check for filter
        String cutoff = System.getProperty("publicationsAfter", "");
        if (cutoff.equals("")) {
            logger.info("No filter for publicationsAfter");
            return publications;
        }

        //check the cutoff matches expected regex
        String patternString = "^ZDB-PUB-\\d+";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(cutoff);
        if (!matcher.find()) {
            logger.error("Bad pattern for publicationsAfter. Expected ZDB-PUB-... , got: " + cutoff + ". Running without filter");
            return publications;
        }

        logger.info("Filtering on publications after or equal to " + cutoff + ".");
        List<Publication> filteredPublications = new ArrayList<>();
        for(Publication publication : publications) {
            if (publication.getZdbID().compareToIgnoreCase(cutoff) >= 0) {
                filteredPublications.add(publication);
            }
        }

        return filteredPublications;
    }

    private void attributeGenesToPublication(Publication publication, Set<Marker> targetedGenes, String intermediaryAbbreviation, String intermediaryID, String reason) throws IOException {
        for(Marker gene : targetedGenes) {
            String hashKey = publication.getZdbID() + "," + gene.getZdbID();
            if (cachedEntries.containsKey(hashKey)) {
                continue;
            }
            cachedEntries.put(hashKey, true);
            if (getInfrastructureRepository().getRecordAttribution(gene.zdbID, publication.getZdbID(), RecordAttribution.SourceType.STANDARD) == null) {
                logger.info("Add to publication " + publication.getZdbID() + " direct attribution of " + gene.getZdbID() + " through intermediary " + intermediaryAbbreviation + "(" + intermediaryID + "): " + reason );
                outputWriter.write("INSERT INTO updates (submitter_id,rec_id,field_name,new_value,comments,submitter_name) " +
                                "SELECT 'ZDB-PERS-210917-1','" + gene.getZdbID() + "','record attribution','" + publication.getZdbID() + "'," + "'Added direct attribution by intermediary " + intermediaryID + " " + reason + "','Ryan Taylor (ZFIN-7704)' WHERE NOT EXISTS ( select 'x' from updates where " +
                                "submitter_id='ZDB-PERS-210917-1' and rec_id='" + gene.getZdbID() + "' and field_name='record attribution' and new_value = '" + publication.getZdbID() + "' );\n"
                );
                outputWriter.write("INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) VALUES ('" + gene.getZdbID() + "', '" + publication.getZdbID() + "', 'standard') ON CONFLICT DO NOTHING;\n");
                outputWriter.flush();
            }
        }
    }

    //TODO: replace this with proper xml configuration
    private void initLogger() {
        logger = Logger.getLogger(FeatureAttributionRepair.class);

        String pattern = "%d [%p] %m%n";

        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        console.setLayout(new PatternLayout(pattern));
        console.setThreshold(logLevel);
        console.activateOptions();
        //add appender to any Logger (here is root)
        logger.addAppender(console);

        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(DEFAULT_OUTPUT_PATH);
        fa.setLayout(new PatternLayout(pattern));
        fa.setThreshold(logLevel);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        logger.addAppender(fa);
    }

}



