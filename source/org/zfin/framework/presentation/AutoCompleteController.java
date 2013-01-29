package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class AutoCompleteController extends MultiActionController {

    private static Logger LOG = Logger.getLogger(AutoCompleteController.class);
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();

    public ModelAndView geneFamilyHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        AutoCompleteBean autoCompleteBean = new AutoCompleteBean();

        String query = request.getParameter("query");
        autoCompleteBean.setMarkerFamilyNames(mr.getMarkerFamilyNamesBySubstring(query));

        LOG.info("gene family, query: '"
                + request.getParameter("query")
                + "' size: "
                + autoCompleteBean.getMarkerFamilyNames().size());

        return new ModelAndView("gene-family-autocomplete.page", LookupStrings.FORM_BEAN, autoCompleteBean);
    }


}
