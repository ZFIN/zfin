package org.zfin.ontology.datatransfer.service;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.obo.dataadapter.AbstractParseEngine;
import org.obo.dataadapter.DefaultOBOParser;
import org.obo.dataadapter.OBOParseEngine;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.*;
import org.obo.history.SessionHistoryList;
import org.zfin.ontology.OntologyMetadata;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.*;


/**
 * Class to validate ontologies, in particular for non-ASCII characters.
 * Command line options:
 * -oboFile <obo file name>
 * -log4jFilename <log4j.xml file>
 * -dbScriptFileName <*.sql file>
 * -propertyFile <path to zfin.properties location>
 * <p/>
 * This class assumes the latest obo file has already been downloaded from the corresponding site.
 * See {@link org.zfin.ontology.datatransfer.DownloadOntology}.
 */
public class OntologyValidation extends AbstractScriptWrapper {

    private static Logger LOG;

    static {
        options.addOption(oboFileNameOption);
        options.addOption(log4jFileOption);
        options.addOption(webrootDirectory);
    }

    private OBOSession oboSession;
    private String oboFilename;
    private CronJobReport report;
    // this attribute holds all the data that need to be imported into the database,
    // i.e. new terms, synonyms etc.. They have a key that is used (referred to) in the db script file
    // Currently, all the data need to be of type string!
    // There are about 20 different types of data set to be imported.
    private Map<String, List<List<String>>> dataMap = new HashMap<String, List<List<String>>>(20);
    private OntologyMetadata oboMetadata;

    public OntologyValidation(String oboFile, String propertyDirectory) throws IOException {
        initializeLoad(oboFile);
        if (propertyDirectory == null)
            initProperties();
        else
            ZfinProperties.init(propertyDirectory + "/WEB-INF/zfin.properties");
        if (propertyDirectory == null)
            throw new RuntimeException("No property file found.");
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue(propertyDirectory);
        cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
    }

    private void initializeLoad(String oboFile) throws IOException {
        oboFilename = oboFile;
        if (!FileUtil.checkFileExists(oboFilename))
            throw new IOException("No OBO file <" + oboFile + "> found. You may have to download the obo file first.");
    }

    public static void main(String[] arguments) {
        LOG = LogManager.getLogger(OntologyValidation.class);
        LOG.info("Start Ontology Loader class: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
//        initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        String oboFile = commandLine.getOptionValue(oboFileNameOption.getOpt());
        String propertyFileName = commandLine.getOptionValue(webrootDirectory.getOpt());
        LOG.info("Loading obo file: " + oboFile);

        OntologyValidation loader = null;
        try {
            loader = new OntologyValidation(oboFile, propertyFileName);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.exit(-1);
        }
        LOG.info("Property: " + ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL.value());
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        loader.initialize(oboFile, cronJobUtil);
        loader.processOboFile();
        loader.checkForNonAsciiCharacters();
    }

    private void checkForNonAsciiCharacters() {
        String g = ".  The skeleton consists of consists of supporting endochondral radials and dermal fin rays or lepidotrichia.";
        for (List<String> term : dataMap.get(UnloadFile.TERM_PARSED.getValue())) {
            int index = 0;
            String termID = null;
            for (String attribute : term) {
                index++;
                if (index == 1)
                    termID = attribute;
                char[] characters = attribute.toCharArray();
                int position = 1;
                for (char character : characters) {
                    if (character > 128) {
                        LOG.info(attribute);
                        System.out.println("Term ID: " + termID);
                        System.out.println("Attribute: " + attribute);
                        System.out.println("Character <" + character + "> at position " + position + " with code " + (int) character + " is beyond 7-bit ASCII code.");
                    }
                    position++;
                }
            }
        }
    }

    public boolean initialize(String fileName, CronJobUtil cronJobUtil) {
        return initialize();
    }

    public boolean initialize() {
        try {
            createOboSession();
        } catch (OBOParseException e) {
            LOG.error("Error while parsing Obo file", e);
            return false;
        } catch (IOException e) {
            LOG.error("No obo file found", e);
            return false;
        }
        return true;
    }

    private boolean processOboFile() {
        parseOboFile();
        return true;
    }

    private void parseOboFile() {
        int numberOfTerms = 0;
        for (IdentifiedObject obj : oboSession.getObjects()) {
            if (obj instanceof OBOClass) {
                OBOClass term = (OBOClass) obj;
                if (!term.getID().startsWith("obo:")) {
                    numberOfTerms++;
                    pushToParsedTerms(term);
                    if (term.isObsolete()) {
                        appendFormattedRecord(UnloadFile.TERM_OBSOLETE, term.getID());
                    }
                    if (term.getSecondaryIDs() != null) {
                        for (String secondaryID : term.getSecondaryIDs())
                            appendFormattedRecord(UnloadFile.TERM_SECONDARY, term.getID(), secondaryID);
                    }
                    for (Link parentTerm : term.getParents()) {

                        appendFormattedRecord(UnloadFile.TERM_RELATIONSHIPS, parentTerm.getParent().getID(), term.getID(), parentTerm.getType().getName());
                    }
                    if (term.getReplacedBy() != null) {
                        for (ObsoletableObject replacedByID : term.getReplacedBy())
                            appendFormattedRecord(UnloadFile.TERM_REPLACED, replacedByID.getID(), term.getID(), "replaced_by");
                    }
                    if (term.getSynonyms() != null) {
                        for (Synonym synonym : term.getSynonyms())
                            appendSynonym(UnloadFile.TERM_SYNONYMS, term.getID(), synonym.getScope(), synonym.getText());
                    }
                    if (term.getConsiderReplacements() != null) {
                        for (ObsoletableObject considerObject : term.getConsiderReplacements())
                            appendFormattedRecord(UnloadFile.TERM_CONSIDER, term.getID(), considerObject.getID(), "consider");
                    }
                    if (term.getDbxrefs() != null) {
                        for (Dbxref dbxref : term.getDbxrefs()) {
                            appendFormattedRecord(UnloadFile.TERM_XREF, term.getID(), dbxref.getDatabase(), dbxref.getDatabaseID(), "xref");
                        }
                    }
                    if (term.getSubsets() != null) {
                        for (TermSubset subset : term.getSubsets()) {
                            appendFormattedRecord(UnloadFile.TERM_SUBSET, term.getID(), subset.getName(), "subset");
                        }
                    }
                }
            }
        }
        if (oboSession.getSubsets() != null) {
            for (TermSubset term : oboSession.getSubsets())
                appendFormattedRecord(UnloadFile.SUBSETDEFS_HEADER, oboSession.getDefaultNamespace().getID(), term.getName(), term.getDesc(), "subsetdefs");
        }
        if (oboSession.getSynonymTypes() != null) {
            for (SynonymType synonymType : oboSession.getSynonymTypes()) {
                appendFormattedRecord(UnloadFile.SYNTYPEDEFS_HEADER, oboSession.getDefaultNamespace().getID(), synonymType.getID(), synonymType.getName(), getSynonymDescriptor(synonymType.getScope()), "syntypedefs");
            }
        }
        String message = "Number of Terms in obo file: " + numberOfTerms;
        LOG.info(message);
    }

    private void appendSynonym(UnloadFile unloadFile, String id, int scope, String text) {
        String type = getSynonymDescriptor(scope);
        appendFormattedRecord(unloadFile, id, text, type, "[]", "synonym");
    }

    private String getSynonymDescriptor(int scope) {
        String type = null;
        switch (scope) {
            case 0:
                type = "RELATED";
                break;
            case 1:
                type = "EXACT";
                break;
            case 2:
                type = "NARROW";
                break;
            case 3:
                type = "BROAD";
                break;
            default:
                type = "UNKNOWN SYNONYM";
        }
        return type;
    }

    private void appendRelationship(UnloadFile unloadFile, String idOne, String idTwo, String relationshipName) {
        int indexOfArrow = idOne.indexOf("->");
        String convertFirstId = idOne.substring(indexOfArrow + 2);
        appendFormattedRecord(unloadFile, convertFirstId, idTwo, relationshipName);
    }

    private void pushToParsedTerms(OBOClass term) {
        String obsolete = "";
        if (term.isObsolete())
            obsolete = "t";
        appendFormattedRecord(UnloadFile.TERM_PARSED, term.getID(),
                term.getName(), term.getNamespace().getID(), term.getDefinition(), term.getComment(), obsolete);
    }

    private void createOboSession() throws OBOParseException, IOException {
        DefaultOBOParser oboParser = new DefaultOBOParser();
        AbstractParseEngine engine = new OBOParseEngine(oboParser);
        // GOBOParseEngine can parse several files at once
        // and create one munged-together ontology,
        // so we need to provide a Collection to the setPaths() method
        Collection<String> paths = new LinkedList<String>();
        paths.add(oboFilename);
        engine.setPaths(paths);
        engine.parse();
        oboSession = oboParser.getSession();
    }

    private OntologyMetadata getMetadataFromOboFile() {
        OntologyMetadata metadata = new OntologyMetadata();
        SessionHistoryList header = oboSession.getCurrentHistory();
        metadata.setDataVersion(header.getVersion());
        metadata.setSavedBy(header.getUser());
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        metadata.setDate(df.format(header.getDate()));
        metadata.setDefaultNamespace(oboSession.getDefaultNamespace().getID());
        metadata.setName(oboSession.getDefaultNamespace().getID());
        String revision = getRevisionFromComment(header.getComment());
        metadata.setDataVersion(revision);
        return metadata;
    }

    /**
     * Check for revision from gene_ontology file which is
     * part of the comment field
     * e.g.: cvs version: $Revision: 1.1416 $
     *
     * @param comment string
     * @return revision number
     */
    public static String getRevisionFromComment(String comment) {
        if (comment == null)
            return null;
        String revName = "$Revision: ";
        int startOfRevision = comment.indexOf(revName);
        if (startOfRevision == -1)
            return null;
        String revision = comment.substring(startOfRevision + revName.length());
        int endOfRevision = revision.indexOf("$");
        return revision.substring(0, endOfRevision).trim();
    }

    public void setLogger(Logger log) {
        LOG = log;
    }

    enum UnloadFile {
        NEW_ALIASES("newAliases"),
        TERM_PARSED("term_parsed.unl"),
        TERM_CONSIDER("term_consider.unl"),
        TERM_RELATIONSHIPS("term_relationships.unl"),
        TERM_REPLACED("term_replaced.unl"),
        TERM_SECONDARY("term_secondary.unl"),
        SECONDARY_TERMS_USED("secondary_terms_used.unl"),
        TERM_OBSOLETE("term_obsolete.unl"),
        TERM_SYNONYMS("term_synonyms.unl"),
        TERM_SUBSET("term_subset.unl"),
        TERM_XREF("term_xref.unl"),
        ONTOLOGY_HEADER("ontology_header.unl"),
        TERMS_MISSING_OBO_ID("terms_missing_obo_id.txt"),
        TERMS_UN_OBSOLETED_ID("terms_un_obsoleted.txt"),
        NEW_TERMS("new_terms.unl"),
        UPDATED_TERMS("updated_terms.unl"),
        EXPRESSION_SUPERTERM_UPDATES("expression-superterm-updates.unl"),
        EXPRESSION_SUBTERM_UPDATES("expression-subterm-updates.unl"),
        PHENOTYPE_SUPERTERM_UPDATES("phenotype-superterm-updates.unl"),
        PHENOTYPE_SUBTERM_UPDATES("phenotype-subterm-updates.unl"),
        PHENOTYPE_RELATED_ENITTY_SUPERTERM_UPDATES("phenotype-related-entity-superterm-updates.unl"),
        PHENOTYPE_RELATED_ENITTY_SUBTERM_UPDATES("phenotype-related-enityt-subterm-updates.unl"),
        PHENOTYPE_QUALITY_UPDATES("phenotype-quality-updates.unl"),
        SYNTYPEDEFS_HEADER("syntypedefs_header.unl"),
        SEC_UNLOAD_REPORT("sec_unload_report.unl"),
        SEC_UNLOAD("sec_unload.unl"),
        MODIFIED_TERM_NAMES("modified_term_names.unl"),
        MODIFIED_TERM_DEFINITIONS("modified_term_definitions.unl"),
        MODIFIED_TERM_COMMENTS("modified_term_comments.unl"),
        SUBSETDEFS_HEADER("subsetdefs_header.unl");
        private String value;

        UnloadFile(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private Map<UnloadFile, FileWriter> fileWriters = new HashMap<UnloadFile, FileWriter>(UnloadFile.values().length);

    private void appendFormattedRecord(UnloadFile unloadFile, String... record) {
        //appendRecord(unloadFile, InformixUtil.getUnloadRecord(record));
        List<List<String>> data = dataMap.get(unloadFile.getValue());
        if (data == null) {
            data = new ArrayList<List<String>>();
        }
        List<String> individualRecord = new ArrayList<String>(record.length);
        individualRecord.addAll(Arrays.asList(record));
        data.add(individualRecord);
        dataMap.put(unloadFile.getValue(), data);
    }
}
