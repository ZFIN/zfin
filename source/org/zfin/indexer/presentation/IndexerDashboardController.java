package org.zfin.indexer.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/indexer")
public class IndexerDashboardController {

    @RequestMapping("/")
    protected String showIndexerDashboard(Model model) {
        return "indexer/indexer-view";
    }

}
