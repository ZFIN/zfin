package org.zfin.mutant.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.Term;
import org.zfin.ontology.presentation.PhenotypeStatementWarehousePresentation;
import org.zfin.util.ZfinStringUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@Controller
@RequestMapping("/fluorescence")
public class FluorescenceController {

    @Autowired
    private MutantRepository mutantRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @RequestMapping("/proteins")
    protected String showFluorescentProteins(Model model) {
        model.addAttribute("proteins", getMarkerRepository().getAllFluorescentProteins());
        return "marker/efg/fluorescence-view";
    }

}
