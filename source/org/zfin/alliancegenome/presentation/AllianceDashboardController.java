package org.zfin.ontology.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.service.ExpressionService;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Controller that serves the Alliance Data Submission Dashboard
 */
@Controller
@RequestMapping("/alliance")
public class AllianceDashboardController {

    @Autowired
    private ExpressionService expressionService;

    @RequestMapping("/dashboard")
    private String createTermUsageReport() {
        return "alliance/dashboard";
    }

}