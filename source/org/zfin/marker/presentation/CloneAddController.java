package org.zfin.marker.presentation;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Clone;
import org.zfin.marker.MarkerService;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 */
public class CloneAddController extends SimpleFormController {

    private static Logger logger = Logger.getLogger(CloneAddController.class) ;
    //    protected Map referenceData(HttpServletRequest httpServletRequest,Object command, Error errors) throws Exception {
    protected Map referenceData(HttpServletRequest httpServletRequest) throws Exception {

        Map refMap = new HashMap() ;
        refMap.put("cloneMarkerTypes", MarkerService.getCloneMarkerTypes()) ;
        refMap.put("cloneLibraries", RepositoryFactory.getMarkerRepository().getProbeLibraries()) ;
        return refMap ;
    }


    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {
        CloneAddBean cloneAddBean = (CloneAddBean) command ;

        String name = cloneAddBean.getName() ;

        // because it can come through on both the request and the command, will put both in with the same name
        // delimited by a comma.  This addresses that.
        String[] names = name.split(",") ;
        if(names.length ==2 && names[0].equals(names[1])){
            cloneAddBean.setName(names[0]);
        }

        cloneAddBean.setOwnerZdbID(Person.getCurrentSecurityUser().getZdbID());
        HibernateUtil.currentSession().beginTransaction() ;
        Clone clone = MarkerService.createClone(cloneAddBean) ;
        HibernateUtil.currentSession().flush() ;
        HibernateUtil.currentSession().getTransaction().commit() ;

        String zdbID = clone.getZdbID() ;
        return new ModelAndView("redirect:/action/marker/marker-edit?zdbID="+zdbID) ;
    }


}
