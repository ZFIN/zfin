package org.zfin.expression;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.*;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FB Case: 5868!
 */
public class EQReport {

    private static String[] termsFirstPaulaReport = {"ZFA:0001184", "ZFA:0001384", "ZFA:0001454", "ZFA:0001161", "ZFA:0000141", "ZFA:0001453",
            "ZFA:0005096", "GO:0033340", "GO:0033339"};

    private static String[] termsSecondPaulaReport = {"ZFA:0001184", "ZFA:0001384", "ZFA:0001454", "ZFA:0001161", "ZFA:0000141", "ZFA:0001453"};
    private static String[] termsThirdPaulaReport = {"GO:0033340", "GO:0033339"};
    private String reportFile;

    public static void main(String[] arguments) {
        EQReport report = new EQReport();
        //report.createFirstReport(termsFirstPaulaReport);
        //report.createSecondReport(termsSecondPaulaReport);
        report.createThirdReport(termsThirdPaulaReport);
        //report.createGoReport();
        report.printToFile();
    }

    private List<Phenotype> removeDuplicates(List<Phenotype> allPhenotypes) {
        Set<Phenotype> phenos = new HashSet<Phenotype>();
        for (Phenotype pheno : allPhenotypes) {
            phenos.add(pheno);
        }
        ArrayList<Phenotype> phenotypeArrayList = new ArrayList<Phenotype>(phenos.size());
        phenotypeArrayList.addAll(phenos);
        return phenotypeArrayList;
    }

    private List<Phenotype> allPhenotypes = new ArrayList<Phenotype>();
    private List<ExpressionResult> allExpressions = new ArrayList<ExpressionResult>();
    private List<MarkerGoTermEvidence> allMarkerGo = new ArrayList<MarkerGoTermEvidence>();

    private List<Term> getGoTerms() {
        String[] ids = {"ZDB-TERM-091209-16772", "ZDB-TERM-091209-18555", "ZDB-TERM-091209-16771", "ZDB-TERM-091209-18535", "ZDB-TERM-091209-18532",
                "ZDB-TERM-091209-18535", "ZDB-TERM-091209-18547", "ZDB-TERM-091209-18554", "ZDB-TERM-091209-18534", "ZDB-TERM-091209-18546"};
        List<Term> allTerms = new ArrayList<Term>(2);
        for (String id : ids) {
            allTerms.add(getGoTerm(id));
        }
        return allTerms;
    }

    private Term getGoTerm(String id) {
        Term term = new GenericTerm();
        term.setZdbID(id);
        return term;
    }

    public void createFirstReport(String[] ids) {
        MutantRepository rep = RepositoryFactory.getMutantRepository();
        List<Term> allTerms = new ArrayList<Term>(50);
        for (String id : ids) {
            Term term = ontologyManager.getTermByID(id);
            if (term != null) {
                allTerms.add(term);
                allTerms.addAll(OntologyService.getAllChildren(term));
            }
            //logger.info("term name: " + term.getTermName());
        }
        System.out.println(allTerms);
        for (Term term : allTerms) {
            List<Phenotype> phenotypes = rep.getPhenotypeWithEntity(term);
            allPhenotypes.addAll(phenotypes);
        }
        reportFile = "paula-first-file.txt";
        createPhenotypeReportLines(allPhenotypes);
    }

    public void createSecondReport(String[] ids) {
        ExpressionRepository rep = RepositoryFactory.getExpressionRepository();
        List<Term> allTerms = new ArrayList<Term>(50);
        for (String id : ids) {
            Term term = ontologyManager.getTermByID(id);
            if (term != null) {
                allTerms.add(term);
                allTerms.addAll(OntologyService.getAllChildren(term));
            }
            //logger.info("term name: " + term.getTermName());
        }
        System.out.println(allTerms);
        for (Term term : allTerms) {
            List<ExpressionResult> phenotypes = rep.getExpressionsWithEntity(term);
            allExpressions.addAll(phenotypes);
        }
        reportFile = "paula-second-file.txt";
        createExpressionReportLines(allExpressions);
    }

    public void createThirdReport(String[] ids) {
        MutantRepository rep = RepositoryFactory.getMutantRepository();
        List<Term> allTerms = new ArrayList<Term>(50);
        for (String id : ids) {
            Term term = ontologyManager.getTermByID(id);
            if (term != null) {
                allTerms.add(term);
                allTerms.addAll(OntologyService.getAllChildren(term));
            }
            //logger.info("term name: " + term.getTermName());
        }
        System.out.println(allTerms);
        for (Term term : allTerms) {
            List<MarkerGoTermEvidence> goTermEvidences = rep.getMarkerGoEvidence(term);
            allMarkerGo.addAll(goTermEvidences);
        }
        reportFile = "paula-third-file.txt";
        createMarkerGOReportLines(allMarkerGo);
    }

    public void createGoReport() {
        MutantRepository rep = RepositoryFactory.getMutantRepository();
        List<Term> allTerms = getGoTerms();
        System.out.println(allTerms);
        for (Term term : allTerms) {
            List<Phenotype> phenotypes = rep.getPhenotypeWithEntity(term);
            allPhenotypes.addAll(phenotypes);
        }
        String name = "";
    }

    private List<StringBuffer> reportLines = new ArrayList<StringBuffer>();

    private void createPhenotypeReportLines(List<Phenotype> phenotypes) {
        if (phenotypes == null)
            return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("Genotype");
        buffer.append("\t");
        buffer.append("Morpholino");
        buffer.append("\t");
        buffer.append("Entity");
        buffer.append("\t");
        buffer.append("Quality ");
        reportLines.add(buffer);
        for (Phenotype phenotype : phenotypes) {
            buffer = new StringBuffer();
            buffer.append(phenotype.getGenotypeExperiment().getGenotype().getName());
            buffer.append("\t");
            Set<ExperimentCondition> conditions = phenotype.getGenotypeExperiment().getExperiment().getMorpholinoConditions();
            if (conditions != null && conditions.size() > 0) {
                for (ExperimentCondition condition : conditions) {
                    buffer.append(condition.getMorpholino().getName());
                    buffer.append(",");
                }
                buffer.deleteCharAt(buffer.length() - 1);
                buffer.append("");
            } else {
                buffer.append(" ");
            }
            buffer.append("\t");
            buffer.append(phenotype.getSuperterm().getTermName());
            if (phenotype.getSubterm() != null) {
                buffer.append(" : ");
                buffer.append(phenotype.getSubterm().getTermName());
            }
            buffer.append("\t");
            buffer.append(phenotype.getTerm().getTermName());
            reportLines.add(buffer);
        }
    }

    private void createExpressionReportLines(List<ExpressionResult> phenotypes) {
        if (phenotypes == null)
            return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID");
        buffer.append("\t");
        buffer.append("Gene");
        buffer.append("\t");
        buffer.append("Antibody");
        buffer.append("\t");
        buffer.append("Fish");
        buffer.append("\t");
        buffer.append("Environment");
        buffer.append("\t");
        buffer.append("Assay");
        buffer.append("\t");
        buffer.append("Structure");
        buffer.append("\t");
        buffer.append("Start");
        buffer.append("\t");
        buffer.append("End");
        buffer.append("\t");
        buffer.append("Expressed");
        reportLines.add(buffer);
        for (ExpressionResult phenotype : phenotypes) {
            buffer = new StringBuffer();
            buffer.append(phenotype.getZdbID());
            buffer.append("\t");
            Marker gene = phenotype.getExpressionExperiment().getGene();
            if (gene != null) {
                buffer.append(gene.getAbbreviation());
            }
            buffer.append("\t");
            if (gene == null) {
                buffer.append(phenotype.getExpressionExperiment().getAntibody().getName());
            }
            buffer.append("\t");
            buffer.append(phenotype.getExpressionExperiment().getGenotypeExperiment().getGenotype().getName());
            buffer.append("\t");
            buffer.append(phenotype.getExpressionExperiment().getGenotypeExperiment().getExperiment().getName());
            buffer.append("\t");
            buffer.append(phenotype.getExpressionExperiment().getAssay().getAbbreviation());
            buffer.append("\t");
            buffer.append(phenotype.getSuperterm().getTermName());
            if (phenotype.getSubterm() != null) {
                buffer.append(" : ");
                buffer.append(phenotype.getSubterm().getTermName());
            }
            buffer.append("\t");
            buffer.append(phenotype.getStartStage().getName());
            buffer.append("\t");
            buffer.append(phenotype.getEndStage().getName());
            buffer.append("\t");
            buffer.append(phenotype.isExpressionFound());
            reportLines.add(buffer);
        }
    }

    private void createMarkerGOReportLines(List<MarkerGoTermEvidence> phenotypes) {
        if (phenotypes == null)
            return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("Gene");
        buffer.append("\t");
        buffer.append("Structure");
        reportLines.add(buffer);
        for (MarkerGoTermEvidence phenotype : phenotypes) {
            buffer = new StringBuffer();
            buffer.append(phenotype.getMarker().getAbbreviation());
            buffer.append("\t");
            buffer.append(phenotype.getGoTerm().getTermName());
            reportLines.add(buffer);
        }
    }

    public void printToFile() {
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(reportFile));
            for (StringBuffer buffer : reportLines) {
                fw.write(buffer.toString());
                fw.write("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    static OntologyManager ontologyManager;
    static final Logger logger = Logger.getLogger(EQReport.class);

    static {
        System.setProperty("java.io.tmpdir", "test/ontologies");
        try {
            ontologyManager = OntologyManager.getEmptyInstance();
            ontologyManager.deserializeRelationships();
            ontologyManager.deserializeOntology(Ontology.ANATOMY);
            ontologyManager.deserializeOntology(Ontology.GO_BP);
        } catch (Exception e) {
            logger.error("failed to load from file: " + ontologyManager, e);
        }
    }
}
