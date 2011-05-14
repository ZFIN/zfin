package org.zfin.mutant.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@Controller
public class PhenotypeStatementDetailController  {
    private static Logger logger = Logger.getLogger(PhenotypeStatementDetailController.class);

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
        List<Term> uniqueTerms = new ArrayList<Term>();
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
