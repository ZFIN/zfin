package org.zfin.sequence.reno.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Class RunController.
 * TODO: comments
 */

public class RunController extends AbstractController {

    private RenoRepository renoRepository; //set in spring configuration
    private String successView; // set in spring configuration bean

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RunBean form = new RunBean();
        List<RedundancyRun> redundancyRuns = renoRepository.getRedundancyRuns();
        List<NomenclatureRun> nomenclatureRuns = renoRepository.getNomenclatureRuns();
        form.setRedundancyRuns(redundancyRuns);
        form.setNomenclatureRuns(nomenclatureRuns);

        return new ModelAndView(successView, LookupStrings.FORM_BEAN, form) ; 
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public RenoRepository getRenoRepository() {
        return renoRepository;
    }

    public void setRenoRepository(RenoRepository renoRepository) {
        this.renoRepository = renoRepository;
    }
}



