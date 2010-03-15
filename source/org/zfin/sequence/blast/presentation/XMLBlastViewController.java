package org.zfin.sequence.blast.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.BlastThreadCollection;
import org.zfin.sequence.blast.BlastThreadService;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;

/**
 * Displays a result from rendered XML.
 */
public class XMLBlastViewController extends AbstractCommandController {

    private int maxTries = 5;
    private long initPauseTimeMs = 2000; // 2 seconds
    private long pauseTimeBewteenTriesMs = 5000; // 5 seconds
    private JAXBContext jc = null;
    private BlastThreadCollection blastThreadCollection ;

    private Logger logger = Logger.getLogger(XMLBlastViewController.class);

    public XMLBlastViewController() {
        try {
            jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
        } catch (JAXBException e) {
            logger.error("Failed to instantiate JAXContext for org.zfin.sequence.blast.results: ", e);
        }
    }

    protected BlastThreadCollection getQueryThreadCollection(){
        return BlastQueryThreadCollection.getInstance();
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        // get file
        XMLBlastBean xmlBlastBean = (XMLBlastBean) command;

        ModelAndView modelAndView = new ModelAndView();
        String fileName = null;
        int tries = 0;

        try {
            // handle file name issues
            if (xmlBlastBean.getResultFile() == null) {
                fileName = request.getParameter("resultFile");
                if (fileName == null) {
                    return new ModelAndView("blast-fetch-null.page");
                }

                if (false == fileName.startsWith(XMLBlastBean.BLAST_PREFIX)) {
                    fileName = XMLBlastBean.BLAST_PREFIX + fileName;
                }

                if (false == fileName.endsWith(XMLBlastBean.BLAST_SUFFIX)) {
                    fileName = fileName + XMLBlastBean.BLAST_SUFFIX;
                }

                File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
                xmlBlastBean.setResultFile(tempFile);
            }
            modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, xmlBlastBean.getTicketNumber());


            // if the thread is still processing
//            if (BlastThreadService.isJobInQueue(xmlBlastBean, BlastQueryThreadCollection.getInstance())) {
            if (BlastThreadService.isJobInQueue(xmlBlastBean, getQueryThreadCollection() )) {
//                if (false == BlastQueryThreadCollection.getInstance().isBlastThreadDone(xmlBlastBean)) {
                modelAndView.setViewName("blast-processing.page");
                modelAndView.addObject(LookupStrings.FORM_BEAN, xmlBlastBean);
            }
            // the thread is done processing
            else {
                // if it is done processing and no file exists, then there is a serious problem
                // failed to find the blast ticket
                if (false == xmlBlastBean.isFileExists()) {
                    modelAndView.setViewName("bad-blast-ticket.page");
                    modelAndView.addObject(LookupStrings.FORM_BEAN, xmlBlastBean);
                }
                // if the file exists, then life is good and we can return the result
                else {
                    // the file does exist
                    // create a bean from the JAXB
                    File resultFile = xmlBlastBean.getResultFile();
                    BlastOutput blastOutput = null;
                    boolean done = false;
                    while (done == false && tries < maxTries) {
                        logger.debug("try: " + tries + " of " + maxTries);
                        try {
                            ++tries;
                            // wait 2 seconds to allow the file time to finish writing
                            Thread.sleep(initPauseTimeMs);
                            Unmarshaller u = jc.createUnmarshaller();
                            blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(resultFile));
                            done = true;
                        }
                        catch (Exception e) {
                            // if it throws premature end of file exception,
                            // then we are probably not done writing to it, so try again
                            if (e.getMessage().contains("Premature end of file.")) {
//                            if(e instanceof org.xml.sax.SAXParseException){
                                logger.warn("sax exception while parsing: " + resultFile, e);
                                done = false;
                                Thread.sleep(pauseTimeBewteenTriesMs);
                            } else {
                                throw e;
                            }
                        }
                    }

                    // create top-level alignment from the bean somehow (becomes an image?)
                    xmlBlastBean.setBlastOutput(blastOutput);
                    xmlBlastBean.setBlastResultBean(BlastResultMapper.createBlastResultBean(blastOutput));

                    // get result and process via biojava
                    modelAndView.setViewName("blast-result.page");
                    modelAndView.addObject(LookupStrings.FORM_BEAN, xmlBlastBean);
                }
            }
            return modelAndView;
        } catch (Exception e) {
            String errorString = "problem viewing executed blast: ";
            errorString += fileName;
            errorString += " tries: " + tries;
            logger.error(errorString, e);
            modelAndView.setViewName("bad-blast-result.page");
            modelAndView.addObject(LookupStrings.FORM_BEAN, xmlBlastBean);
            return modelAndView;
        }
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public void setInitPauseTimeMs(long initPauseTimeMs) {
        this.initPauseTimeMs = initPauseTimeMs;
    }

    public void setPauseTimeBewteenTriesMs(long pauseTimeBewteenTriesMs) {
        this.pauseTimeBewteenTriesMs = pauseTimeBewteenTriesMs;
    }
}
