package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

@Controller
@RequestMapping("/expression")
public class AssayController {

    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    @RequestMapping("/assay-abbrev-popup")
    public String getExperimentPopup(Model model) {
        model.addAttribute("assay", infrastructureRepository.getAllAssays());
        return "expression/assay-abbrev-popup";
    }

}
