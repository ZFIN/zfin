package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.support.StringMultipartFileEditor;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.*;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class provides a jsp front-end to the cgi-blast, replacing blast.cgi.
 * Here are the cases we have to handle:
 * 1. submit from externalBlastDropDown (very few cases?).  May go straight to post, but other behavior is preferred.
 * 2. submit from externalAccessionBlastDropDown
 * 3. view new blast page (ie, someone clicks on the blast link)
 * 4. submit blast
 * <p/>
 * 5. view old blast via edit and resubmit
 */
@Controller
public class XMLBlastController {

    @ModelAttribute("formBean")
    private XMLBlastBean getDefaultSearchFormPrevious(HttpServletRequest request) throws JAXBException, FileNotFoundException {
        String ticket = request.getParameter("previousSearch");
        if (ticket != null) {
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            XMLBlastBean xmlBlastBean = new XMLBlastBean();
            xmlBlastBean.setTicketNumber(ticket);
            BlastOutput blastOutput = (BlastOutput) jc.createUnmarshaller().unmarshal(new FileInputStream(System.getProperty("java.io.tmpdir") + "/" + xmlBlastBean.getResultFile()));
            xmlBlastBean = BlastResultMapper.createBlastResultBean(blastOutput).getXmlBlastBean();
            return xmlBlastBean;
        } else
            return new XMLBlastBean();
    }

    @RequestMapping(value = "/blast/blast", method = RequestMethod.GET)
    protected String showBlastSearchPage(@RequestParam(required = false) String previousSearch,
                                         @ModelAttribute("formBean") XMLBlastBean xmlBlastBean,
                                         Model model) throws Exception {
        // if no database selected, then select RNA/CDNA
        if (StringUtils.isEmpty(xmlBlastBean.getDataLibraryString())) {
            Database defaultDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.RNASEQUENCES);
            xmlBlastBean.setDataLibraryString(defaultDatabase.getAbbrev().toString());
        }

        if (previousSearch == null) {
//        // set defaults here
            if (xmlBlastBean.getExpectValue() == null) {
                xmlBlastBean.setExpectValue(1E-25);
            }
            if (xmlBlastBean.getSequenceType() == null ||
                    XMLBlastBean.SequenceType.NUCLEOTIDE == XMLBlastBean.SequenceType.getSequenceType(xmlBlastBean.getSequenceType())) {
                xmlBlastBean.setWordLength(11);
                xmlBlastBean.setDust(true);
                xmlBlastBean.setPoly_a(true);
                xmlBlastBean.setSeg(false);
                xmlBlastBean.setXnu(false);
                xmlBlastBean.setSequenceType(XMLBlastBean.SequenceType.NUCLEOTIDE.getValue());
            } else {
                xmlBlastBean.setWordLength(3);
                xmlBlastBean.setMatrix("BLOSUM62");
                xmlBlastBean.setDust(false);
                xmlBlastBean.setPoly_a(false);
                xmlBlastBean.setSeg(true);
                xmlBlastBean.setXnu(true);
                xmlBlastBean.setSequenceType(XMLBlastBean.SequenceType.PROTEIN.getValue());
            }

            if (StringUtils.contains(xmlBlastBean.getDataLibraryString(), "MicroRNA")
                    || StringUtils.contains(xmlBlastBean.getDataLibraryString(), "miRNA")
                    || StringUtils.contains(xmlBlastBean.getDataLibraryString(), "zfin_microRNA")
                    || StringUtils.contains(xmlBlastBean.getDataLibraryString(), "zfin_mrph")
                    || StringUtils.contains(xmlBlastBean.getDataLibraryString(), "zfin_talen")
                    || StringUtils.contains(xmlBlastBean.getDataLibraryString(), "zfin_crispr")) {
                //these values should match blast.js
                xmlBlastBean.setShortAndNearlyExact(true);
                xmlBlastBean.setExpectValue(1000d);
                xmlBlastBean.setWordLength(7);
                xmlBlastBean.setDust(false);
                xmlBlastBean.setPoly_a(false);
                xmlBlastBean.setXnu(false);
                xmlBlastBean.setSeg(false);
                xmlBlastBean.setXnu(false);
            }
        }

        setDatabases(xmlBlastBean);

        model.addAttribute("sequenceTypes", XMLBlastBean.SequenceType.values());
        model.addAttribute("matrices", XMLBlastBean.Matrix.values());
        model.addAttribute("programs", XMLBlastBean.Program.values());
        return "blast-setup.page";
    }

    @Autowired
    private XMLBlastValidator xmlBlastValidator;

    @InitBinder
    protected void initBinder(ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(String.class, "sequenceFile", new StringMultipartFileEditor());
    }

    protected void bindBySequenceID(XMLBlastBean xmlBlastBean, BindingResult errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sequenceID", "Missing sequence ID.", "Missing sequence ID.");
        if (errors.hasErrors()) {
            xmlBlastBean.setQuerySequence(null);
            xmlBlastBean.setSequenceFile(null);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String sequenceID : xmlBlastBean.getSequenceID().split(",")) {
            sequenceID = sequenceID.trim();
            sequenceID = sequenceID.replaceFirst("\\..*", "");
            List<Sequence> sequences =
                    MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs(sequenceID);
            // If not found in ZFIN go out to NCBI
            if (CollectionUtils.isEmpty(sequences))
                sequences = MultipleBlastServerService.getAccessionFromNcbi(sequenceID, xmlBlastBean.getSequenceType().equals("nt") ? NCBIEfetch.Type.NUCLEOTIDE : NCBIEfetch.Type.POLYPEPTIDE);
            sequences = MountedWublastBlastService.getInstance().filterUniqueSequences(sequences);

            if (CollectionUtils.isEmpty(sequences)) {
                errors.rejectValue("sequenceID", "code", "No sequences found for: " + sequenceID);
            } else {
                for (Sequence sequence : sequences) {
                    // we can assume that this is always FASTA so we can prepend if necessary
                    sb.append(prependSequenceWithDefline(sequence.getFormattedData()) + "\n");
                }
            }
        }
        xmlBlastBean.setQuerySequence(sb.toString());
    }

    protected void bindByUpload(XMLBlastBean xmlBlastBean, BindingResult errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sequenceFile", "Missing file.", "Missing file.");
        if (errors.hasErrors()) {
            xmlBlastBean.setQuerySequence(null);
            xmlBlastBean.setSequenceID(null);
            return;
        }
        String fileData = xmlBlastBean.getSequenceFile();
//        if(StringUtils.isNotEmpty(fileData)){
        logger.info("fileData is not null: " + fileData);
        fileData = MountedWublastBlastService.getInstance().removeLeadingNumbers(fileData, XMLBlastBean.SequenceType.getSequenceType(xmlBlastBean.getSequenceType()));
        fileData = prependSequenceWithDefline(fileData);
        // need to do a validation
        // clip off sequence file and/or validate against
        xmlBlastBean.setQuerySequence(fileData);
    }

    protected void bindBySequenceBox(XMLBlastBean xmlBlastBean, BindingResult errors) {
        if (errors.hasErrors()) {
            xmlBlastBean.setSequenceID(null);
            xmlBlastBean.setSequenceFile(null);
            return;
        }
        // fix query sequence
        xmlBlastBean.setQuerySequence(xmlBlastBean.getQuerySequence().trim());
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "querySequence", "Missing sequence.", "Missing sequence.");
        String querySequence = xmlBlastBean.getQuerySequence();
        querySequence = MountedWublastBlastService.getInstance().removeLeadingNumbers(querySequence, XMLBlastBean.SequenceType.getSequenceType(xmlBlastBean.getSequenceType()));
        querySequence = prependSequenceWithDefline(querySequence);
        xmlBlastBean.setQuerySequence(querySequence);
    }

    /**
     * This is run before validation.
     *
     * @param errors Validation errors.
     * @throws Exception Failure.
     */
    public void onBind(XMLBlastBean xmlBlastBean, BindingResult errors) throws Exception {

        xmlBlastBean.setSequenceType(XMLBlastBean.Program.getSequenceTypeForProgram(xmlBlastBean.getProgram()).getValue());

        if (xmlBlastBean.getQueryType().equals(XMLBlastBean.QueryTypes.SEQUENCE_ID.toString())) {
            bindBySequenceID(xmlBlastBean, errors);
        } else if (xmlBlastBean.getQueryType().equals(XMLBlastBean.QueryTypes.UPLOAD.toString())) {
            bindByUpload(xmlBlastBean, errors);
        }
        // if query sequence
        else if (xmlBlastBean.getQueryType().equals(XMLBlastBean.QueryTypes.FASTA.toString())) {
            bindBySequenceBox(xmlBlastBean, errors);
        }

        bindDatabases(xmlBlastBean, errors);
    }

    private void bindDatabases(XMLBlastBean inputXMLBlastBean, BindingResult errors) throws BlastDatabaseException {

        if (StringUtils.isEmpty(inputXMLBlastBean.getDataLibraryString())) {
            errors.rejectValue("dataLibraryString", "code", "Select sequence database.");
            return;
        }

        // Get the blast database string (from the database drop-down).
        // For the unauthorized users this is only ever one string (the blast abbreviation).
        // If an authorized user selects more than one database it will be comma separated and will need to be tokenized.
        StringTokenizer databaseAbbrevTokens = new StringTokenizer(inputXMLBlastBean.getDataLibraryString(), ",");

        // these are the non-empty databases based on the databaseTokens.  databaseTokens >= databaseTargets
        List<Database> databaseTargets = new ArrayList<>();

        logger.debug("# of tokens: " + databaseAbbrevTokens.countTokens());
        while (databaseAbbrevTokens.hasMoreTokens()) {
            String databaseAbbrevString = databaseAbbrevTokens.nextToken();
            logger.info("database string[" + databaseAbbrevString + "]");
            Database targetDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.getType(databaseAbbrevString));
            List<Database> actualDatabaseTargets = BlastPresentationService.getNonEmptyLeaves(targetDatabase);

            if (CollectionUtils.isNotEmpty(actualDatabaseTargets)) {
                for (Database databaseTarget : actualDatabaseTargets) {
                    try {
                        if (WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(databaseTarget).getNumSequences() > 0
                                && !databaseTargets.contains(databaseTarget)) {
                            databaseTargets.add(databaseTarget);
                        }
                    } catch (Exception e) {
                        logger.error("failed to get database statistics", e);
                    }
                }
            } else {
                try {
                    if (WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(targetDatabase).getNumSequences() > 0
                            &&
                            !databaseTargets.contains(targetDatabase)
                            ) {
                        databaseTargets.add(targetDatabase);
                    }
                } catch (Exception e) {
                    logger.error("failed to get database statistics", e);
                }
            }
        }

        if (CollectionUtils.isEmpty(databaseTargets)) {
            String errorString;
            int numDatabases = databaseAbbrevTokens.countTokens();
            if (numDatabases > 1) {
                errorString = "Selected databases have no sequence.";
            } else {
                errorString = "Selected database has no sequence.";
            }
            errors.rejectValue("dataLibraryString", "code", errorString);
        }
        inputXMLBlastBean.setActualDatabaseTargets(databaseTargets);
    }

    public static String prependSequenceWithDefline(String sequence) {
        if (sequence != null && !sequence.startsWith(">")) {
            sequence = ">single query\n" +
                    sequence;
        }
        return sequence;
    }

    private void setDatabases(XMLBlastBean xmlBlastBean) {
        boolean isRoot = Person.isCurrentSecurityUserRoot();
        xmlBlastBean.setNucleotideDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.NUCLEOTIDE, !isRoot, true));
        xmlBlastBean.setProteinDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.PROTEIN, !isRoot, true));
    }

    @RequestMapping(value = "/blast/blast", method = RequestMethod.POST)
    protected String initiateBlast(Model model,
                                   @ModelAttribute("formBean") XMLBlastBean inputXMLBlastBean,
                                   BindingResult result) throws Exception {
        onBind(inputXMLBlastBean, result);
        String querySequence = inputXMLBlastBean.getQuerySequence();
        StringReader in = new StringReader(querySequence);
        logger.debug("onsubmit enter");

        xmlBlastValidator.validate(inputXMLBlastBean, result);
        if (result.hasErrors()) {
            setDatabases(inputXMLBlastBean);
            model.addAttribute("programs", XMLBlastBean.Program.values());
            return "blast-setup.page";
        }
        // count the number of sequences put in so we can create the correct number of blast files
        BufferedReader bufferedReader = new BufferedReader(in);
        SymbolTokenization symbolTokenization;
        if (inputXMLBlastBean.getSequenceType() == null ||
                XMLBlastBean.SequenceType.NUCLEOTIDE == XMLBlastBean.SequenceType.getSequenceType(inputXMLBlastBean.getSequenceType())) {
            symbolTokenization = RichSequence.IOTools.getNucleotideParser();
        } else {
            symbolTokenization = RichSequence.IOTools.getProteinParser();
        }
        RichSequenceIterator iterator = RichSequence.IOTools.readFasta(bufferedReader, symbolTokenization, new SimpleNamespace("fasta-in"));
        // handle populating view object
        if (!iterator.hasNext()) {
            logger.error("no sequence read from query sequence in submit phase: \n" + querySequence);
            result.addError(new FieldError("", "", "No sequences given"));
        }


        // create resultXMLBlastBeans for each query sequence
        List<XMLBlastBean> resultXMLBlastBeans = new ArrayList<>();
        // for each sequence, split
        while (iterator.hasNext()) {
            RichSequence sequence = iterator.nextRichSequence();
            // create clone of first one
            XMLBlastBean individualXMLBlastBean = inputXMLBlastBean.clone();

            // create an individual temp file and set bean
            File blastResultFile = File.createTempFile(XMLBlastBean.BLAST_PREFIX, XMLBlastBean.BLAST_SUFFIX);
            individualXMLBlastBean.setResultFile(blastResultFile);

            // I believe that this includes the defline
            // set the sequence
            individualXMLBlastBean.setQuerySequence(">" + sequence.getAccession() + (sequence.getDescription() != null ? " " + sequence.getDescription() : "") + "\n" + sequence.seqString());

            // add the file and bean to their lists
            resultXMLBlastBeans.add(individualXMLBlastBean);
        }

        // set the multiple results file for each and execute thread
        for (XMLBlastBean blastBean : resultXMLBlastBeans) {
            // set the multiple query files
            blastBean.setOtherQueries(resultXMLBlastBeans);
            // execute thread
//            BlastQueryThreadCollection.getInstance().executeBlastThread(blastBean);
            logger.debug("scheduling blast ticket: " + blastBean.getTicketNumber());
            scheduleBlast(blastBean);
        }

        // set the inputXMLBlastBean to the first result bean
        if (resultXMLBlastBeans.size() > 0) {
            inputXMLBlastBean = resultXMLBlastBeans.get(0);
        }
	model.addAttribute(LookupStrings.FORM_BEAN, inputXMLBlastBean);
        return "blast-processing.page";
    }

    protected void scheduleBlast(XMLBlastBean blastBean) {
        BlastHeuristicFactory productionBlastHeuristicFactory = new ProductionBlastHeuristicFactory();
        BlastHeuristicCollection blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(blastBean);
        BlastQueryJob blastSingleTicketQueryThread = new BlastDistributableQueryThread(blastBean, blastHeuristicCollection);
        BlastQueryThreadCollection.getInstance().addJobAndStart(blastSingleTicketQueryThread);
    }

    private static Logger logger = Logger.getLogger(XMLBlastController.class);

}
