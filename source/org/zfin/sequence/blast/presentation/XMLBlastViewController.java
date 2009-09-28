package org.zfin.sequence.blast.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.blast.BlastSingleQueryThreadCollection;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;

/**
 * Displays a result from rendered XML.
 */
public class XMLBlastViewController extends AbstractCommandController {


    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        // get file
        XMLBlastBean xmlBlastBean = (XMLBlastBean) command ;

        ModelAndView modelAndView = new ModelAndView() ;

        try {
            // handle file name issues
            if(xmlBlastBean.getResultFile()==null){
                String fileName = request.getParameter("resultFile") ;
                if(fileName==null){
                    return new ModelAndView("blast-fetch-null.page");
                }

                if(false==fileName.startsWith(XMLBlastBean.BLAST_PREFIX)){
                    fileName = XMLBlastBean.BLAST_PREFIX + fileName;
                }

                if(false==fileName.endsWith(XMLBlastBean.BLAST_SUFFIX)){
                    fileName =  fileName + XMLBlastBean.BLAST_SUFFIX ;
                }

                File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" +fileName) ;
                xmlBlastBean.setResultFile(tempFile);
            }
            modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, xmlBlastBean.getTicketNumber());


            // if the thread is still processing
            if( false == BlastSingleQueryThreadCollection.getInstance().isBlastThreadDone(xmlBlastBean)){
                modelAndView.setViewName("blast-processing.page");
                modelAndView.addObject(LookupStrings.FORM_BEAN,xmlBlastBean) ;
            }
            else
            // failed to find the blast ticket
            if(false==xmlBlastBean.isFileExists() &&
                    true == BlastSingleQueryThreadCollection.getInstance().isBlastThreadDone(xmlBlastBean)){
                modelAndView.setViewName("bad-blast-ticket.page");
                modelAndView.addObject(LookupStrings.FORM_BEAN,xmlBlastBean) ;
            }
            else
            // the blast file is there and is done processing
            if(true==xmlBlastBean.isFileExists() &&
                    true== BlastSingleQueryThreadCollection.getInstance().isBlastThreadDone(xmlBlastBean)
                    ){
                // create a bene from the JAXB
                JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
                Unmarshaller u = jc.createUnmarshaller();
                BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(xmlBlastBean.getResultFile()));

                // create top-level alignment from the bean somehow (becomes an image?)
                xmlBlastBean.setBlastOutput(blastOutput) ;
                xmlBlastBean.setBlastResultBean(BlastResultMapper.createBlastResultBean(blastOutput)) ;

                // get result and process via biojava
                modelAndView.setViewName("blast-result.page");
                modelAndView.addObject(LookupStrings.FORM_BEAN,xmlBlastBean) ;
            }
            else{
                modelAndView.setViewName("bad-blast-result.page");
                modelAndView.addObject(LookupStrings.FORM_BEAN,xmlBlastBean) ;
            }
            return modelAndView ;
        } catch (Exception e) {
            modelAndView.setViewName("bad-blast-result.page");
            modelAndView.addObject(LookupStrings.FORM_BEAN,xmlBlastBean) ;
            return  modelAndView ;
        }
    }
}
