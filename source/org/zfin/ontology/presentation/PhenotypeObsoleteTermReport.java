package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.ConsiderTerm;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.ReplacementTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

/**
 * Report for phenotypes with obsoleted terms.
 */
public class PhenotypeObsoleteTermReport extends ObsoleteTermReport{

    private PhenotypeStatement phenotypeStatement;

    public PhenotypeObsoleteTermReport(PhenotypeStatement phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }

    public PhenotypeStatement getPhenotypeStatement() {
        return phenotypeStatement;
    }
}