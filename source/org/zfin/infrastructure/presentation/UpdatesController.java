package org.zfin.infrastructure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.UpdatesDTO;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.repository.PublicationRepository;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/updates")
public class UpdatesController {

    @Autowired
    InfrastructureRepository infrastructureRepository;

    @Autowired
    PublicationRepository publicationRepository;


    @RequestMapping("/{zdbID}")
    public String viewUpdates(Model model, @PathVariable String zdbID) {
        List<UpdatesDTO> updatesDTO = UpdatesDTO.fromUpdates(infrastructureRepository.getUpdates(zdbID));
        model.addAttribute("zdbID", zdbID);
        model.addAttribute("updates", updatesDTO);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Updates for " + zdbID);
        return "infrastructure/view-updates";
    }

    @RequestMapping("/v2/{zdbID}")
    public String viewUpdates2(Model model, @PathVariable String zdbID) {
        List<UpdatesDTO> updatesDTO = UpdatesDTO.fromUpdates(infrastructureRepository.getUpdates(zdbID));

        //is this a publication?
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication != null) {
            List<PublicationTrackingHistory> events = publicationRepository.fullTrackingHistory(publication);
            List<UpdatesDTO> publicationUpdates = UpdatesDTO.fromPublicationEvents(events);
            updatesDTO.addAll(publicationUpdates);
        }
        updatesDTO.sort(Comparator.comparing(UpdatesDTO::getWhenUpdated).reversed());

        model.addAttribute("zdbID", zdbID);
        model.addAttribute("updates", updatesDTO);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Updates for " + zdbID);
        return "infrastructure/view-updates";
    }
}
