package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RunCandidate;


/**
 * Class CandidateController.
 */
@Controller
public class RedirectCandidateViewController {

    private static Logger logger = Logger.getLogger(NomenclatureCandidateController.class);


    @RequestMapping(value = "/candidate-view/{zdbID}",method = RequestMethod.GET)
    protected String handle( @PathVariable String zdbID) throws Exception {
        RunCandidate rc = RepositoryFactory.getRenoRepository().getRunCandidateByID(zdbID);

        if (rc.getRun().isNomenclature()) {
            return "forward:/action/reno/nomenclature-candidate-view/"+zdbID ;
        } else {
            return "forward:/action/reno/redundancy-candidate-view/"+zdbID ;
        }
    }
//
//    @RequestMapping(value = "/candidate-view/{zdbID}",method = RequestMethod.POST)
//    protected String handlePost( @PathVariable String zdbID) throws Exception {
//        RunCandidate rc = RepositoryFactory.getRenoRepository().getRunCandidateByID(zdbID);
//
//        if (rc.getRun().isNomenclature()) {
//            return "forward:/action/reno/nomenclature-candidate-view/"+zdbID ;
//        } else {
//            return "forward:/action/reno/redundancy-candidate-view/"+zdbID ;
//        }
//    }

}
