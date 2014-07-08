package org.zfin.expression.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

@Controller
public class AssayController {

    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private static Logger logger = Logger.getLogger(AssayController.class);


    @RequestMapping("/assay-abbrev-popup")
    public String getExperimentPopup(Model model) {
        model.addAttribute("assay", infrastructureRepository.getAllAssays());
//        logger.error("popup is called");
        return "expression/assay-abbrev-popup.popup";
    }

}
