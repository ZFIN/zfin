package org.zfin.expression.task;

import org.apache.commons.lang3.ObjectUtils;
import org.biojava.bio.BioException;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.expression.ExpressionFigureStage;
import org.zfin.expression.ExpressionResult2;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.framework.HibernateUtil.flushAndCommitCurrentSession;
import static org.zfin.framework.HibernateUtil.createTransaction;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.util.ZfinStringUtils.sortedZdbIDs;

//TODO: Remove this class once the task is complete
// This class is a task to merge duplicate expression experiments
// It will merge the experiments so that only one remains of each set of duplicates
// It should only need to be run once, and then the unique constraint will prevent future duplicates

public class ExpressionExperimentAnalysisTask extends AbstractScriptWrapper {

    public static void main(String[] args) {
        ExpressionExperimentAnalysisTask task = new ExpressionExperimentAnalysisTask();

        try {
            task.runTask();
        } catch (Exception e) {
            System.err.println("Exception Error while running task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        HibernateUtil.closeSession();
    }

    public void runTask() throws IOException, BioException, SQLException {
        initAll();

        List<ExpressionExperiment2> experimentsToDelete = new ArrayList<>();
        List<ExpressionExperimentDuplicate> duplicateReportRows = rowsOfExperimentsWithDuplicates();
        System.out.println("Found " + duplicateReportRows.size() + " rows with duplicates");

        mergeAllDuplicateExperiments(duplicateReportRows, experimentsToDelete);
        deleteExperiments(experimentsToDelete);
        createConstraint();
        printReport(duplicateReportRows);
    }

    private void printReport(List<ExpressionExperimentDuplicate> duplicateReportRows) {
        //create a temp file to write the report to
        File reportFile = new File("expression_experiment_duplicates_report.csv");
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(reportFile)))) {
            writer.println("Assay Name,ATB ZDB ID,Gene ZDB ID,Genox ZDB ID,Probe Feature ZDB ID,Source ZDB ID,Experiment ZDB IDs");
            for (ExpressionExperimentDuplicate row : duplicateReportRows) {
                writer.println(row.xpatex_assay_name() + "," + row.xpatex_atb_zdb_id() + "," + row.xpatex_gene_zdb_id() + "," + row.xpatex_genox_zdb_id() + "," + row.xpatex_probe_feature_zdb_id() + "," + row.xpatex_source_zdb_id() + ",\"" + row.ids() + "\"");
            }
        } catch (IOException e) {
            System.err.println("Error while writing report: " + e.getMessage());
        }
        System.out.println("Report written to: " + reportFile.getAbsolutePath());
    }

    private void mergeAllDuplicateExperiments(List<ExpressionExperimentDuplicate> results, List<ExpressionExperiment2> experimentsToDelete) {
        for(ExpressionExperimentDuplicate row : results) {
            createTransaction();

            System.out.println("\nProcessing duplicates: " + row.ids() + " (PUB: " + row.xpatex_source_zdb_id() + ")");
            List<ExpressionExperiment2> experiments = row.ids().stream().map(id -> getExpressionRepository().getExpressionExperiment(id)).toList();

            ExpressionExperiment2 canonical = null;
            int duplicateCanonicalsCount = 0;
            for(ExpressionExperiment2 experiment : experiments) {
                Set<ExpressionFigureStage> figureStages = experiment.getFigureStageSet();
                for(ExpressionFigureStage figureStage : figureStages) {
                    Set<ExpressionResult2> expressionResults = figureStage.getExpressionResultSet();
                    if (expressionResults.size() > 0) {
                        if (canonical == null) {
                            canonical = experiment;
                        } else if (canonical.equals(experiment)) {
                            //ignore
                        } else {
                            System.out.println("  Duplicate canonical candidates: " + experiment.getZdbID() + " " + canonical.getZdbID());
                            mergeExperiments(experiment, canonical);
                            duplicateCanonicalsCount++;
                        }
                    }
                }
            }

            if(canonical == null) {
                canonical = experiments.get(0);
                System.out.println("  No canonical selected for: " + row.ids());
                System.out.println("  Setting arbitrary canonical (based on oldest): " + canonical.getZdbID());
            } else if (duplicateCanonicalsCount > 0) {
                System.out.println("  Merged multiple experiments: " + duplicateCanonicalsCount);
                System.out.println("  Canonical: " + canonical.getZdbID());
            } else {
                System.out.println("  Canonical: " + canonical.getZdbID());
            }
            //delete the non-canonical experiment (not sure if this will cascade -- so we may need to do it in above loop)
            System.out.println("  Removing non-canonical experiments");
            for(ExpressionExperiment2 experiment : experiments) {
                if (!ObjectUtils.equals(experiment.getZdbID(), canonical.getZdbID())) {
                    System.out.println("    Marking for Deletion: " + experiment.getZdbID());
                    experimentsToDelete.add(experiment);
                }
            }

            try {
                flushAndCommitCurrentSession();
            } catch (Exception e) {
                System.out.println("  Error while saving: " + e.getMessage());
                e.printStackTrace();
                currentSession().clear();
                currentSession().getTransaction().rollback();
            }
        }
    }

    private static void deleteExperiments(List<ExpressionExperiment2> experimentsToDelete) {
        System.out.println("Deleting " + experimentsToDelete.size() + " experiments");
        currentSession().clear();
        for(ExpressionExperiment2 experiment : experimentsToDelete) {
            createTransaction();
            System.out.println("  Deleting: " + experiment.getZdbID());
            currentSession().createNativeQuery("DELETE FROM expression_experiment2 WHERE xpatex_zdb_id = :zdbID")
                    .setParameter("zdbID", experiment.getZdbID())
                    .executeUpdate();
            flushAndCommitCurrentSession();
        }
    }


    //keep the canonical
    private void mergeExperiments(ExpressionExperiment2 experiment, ExpressionExperiment2 canonical) {
        System.out.println("  Merging: " + experiment.getZdbID() + " into " + canonical.getZdbID());
        Set<ExpressionFigureStage> canonicalFigureStages = canonical.getFigureStageSet();
        for(ExpressionFigureStage figureStage : experiment.getFigureStageSet()) {

            //compare the stage start, stage end, and the figure
            if (containsStage(canonicalFigureStages, figureStage)) {

                //if they are the same, we don't need to copy it, otherwise, we need to copy/merge them into the canonical one
                ExpressionFigureStage matchingCanonicalStage = getMatchingStage(canonicalFigureStages, figureStage);

                //do another level of comparison for the expressionResultSet (using expressionFound, SuperTerm and SubTerm)
                //loop over more
                for(ExpressionResult2 result : figureStage.getExpressionResultSet()) {
                    if (!containsExpressionResult(matchingCanonicalStage.getExpressionResultSet(), result)) {
                        System.out.println("    Merging Expression Result Set (" + result + ") for experiment: " + experiment.getZdbID() + " into " + canonical.getZdbID() + " (for PUB " + experiment.getPublication().getZdbID() + ")" );
                        matchingCanonicalStage.getExpressionResultSet().add(result);
                    }
                }
            } else {
                System.out.println("    Merging Figure Stages for experiments: " + experiment.getZdbID() + " into " + canonical.getZdbID() + " (for PUB " + experiment.getPublication().getZdbID() + ")" );
                canonical.getFigureStageSet().add(figureStage);
            }
        }
        //save canonical
        currentSession().save(canonical);
    }

    private boolean containsExpressionResult(Set<ExpressionResult2> expressionResultSet, ExpressionResult2 result) {
        for(ExpressionResult2 canonicalResult : expressionResultSet) {
            if (canonicalResult.getSuperTerm().equals(result.getSuperTerm()) &&
                    Objects.equals(canonicalResult.getSubTerm(), result.getSubTerm()) &&
                    canonicalResult.isExpressionFound() == result.isExpressionFound()) {
                return true;
            }
        }
        return false;
    }

    private boolean containsStage(Set<ExpressionFigureStage> canonicalFigureStages, ExpressionFigureStage figureStage) {
        return getMatchingStage(canonicalFigureStages, figureStage) != null;
    }

    private ExpressionFigureStage getMatchingStage(Set<ExpressionFigureStage> canonicalFigureStages, ExpressionFigureStage figureStage) {
        for(ExpressionFigureStage canonicalStage : canonicalFigureStages) {
            if (canonicalStage.getStartStage().equals(figureStage.getStartStage()) &&
                    canonicalStage.getEndStage().equals(figureStage.getEndStage()) &&
                    canonicalStage.getFigure().equals(figureStage.getFigure())) {
                return canonicalStage;
            }
        }
        return null;
    }

    private List<ExpressionExperimentDuplicate> rowsOfExperimentsWithDuplicates() {
        String sql = """
                    SELECT
                        *
                    FROM (
                        SELECT
                            xpatex_assay_name,
                            xpatex_probe_feature_zdb_id,
                            xpatex_gene_zdb_id,
                            xpatex_genox_zdb_id,
                            xpatex_atb_zdb_id,
                            xpatex_source_zdb_id,
                            string_agg(xpatex_zdb_id, ',' ORDER BY xpatex_zdb_id) AS ids,
                            count(*) AS numrows
                        FROM
                            expression_experiment2
                        GROUP BY
                            xpatex_assay_name,
                            xpatex_probe_feature_zdb_id,
                            xpatex_gene_zdb_id,
                            xpatex_genox_zdb_id,
                            xpatex_atb_zdb_id,
                            xpatex_source_zdb_id
                        HAVING
                            count(*) > 1) subq
                    ORDER BY
                        ids DESC
--                        LIMIT 50 --TODO: remove this
                """;
        List results = currentSession().createNativeQuery(sql).list();
        return results.stream().map(row -> ExpressionExperimentDuplicate.fromTupleArray((Object[])row)).toList();
    }

    private static void createConstraint() {
        //create a constraint to prevent future duplicates
        System.out.println("Creating unique constraint");
        try {
            HibernateUtil.createTransaction();
            String sql = """
                    ALTER TABLE expression_experiment2
                    ADD CONSTRAINT unique_expression_experiment2
                    UNIQUE (xpatex_assay_name, xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id, xpatex_genox_zdb_id, xpatex_atb_zdb_id, xpatex_source_zdb_id)
                    """;
            currentSession().createNativeQuery(sql).executeUpdate();
            flushAndCommitCurrentSession();
        } catch (Exception e) {
            System.out.println("Error while creating constraint: " + e.getMessage());
            e.printStackTrace();
            currentSession().getTransaction().rollback();
        }
    }

    public record ExpressionExperimentDuplicate(String xpatex_assay_name, String xpatex_probe_feature_zdb_id, String xpatex_gene_zdb_id, String xpatex_genox_zdb_id, String xpatex_atb_zdb_id, String xpatex_source_zdb_id, List<String> ids) {
        public static ExpressionExperimentDuplicate fromTupleArray(Object[] o) {
            String idsString = (String) o[6];
            List<String> ids = Arrays.asList(idsString.split(","));
            List<String> sortedIds = sortedZdbIDs(ids);

            return new ExpressionExperimentDuplicate(
                    (String) o[0],
                    (String) o[1],
                    (String) o[2],
                    (String) o[3],
                    (String) o[4],
                    (String) o[5],
                    sortedIds
            );
        }
    }
}
