package org.zfin.marker.presentation;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptType;
import org.zfin.marker.TranscriptStatus;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.TranscriptService;
import org.zfin.people.Person;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 */
public class TranscriptAddController extends SimpleFormController {

    private static Logger logger = Logger.getLogger(TranscriptAddController.class) ;

    public static final String LOOKUP_TRANSCRIPT_TYPES = "transcriptTypes" ;

    //    protected Map referenceData(HttpServletRequest httpServletRequest,Object command, Error errors) throws Exception {

    protected Map referenceData(HttpServletRequest httpServletRequest, Object o, Errors errors) throws Exception {
        TranscriptAddBean transcriptAddBean = (TranscriptAddBean) o ;

        Map<String,String> types = new LinkedHashMap<String,String>() ;
        types.put("","Choose Type");

        TranscriptType.Type[] transcriptTypes = TranscriptType.Type.values() ;
        for(TranscriptType.Type transcriptType:transcriptTypes){
            types.put(transcriptType.toString(),transcriptType.toString()) ;
        }

        Map<String,String> statuses = new LinkedHashMap<String,String>() ;
        statuses.put("Choose Status","");

        TranscriptStatus.Status[] transcriptStatuses= TranscriptStatus.Status.values() ;
        for(TranscriptStatus.Status transcriptStatus:transcriptStatuses){
            statuses.put(transcriptStatus.toString(),transcriptStatus.toString()) ;
        }


        Map refMap = new HashMap() ;
        refMap.put(LookupStrings.DYNAMIC_TITLE,"Add Transcript") ;
        refMap.put("types",types) ;
        refMap.put("statuses",statuses) ;

        String name= httpServletRequest.getParameter("name") ;
        logger.info("name: " + name);
        transcriptAddBean.setName(name);
        return refMap ;
    }



    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {
        TranscriptAddBean transcriptAddBean = (TranscriptAddBean) command ;
        transcriptAddBean.setOwnerZdbID(Person.getCurrentSecurityUser().getZdbID());

        // create transcript
        Session session = HibernateUtil.currentSession() ;
        Transaction tx = session.beginTransaction() ;
        ModelAndView modelAndView ;
        try{
            Marker marker = TranscriptService.createTranscript(transcriptAddBean) ;

            // get zdbID and return to edti page
//        String zdbID = "ZDB-EST-000426-1181";
            String zdbID = marker.getZdbID() ;
            modelAndView = new ModelAndView("redirect:marker-edit?zdbID="+zdbID) ;
            tx.commit();
            return modelAndView ;
        }
        catch(Exception e){
            logger.error(e);
            tx.rollback();
            // todo: add some errors here
            modelAndView = new ModelAndView("transcript-add.page") ;
        }
        return modelAndView ;
    }

    //    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
//                                                 HttpServletResponse httpServletResponse) throws Exception {
//
//
//        return modelAndView ;
//
//    }
}