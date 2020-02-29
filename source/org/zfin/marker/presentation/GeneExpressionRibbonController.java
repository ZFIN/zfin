package org.zfin.marker.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.*;
import org.zfin.ontology.service.RibbonService;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class GeneExpressionRibbonController {

    @Autowired
    private RibbonService ribbonService;

    @RequestMapping(value = "/marker/{zdbID}/expression/ribbon-summary")
    public RibbonSummary getExpressionRibbonSummary(@PathVariable("zdbID") String zdbID) throws Exception {
        return ribbonService.buildExpressionRibbonSummary(zdbID);
    }

}
