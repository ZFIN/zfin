package org.zfin.marker.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.RibbonSummary;
import org.zfin.ontology.service.RibbonService;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class PhenotypeRibbonController {

    @Autowired
    private RibbonService ribbonService;

    @RequestMapping(value = "/marker/{zdbID}/phenotype/ribbon-summary")
    public RibbonSummary getPhenotypeRibbonSummary(@PathVariable("zdbID") String zdbID) throws Exception {
        return ribbonService.buildPhenotypeRibbonSummary(zdbID);
    }

}
