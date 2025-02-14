package org.zfin.alliancegenome.presentation;

import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.mutant.Fish;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getFishRepository;

/**
 * Controller that serves the Alliance Data Submission Dashboard
 */
@Controller
@RequestMapping("/alliance")
public class AllianceDashboardController {


    @RequestMapping("/dashboard")
    private String allianceDashboard(Model model, HttpSession session) {
        List<String> differencesAlliance = (List<String>) session.getAttribute("differencesAlliance");
        List<String> differencesZfin = (List<String>) session.getAttribute("differencesZfin");
        if(differencesZfin == null && differencesAlliance == null){
            FishRESTAllianceService service = new FishRESTAllianceService();
            List<AffectedGenomicModel> response = service.getAGM();
            List<String> allianceIDs = response.stream()
                .filter(affectedGenomicModel -> affectedGenomicModel.getDataProvider().getAbbreviation().equals("ZFIN"))
                .filter(affectedGenomicModel -> !affectedGenomicModel.getObsolete())
                .map(affectedGenomicModel1 -> affectedGenomicModel1.getPrimaryExternalId().replace("ZFIN:", ""))
                .toList();

            List<Fish> allFish = getFishRepository().getAllFish(0);
            List<String> zfinIDs = allFish.stream().map(Fish::getZdbID).toList();
            differencesAlliance = new ArrayList<>((CollectionUtils.removeAll(allianceIDs, zfinIDs)));
            differencesZfin = new ArrayList<>((CollectionUtils.removeAll(zfinIDs, allianceIDs)));
            session.setAttribute("differencesAlliance", differencesAlliance);
            session.setAttribute("differencesZfin", differencesZfin);
        }
        model.addAttribute("differencesAlliance", differencesAlliance);
        model.addAttribute("differencesZfin", differencesZfin);
        return "alliance/dashboard";
    }

}