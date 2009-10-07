package org.zfin.sequence.blast.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class IncludeExternalBlastController extends AbstractCommandController{

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        IncludeExternalBlastBean includeExternalBlastBean = (IncludeExternalBlastBean) o ;

        DBLink dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByID(includeExternalBlastBean.getZdbID()) ;
        includeExternalBlastBean.setDbLink(dbLink);

        ModelAndView modelAndView = new ModelAndView("include-external-blast.page") ;
        modelAndView.addObject(LookupStrings.FORM_BEAN,includeExternalBlastBean) ;
        
        return modelAndView ;
    }
}
