package org.zfin.sequence.reno.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RunCandidate;


@Controller
@RequestMapping(value = "/reno")
public class RedirectCandidateViewController {


    @RequestMapping(value = "/candidate-view/{zdbID}", method = RequestMethod.GET)
    protected String handle(@PathVariable String zdbID) throws Exception {
        RunCandidate rc = RepositoryFactory.getRenoRepository().getRunCandidateByID(zdbID);

        if (rc.getRun().isNomenclature()) {
            return "forward:/action/reno/nomenclature-candidate-view/" + zdbID;
        } else {
            return "forward:/action/reno/redundancy-candidate-view/" + zdbID;
        }
    }
}
