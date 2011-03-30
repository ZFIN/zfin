package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.presentation.OntologyLoadingEntity;
import org.zfin.util.DateUtil;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * A helper class that provides methods that format anatomy items or
 * developmental stage objects according to certain presentation rules.
 */
public final class OntologySerializationService {

    // name of serialized file.
    public static final String SERIALIZED_LOOKUP_SUFFIX = "-lookup.ser";
    public static final String LOADING_STATS_SER = "loading-statistics.ser";
    private static final double MILLISECONDS_PER_SECOND = 1000.0;

    private static final Logger LOGGER = Logger.getLogger(OntologySerializationService.class);
    private OntologyManager ontologyManager;

    public OntologySerializationService(OntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }

    /*
    * Here, we serialize 2 objects
    *
    * @param ontology Ontology.
    */
    private void serializeOntologyInternal(Ontology ontology) {
        long start = System.currentTimeMillis();
        File lookupFile = FileUtil.serializeObject(ontologyManager.getOntologyMap().get(DTOConversionService.convertToOntologyDTO(ontology)),
                FileUtil.createOntologySerializationFile(ontology.name() + SERIALIZED_LOOKUP_SUFFIX));
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;

        LOGGER.info("Time to serialize ontology[" + ontology.name() + "]: " + time + " seconds.");
        LOGGER.info("Lookup file path[" + lookupFile.getAbsolutePath() + "] size: " + (lookupFile.length() / 1024) + "kB");
    }

    public void serializeOntology(final Ontology ontology) {
        new Thread() {
            @Override
            public void run() {
                serializeOntologyInternal(ontology);
            }
        }.start();
    }

    public void serializeObject(Object object, String fileName) {
        if (object == null || fileName == null)
            return;

        long start = System.currentTimeMillis();
        File file = FileUtil.serializeObject(object,
                FileUtil.createOntologySerializationFile(fileName));
        long end = System.currentTimeMillis();
        double time = (double) (end - start) / MILLISECONDS_PER_SECOND;
        LOGGER.info("Time to serialize " + fileName + " [" + object.getClass().getSimpleName() + "]: " + time + " seconds.");
        LOGGER.info("Lookup file path [" + file.getAbsolutePath() + "] size: " + (file.length() / 1024) + "kB");
    }

    public void serializeLoadData(Map<OntologyDTO, OntologyLoadingEntity> loadingData) {
        serializeObject(loadingData, LOADING_STATS_SER);
    }

    private Object deserializeFile(String fileName) throws Exception {
        Object entity;
        long start = System.currentTimeMillis();
        File lookupFile;
        try {
            lookupFile = FileUtil.createOntologySerializationFile(fileName);
            if (!lookupFile.exists() || !lookupFile.canRead()) {
                throw new IOException("Lookup file does not exist or has bad permissions: " + lookupFile.getAbsolutePath());
            }
            LOGGER.info("Lookup file: " + lookupFile + " size:" + lookupFile.length() + " last modified: " + new Date(lookupFile.lastModified()));
            entity = FileUtil.deserializeOntologies(lookupFile);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize the files", e);
            throw e;
        }
        LOGGER.info("Time to deserialize ontology[" + fileName + "]: " + DateUtil.getTimeDuration(start));
        return entity;
    }


    @SuppressWarnings({"unchecked"})
    public void deserializeOntology(Ontology ontology) throws Exception {
        PatriciaTrieMultiMap<TermDTO> lookupMap = (PatriciaTrieMultiMap<TermDTO>) deserializeFile(ontology.name() + SERIALIZED_LOOKUP_SUFFIX);
        lookupMap.rebuild();
        ontologyManager.getOntologyMap().remove(ontology);
        ontologyManager.getOntologyMap().put(DTOConversionService.convertToOntologyDTO(ontology), lookupMap);
    }

    @SuppressWarnings({"unchecked"})
    void deserializeLoadingStatistic() throws Exception {
        ontologyManager.setLoadingData((Map<OntologyDTO, OntologyLoadingEntity>) deserializeFile(LOADING_STATS_SER));
    }

}


