package org.zfin.sequence.reno.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.repository.RenoRepository;

import java.util.List;

/**
 * Class RunController.
 */
@Controller
@RequestMapping(value = "/reno")
public class RunController {

    private RenoRepository renoRepository = RepositoryFactory.getRenoRepository() ;

    @RequestMapping("/run-list")
    protected ModelAndView handleRequestInternal() throws Exception {
        RunBean form = new RunBean();
        List<RedundancyRun> redundancyRuns = renoRepository.getRedundancyRuns();
        List<NomenclatureRun> nomenclatureRuns = renoRepository.getNomenclatureRuns();
        form.setRedundancyRuns(redundancyRuns);
        form.setNomenclatureRuns(nomenclatureRuns);

        return new ModelAndView("reno/run-list", LookupStrings.FORM_BEAN, form);
    }

}



