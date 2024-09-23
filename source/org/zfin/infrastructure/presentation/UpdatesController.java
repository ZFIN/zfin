package org.zfin.infrastructure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.publication.repository.PublicationRepository;

@Controller
@RequestMapping("/updates")
public class UpdatesController {

    @Autowired
    InfrastructureRepository infrastructureRepository;

    @Autowired
    PublicationRepository publicationRepository;


    @RequestMapping("/{zdbID}")
    public String viewUpdates(Model model,
                              @PathVariable String zdbID,
                              @RequestParam(value = "fieldNameFilter", required = false) String fieldNameFilter
                              ) {
        model.addAttribute("zdbID", zdbID);
        model.addAttribute("publication", publicationRepository.getPublication(zdbID));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Updates for " + zdbID);
        if (fieldNameFilter != null) {
            model.addAttribute("fieldNameFilter", fieldNameFilter);
        }
        return "infrastructure/view-updates";
    }
}
