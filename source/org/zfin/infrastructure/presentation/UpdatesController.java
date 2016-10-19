package org.zfin.infrastructure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.Updates;
import org.zfin.infrastructure.repository.InfrastructureRepository;

import java.util.List;

@Controller
@RequestMapping("/updates")
public class UpdatesController {

    @Autowired
    InfrastructureRepository infrastructureRepository;

    @RequestMapping("/{zdbID}")
    public String viewUpdates(Model model, @PathVariable String zdbID) {
        List<Updates> updates = infrastructureRepository.getUpdates(zdbID);
        model.addAttribute("zdbID", zdbID);
        model.addAttribute("updates", updates);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Updates for " + zdbID);
        return "infrastructure/view-updates.page";
    }

}
