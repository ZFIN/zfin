package org.zfin.wiki.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.wiki.AntibodyWikiWebService;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class WikiLinkController extends AbstractCommandController {

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        WikiBean wikiBean = (WikiBean) o ;

        wikiBean.setUrl(AntibodyWikiWebService.getInstance().getWikiLink(wikiBean.getName()));

        return new ModelAndView("wiki-link.page", LookupStrings.FORM_BEAN,wikiBean) ;
    }
}
