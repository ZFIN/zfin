package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.mvc.BaseCommandController;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RunCandidate ;
import org.zfin.framework.presentation.LookupStrings;

import java.util.Map;

/**
 *  Class AlignmentsController.
 */
public class AlignmentsController extends AbstractCommandController {

    public static String RUNCANDIDATE_ZDBID = "runCandidate.zdbID" ;
    private static Logger logger = Logger.getLogger(AlignmentsController.class);
    private String successView ; // set in spring configuration bean

    public AlignmentsController(){
        // set here because test-classes choke without it
        // also has to be set in order to provide errors
        setCommandClass(Object.class);
        setCommandName("formBean");
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,Object command, BindException errors) throws Exception {
        String runCandidateZdbID = request.getParameter(RUNCANDIDATE_ZDBID) ;
        RunCandidate runCandidate =   RepositoryFactory.getRenoRepository().getRunCandidateByID( runCandidateZdbID ) ;
        CandidateBean candidateBean = new CandidateBean() ;
        logger.info("ZdbID[" +runCandidateZdbID + "] runCandidate[" + runCandidate+"]") ;

        if(runCandidate!=null){
            logger.info("ZdbID[" +runCandidateZdbID + "] runCandidate queries.size()["+runCandidate.getCandidateQueries() +"]") ;
            candidateBean.setRunCandidate(runCandidate) ;
        }
        else{
            logger.fatal("ZdbID[" +runCandidateZdbID + "] runCandidate is null generating an empty one") ;
            candidateBean.setRunCandidate(new RunCandidate() ) ;
            errors.reject("no message key","Invalidate RunCandidate zdbID["+runCandidateZdbID+"]") ;
        }

        ModelAndView modelAndView = new ModelAndView(successView,errors.getModel()) ;
        modelAndView.addObject(LookupStrings.FORM_BEAN,candidateBean) ;
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE,runCandidateZdbID) ;
        return modelAndView ;

    }

    /**
     * Get successView.
     *
     * @return successView as String.
     */
    public String getSuccessView()
    {
        return successView;
    }

    /**
     * Set successView.
     *
     * @param successView the value to set.
     */
    public void setSuccessView(String successView)
    {
        this.successView = successView;
    }


    
}


