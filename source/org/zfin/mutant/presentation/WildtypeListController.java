package org.zfin.mutant.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Controller
public class WildtypeListController {

    private static final Logger LOG = Logger.getLogger(WildtypeListController.class);

    @Autowired
    private MutantRepository mutantRepository;
    
    @RequestMapping(value = {"/wildtype-list"})
    protected String getWildtypeList(Model model) {
        LOG.debug("Start WildytpeListController");

        List<Genotype> wildtypes = mutantRepository.getWildtypeGenotypes();
        model.addAttribute("wildtypes", wildtypes);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Wild-TypeLines");

        return "feature/wildtype-lines.page";
    }

}