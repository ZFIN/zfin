package org.zfin.indexer.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.interfaces.BaseCrudController;
import org.zfin.framework.services.BaseEntityCrudService;
import org.zfin.indexer.IndexerRun;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.repository.MutantRepository;

@Controller
@RequestMapping("/indexer")
public class IndexerController {

    @RequestMapping("/runnn")
    protected String showIndexerDashboard(Model model) {
        return "indexer/indexer-view";
    }

}
