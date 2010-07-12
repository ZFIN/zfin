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
 */
public class AntibodyDetailController extends AbstractCommandController {

    public static final String ACCESS_URL = "/action/antibody/detail?antibody.zdbID=";
    public static final String ANTIBODY_DETAIL_PAGE_TILES = "antibody-detail.page";
    public static final String REDIRECT = "redirect:";

    private static final Logger LOG = Logger.getLogger(AntibodyDetailController.class);

    private AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();

    public AntibodyDetailController() {
        setCommandClass(AntibodyBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Antibody Detail Controller");
        AntibodyBean form = (AntibodyBean) command;
        Antibody ab = antibodyRepository.getAntibodyByIDFullyPopulated(form.getAntibody().getZdbID());
        if (ab == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, form.getAntibody().getZdbID());

        return getModelAndViewForSingleAntibody(ab, false);
    }

    public static ModelAndView getModelAndViewForSingleAntibody(Antibody ab, boolean redirect) {
        AntibodyBean form = new AntibodyBean();
        AntibodyService abStat = new AntibodyService(ab);
        form.setAntibodyStat(abStat);
        form.setAntibody(ab);

        ModelAndView modelAndView;
        if (redirect) {
            modelAndView = new ModelAndView(REDIRECT + ACCESS_URL + ab.getZdbID());
        } else {
            modelAndView = new ModelAndView(ANTIBODY_DETAIL_PAGE_TILES, LookupStrings.FORM_BEAN, form);
            modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, ab.getName());
        }
        return modelAndView;
    }
}
