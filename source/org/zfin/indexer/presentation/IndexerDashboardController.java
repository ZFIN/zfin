package org.zfin.indexer.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.indexer.UiIndexer;
import org.zfin.indexer.UiIndexerConfig;

@Controller
@RequestMapping("/indexer")
public class IndexerDashboardController {

    @RequestMapping("/")
    protected String showIndexerDashboard(Model model) {
        return "indexer/indexer-view";
    }

    @RequestMapping(value = "/runIndexer/{indexerName}", method = RequestMethod.GET)
    public String runIndexer(@PathVariable String indexerName) {
        UiIndexerConfig config = UiIndexerConfig.getIndexerByName(indexerName);
        if (config != null) {
            String[] args = {config.getTypeName()};
            new Thread(() -> {
                try {
                    UiIndexer.main(args);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        return "redirect:/action/indexer/";
    }


}
