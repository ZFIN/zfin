package org.zfin.mutant.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.repository.MutantRepository;

import java.util.List;


@Controller
@RequestMapping("/feature")
public class WildtypeListController {

    private static final Logger LOG = LogManager.getLogger(WildtypeListController.class);

    @Autowired
    private MutantRepository mutantRepository;

    @RequestMapping(value = {"/wildtype-list"})
    protected String getWildtypeList(Model model) {
        LOG.debug("Start WildytpeListController");

        List<Genotype> wildtypes = mutantRepository.getAllWildtypeGenotypes();
        model.addAttribute("wildtypes", wildtypes);

        return "feature/wildtype-lines.page";
    }

}