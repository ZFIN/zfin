package org.zfin.uniquery;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.zfin.infrastructure.ActiveData;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.webrootDirectory;
import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.workingDirectoryOption;
import static org.zfin.uniquery.IndexerCommandLineOptions.log4jFileOption;
import static org.zfin.uniquery.IndexerCommandLineOptions.numberOfDetailPagesOption;

/**
 * This class generates the list of urls of ZFIN's detail pages.
 */
public class GenerateEntityDetailPageUrls extends AbstractScriptWrapper {

    private static Logger LOG;

    static {
        options.addOption(numberOfDetailPagesOption);
        options.addOption(log4jFileOption);
        options.addOption(webrootDirectory);
        options.addOption(workingDirectoryOption);
    }

    private List<String> allUrls = new ArrayList<String>(100);
    private static final String entityMappingFileName = "entity-mapping.properties";
    private static final String outputFileName = "allDetailPages.txt";
    private CompositeConfiguration entityUrlMapping;
    private int numberOfRecordsPerEntities = 0;
    private static final String pageClassMappingFile = "entities-to-be-indexed.txt";

    public GenerateEntityDetailPageUrls() {
    }

    public void setProperties(String propertyDirectory, String rootIndexerDirectory) throws IOException {
        if (propertyDirectory == null) {
            initAll();
        } else {
            propertyDirectory += "/WEB-INF/zfin.properties";
            initAll(propertyDirectory);
        }
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue(propertyDirectory);
        cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        setDetailEntityProperties( rootIndexerDirectory, propertyDirectory);
    }

    void setDetailEntityProperties(String rootIndexerDirectory, String propertyDirectory) {
        rootDirectory = rootIndexerDirectory;
        File mappingFile = new File(rootIndexerDirectory, entityMappingFileName);
        File pageClasses = new File(rootIndexerDirectory, pageClassMappingFile);
        entityUrlMapping = new CompositeConfiguration();
        entityUrlMapping.addConfiguration(new SystemConfiguration());
        try {
            entityUrlMapping.addConfiguration(new PropertiesConfiguration(propertyDirectory));
            entityUrlMapping.addConfiguration(new PropertiesConfiguration(mappingFile.getAbsolutePath()));
            entityUrlMapping.addConfiguration(new PropertiesConfiguration(pageClasses.getAbsolutePath()));
        } catch (ConfigurationException e) {
            LOG.error("error during configuration file initialization");
        }
    }

    public void setNumberOfRecordsPerEntities(int numberOfRecordsPerEntities) {
        this.numberOfRecordsPerEntities = numberOfRecordsPerEntities;
    }

    private String rootDirectory;
    /**
     * Main entry point when generating the list without indexing them.
     *
     * @param arguments arguments
     */
    public static void main(String arguments[]) {
        LOG = Logger.getLogger(GenerateEntityDetailPageUrls.class);
        LOG.info("Start Ontology Loader class: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "create detail page url");
        initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        String propertyFileName = commandLine.getOptionValue(webrootDirectory.getOpt());
        String numberOfEntities = commandLine.getOptionValue(numberOfDetailPagesOption.getOpt());
        String rootIndexerDirectory = commandLine.getOptionValue(workingDirectoryOption.getOpt());
        LOG.info("Generate entity detail page URLs with upper limit: " + numberOfEntities);

        File configurationFile = new File(rootIndexerDirectory, "indexer.xml");
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("file:"+configurationFile.getAbsolutePath());
        GenerateEntityDetailPageUrls generateUrls = (GenerateEntityDetailPageUrls) context.getBean("detailPageGenerator");
        try {
            generateUrls.setProperties(propertyFileName, rootIndexerDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        generateUrls.numberOfRecordsPerEntities = Integer.parseInt(numberOfEntities);
        generateUrls.generateAllUrls();
        LOG.info("Property: " + ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL.value());
        generateUrls.saveUrlsInfile();
        LOG.warn("Generated " + generateUrls.allUrls.size() + " urls");
    }

    public void saveUrlsInfile() {
        try {
            saveUrlsInFile(getDetailPageFilePath());
        } catch (FileNotFoundException e) {
            LOG.error("Error during file output creation");
        }
    }

    private String fullDetailPagePath;

    public String getDetailPageFilePath() {
        return FileUtil.createAbsolutePath(rootDirectory, "etc", outputFileName);
    }

    private void saveUrlsInFile(String fileName) throws FileNotFoundException {
        File outputFile = new File(fileName);
        PrintWriter fos = null;
        fos = new PrintWriter(outputFile);
        for (String url : allUrls)
            fos.println(url);
        fos.flush();
        fos.close();
    }

    public void generateAllUrls() {
        boolean isMoreEntities = true;
        int index = 1;
        while (isMoreEntities) {
            String entity = entityUrlMapping.getString(String.valueOf(index++));
            if (entity == null) {
                isMoreEntities = false;
                continue;
            }
            String className = entity + "IdList";
            boolean foundEntityClass = false;
            for (EntityIdList entityIdList : entitiesIdListCollection) {
                entityIdList.setEntityUrlMapping(entityUrlMapping);
                if (entityIdList.getClass().getSimpleName().equals(className)) {
                    allUrls.addAll(entityIdList.getUrlList(numberOfRecordsPerEntities));
                    foundEntityClass = true;
                }
            }
            if (!foundEntityClass)
                LOG.warn("Could not find class to create detail page list: " + className);
        }
        saveUrlsInfile();
    }

    private void generateTerms() {
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        List<String> allTerms = ontologyRepository.getAllTerms(numberOfRecordsPerEntities);
        convertIdsIntoUrls(allTerms);
    }

    private void convertIdsIntoUrls(List<String> allMarkers) {
        for (String id : allMarkers) {
            ActiveData.Type type = ActiveData.validateID(id);

            if (type == null)
                throw new RuntimeException("No active Data entity found for " + id);
            String url = entityUrlMapping.getString(type.name());
            if (url == null) {
                LOG.info("No url mapping found for id " + id);
                continue;
            }
            if (!url.endsWith("="))
                url += "=";
            if (!url.startsWith("/"))
                url += "/";
            allUrls.add(ZfinPropertiesEnum.NON_SECURE_HTTP.toString() + ZfinPropertiesEnum.DOMAIN_NAME + url + id);
        }
    }

    private Collection<AbstractEntityIdList> entitiesIdListCollection;

    @Autowired
    public void setEntitiesIdListCollection(Collection<AbstractEntityIdList> entitiesIdListCollection) {
        this.entitiesIdListCollection = entitiesIdListCollection;
    }

}
