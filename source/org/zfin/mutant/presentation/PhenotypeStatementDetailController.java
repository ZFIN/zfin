package org.zfin.mutant.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.Term;
import org.zfin.ontology.presentation.PhenotypeStatementWarehousePresentation;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/phenotype")
public class PhenotypeStatementDetailController {

    @Autowired
    private MutantRepository mutantRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @RequestMapping("/phenotype-statement")
    protected String getPhenotypeStatementPage(@RequestParam Long id,
                                               Model model,
                                               HttpServletResponse response) {
        PhenotypeStatementWarehouse phenotypeStatement = mutantRepository.getPhenotypeStatementWarehouseById(id);
        String statementName = PhenotypeStatementWarehousePresentation.getNameWithoutNormalText(phenotypeStatement);

        if (phenotypeStatement == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return "record-not-found.page";
        }

        model.addAttribute("phenotypeStatement", phenotypeStatement);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Phenotype Statement: " + statementName);

        if (phenotypeStatement.isMorphologicalPhenotype()) {
            model.addAttribute("uniqueTerms", getUniqueTerms(phenotypeStatement));
            return "phenotype/phenotype-statement.page";
        } else {
            model.addAttribute("genePreviousNames", markerRepository.getPreviousNamesLight(phenotypeStatement.getGene()));
            return "phenotype/phenotypic-expression-statement.page";
        }
    }

    @RequestMapping("/phenotype-statement-popup")
    protected String getPhenotypeStatementPopup(@RequestParam Long id,
                                                Model model,
                                                HttpServletResponse response) {
        PhenotypeStatementWarehouse phenotypeStatement = mutantRepository.getPhenotypeStatementWarehouseById(id);

        if (phenotypeStatement == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return "record-not-found.popup";
        }

        model.addAttribute("phenotypeStatement", phenotypeStatement);

        if (phenotypeStatement.isMorphologicalPhenotype()) {
            model.addAttribute("uniqueTerms", getUniqueTerms(phenotypeStatement));
            return "phenotype/phenotype-statement-popup.popup";
        } else {
            model.addAttribute("genePreviousNames", markerRepository.getPreviousNamesLight(phenotypeStatement.getGene()));
            return "phenotype/phenotypic-expression-statement-popup.popup";
        }
    }

    protected Collection<Term> getUniqueTerms(PhenotypeStatementWarehouse phenotypeStatement) {
        List<Term> uniqueTerms = new ArrayList<>();
        uniqueTerms.add(phenotypeStatement.getEntity().getSuperterm());
        if (phenotypeStatement.getEntity().getSubterm() != null) {
            uniqueTerms.add(phenotypeStatement.getEntity().getSubterm());
        }
        uniqueTerms.add(phenotypeStatement.getQuality());
        if (phenotypeStatement.getRelatedEntity() != null) {
            if (!uniqueTerms.contains(phenotypeStatement.getRelatedEntity().getSuperterm())) {
                uniqueTerms.add(phenotypeStatement.getRelatedEntity().getSuperterm());
            }
            if (phenotypeStatement.getRelatedEntity().getSubterm() != null
                    && !uniqueTerms.contains(phenotypeStatement.getRelatedEntity().getSubterm())) {
                uniqueTerms.add(phenotypeStatement.getRelatedEntity().getSubterm());
            }
        }

        return uniqueTerms;
    }

}
