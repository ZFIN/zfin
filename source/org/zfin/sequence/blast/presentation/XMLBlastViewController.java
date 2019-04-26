package org.zfin.sequence.blast.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.BlastThreadCollection;
import org.zfin.sequence.blast.BlastThreadService;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;

/**
 * Displays a result from rendered XML.
 */
@Controller
public class XMLBlastViewController {

    private int maxTries = 3;
    private long initPauseTimeMs = 2000; // 2 seconds
    private long pauseTimeBetweenTriesMs = 5000; // 5 seconds
    private JAXBContext jc = null;

    private Logger logger = LogManager.getLogger(XMLBlastViewController.class);

    public XMLBlastViewController() {
        try {
            jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
        } catch (JAXBException e) {
            logger.error("Failed to instantiate JAXContext for org.zfin.sequence.blast.results: ", e);
        }
    }

    protected BlastThreadCollection getQueryThreadCollection() {
        return BlastQueryThreadCollection.getInstance();
    }

    @RequestMapping("/blast/blast-view")
    protected String showBlast(@RequestParam(required = false) String resultFileString,
                               @ModelAttribute("formBean") XMLBlastBean xmlBlastBean,
                               Model model) throws Exception {
        String fileName = null;
        int tries = 0;
        String returnView;

        try {
            // handle file name issues
            if (xmlBlastBean.getResultFile() == null) {
                fileName = resultFileString;

                // spring 3 now makes that hard to happen
                if (fileName == null) {
                    return "blast-fetch-null.page";
                }

                xmlBlastBean.setResultFile(new File(fileName));
            }

            if (!isValidBlastResultLocation(xmlBlastBean)) {
                fixFileLocation(xmlBlastBean);
            }

            model.addAttribute(LookupStrings.DYNAMIC_TITLE, xmlBlastBean.getTicketNumber());


            // if the thread is still processing
//            if (BlastThreadService.isJobInQueue(xmlBlastBean, BlastQueryThreadCollection.getInstance())) {
            if (BlastThreadService.isJobInQueue(xmlBlastBean, getQueryThreadCollection())) {
//                if (false == BlastQueryThreadCollection.getInstance().isBlastThreadDone(xmlBlastBean)) {
                returnView = "blast-processing.page";
            }
            // the thread is done processing
            else {
                // if it is done processing and no file exists, then there is a serious problem
                // failed to find the blast ticket
                if (!xmlBlastBean.isFileExists()) {
                    returnView = "bad-blast-ticket.page";
                }
                // if the file exists, then life is good and we can return the result
                else {
                    // the file does exist
                    // create a bean from the JAXB
                    File resultFile = xmlBlastBean.getResultFile();
                    BlastOutput blastOutput = null;
                    boolean done = false;
                    while (!done && tries < maxTries) {
                        logger.debug("try: " + tries + " of " + maxTries);
                        try {
                            ++tries;
                            // wait 2 seconds to allow the file time to finish writing
                            Thread.sleep(initPauseTimeMs);
                            Unmarshaller u = jc.createUnmarshaller();
                            blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(resultFile));
                            done = true;
                        } catch (Exception e) {
                            // if it throws premature end of file exception,
                            // then we are probably not done writing to it, so try again
                            if (e.getMessage() != null && e.getMessage().contains("Premature end of file.")) {
//                            if(e instanceof org.xml.sax.SAXParseException){
                                logger.warn("sax exception while parsing: " + resultFile, e);
                                done = false;
                                Thread.sleep(pauseTimeBetweenTriesMs);
                            } else {
                                throw e;
                            }
                        }
                    }

                    // create top-level alignment from the bean somehow (becomes an image?)
                    xmlBlastBean.setBlastOutput(blastOutput);
                    xmlBlastBean.setBlastResultBean(BlastResultMapper.createBlastResultBean(blastOutput));

                    // get result and process via biojava
                    returnView = "blast-result.page";
                }
            }
            return returnView;
        } catch (Exception e) {
            String errorString = "problem viewing executed blast: ";
            errorString += fileName;
            errorString += " tries: " + tries;
            logger.error(errorString, e);
            return "bad-blast-result.page";
        }
    }

    public boolean isValidBlastResultLocation(XMLBlastBean xmlBlastBean) {
        try {
            return xmlBlastBean.getResultFile().getAbsolutePath().contains(System.getProperty("java.io.tmpdir"))
                    &&
                    xmlBlastBean.isFileExists()
                    ;
        } catch (Exception e) {
            logger.error("Failed to evaluate fileName:\n" + xmlBlastBean.getResultFile(), e);
            return false;
        }
    }

    public XMLBlastBean fixFileLocation(XMLBlastBean xmlBlastBean) {
        String fileName = xmlBlastBean.getResultFile().getName();
        if (!fileName.startsWith(XMLBlastBean.BLAST_PREFIX)) {
            fileName = XMLBlastBean.BLAST_PREFIX + fileName;
        }

        if (!fileName.endsWith(XMLBlastBean.BLAST_SUFFIX)) {
            fileName = fileName + XMLBlastBean.BLAST_SUFFIX;
        }
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        xmlBlastBean.setResultFile(file);
        return xmlBlastBean;
    }
}
