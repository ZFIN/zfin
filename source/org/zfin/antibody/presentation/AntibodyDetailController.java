package org.zfin.antibody.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller class that serves the antibody details page.
 * @deprecated Please use AntibodyViewController instead.  This will be deleted.
 */
public class AntibodyDetailController extends AbstractCommandController {

    public static final String ACCESS_URL = "/action/antibody/detail?antibody.zdbID=";
    public static final String NEW_URL = "/action/marker/view/";
    public static final String ANTIBODY_DETAIL_PAGE_TILES = "antibody-detail.page";
    public static final String REDIRECT = "redirect:";

    private static final Logger LOG = Logger.getLogger(AntibodyDetailController.class);

    public AntibodyDetailController() {
        setCommandClass(AntibodyBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Antibody Detail Controller");
        AntibodyBean form = (AntibodyBean) command;
        return new ModelAndView(REDIRECT+NEW_URL+form.getAntibody().getZdbID());
    }
}
