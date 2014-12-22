package org.zfin.mutant.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/phenotype")
public class PhenotypeStatementDetailController  {

    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

    @RequestMapping(value = {"/phenotype-statement"})
    protected String getPhenotypeStatementPage(@RequestParam Long id, Model model) {
        PhenotypeStatement phenotypeStatement = mutantRepository.getPhenotypeStatementById(id);

        if (phenotypeStatement == null) {
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return "record-not-found.page";
        }


        model.addAttribute("phenotypeStatement",phenotypeStatement);
        model.addAttribute("uniqueTerms", getUniqueTerms(phenotypeStatement));

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Phenotype Statement: " + phenotypeStatement.getDisplayName());
        return "phenotype/phenotype-statement.page";
    }

    @RequestMapping(value = {"/phenotype-statement-popup"})
    protected String getPhenotypeStatementPopup(@RequestParam Long id, Model model) {
        PhenotypeStatement phenotypeStatement = mutantRepository.getPhenotypeStatementById(id);

        if (phenotypeStatement == null) {
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return "record-not-found.popup";
        }

        model.addAttribute("phenotypeStatement",phenotypeStatement);
        model.addAttribute("uniqueTerms", getUniqueTerms(phenotypeStatement));

        return "phenotype/phenotype-statement-popup.popup";
    }


    protected Collection<Term> getUniqueTerms(PhenotypeStatement phenotypeStatement) {
        List<Term> uniqueTerms = new ArrayList<>();
            uniqueTerms.add(phenotypeStatement.getEntity().getSuperterm());
        if (phenotypeStatement.getEntity().getSubterm() != null)
            uniqueTerms.add(phenotypeStatement.getEntity().getSubterm());
        uniqueTerms.add(phenotypeStatement.getQuality());
        if (phenotypeStatement.getRelatedEntity() != null) {
            if (!uniqueTerms.contains(phenotypeStatement.getRelatedEntity().getSuperterm()))
                uniqueTerms.add(phenotypeStatement.getRelatedEntity().getSuperterm());
            if (phenotypeStatement.getRelatedEntity().getSubterm() != null
                    && !uniqueTerms.contains(phenotypeStatement.getRelatedEntity().getSubterm()))
                uniqueTerms.add(phenotypeStatement.getRelatedEntity().getSubterm());
        }

        return uniqueTerms;
    }

}
