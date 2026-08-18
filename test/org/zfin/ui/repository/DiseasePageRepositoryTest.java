package org.zfin.ui.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

/**
 * Guards the HQL in {@link HibernateDiseasePageRepository} against attribute paths Hibernate
 * cannot resolve. Two of these queries have failed in production with
 * org.hibernate.query.sqm.UnknownPathException -- fishModelDisplay.publication (#1933) and
 * chebiDisplay.term (ZFIN-10412) -- both because an order-by referenced a getter that is not a
 * mapped attribute. createQuery() parses the entire HQL string, so merely calling each method
 * catches that whole class of bug; the assertions on the returned results are secondary.
 * <p>
 * Each query is exercised in three shapes, because each builds a different HQL string:
 * the direct branch (where ... = :term), the include-children branch (where ... in :ids), and
 * the direct branch with a populated filter map -- whose keys are themselves HQL paths appended
 * as LOWER(key) like '...', using the same keys TermAPIController passes.
 * <p>
 * Terms are discovered from the ui.* tables rather than hardcoded: the direct branch needs a
 * term some row points at, and the include-children branch needs one that appears in a row's
 * ancestor array (otherwise findIdsByAncestor short-circuits and the HQL is never built). Both
 * helpers pick the term matching the FEWEST rows so the queries stay cheap. If the ui.* table
 * is empty (unloaded database) the test skips rather than fails.
 */
public class DiseasePageRepositoryTest extends AbstractDatabaseTest {

    private final DiseasePageRepository repository = RepositoryFactory.getDiseasePageRepository();

    @Test
    public void getGenesInvolvedResolvesAllPaths() {
        GenericTerm term = directTerm("ui.omim_phenotype_display", "opd_term_zdb_id");
        assertNotNull(repository.getGenesInvolved(term, new Pagination(), false));
        assertNotNull(repository.getGenesInvolved(term, filterOn(
                "omimPhenotype.homoSapiensGene.symbol",
                "omimPhenotype.name",
                "omimPhenotype.disease.termName",
                "omimPhenotype.omimAccession",
                "zfinGene.abbreviation"), false));

        GenericTerm ancestor = ancestorTerm("ui.omim_phenotype_display", "opd_ancestor_term_ids");
        assertNotNull(repository.getGenesInvolved(ancestor, new Pagination(), true));
    }

    @Test
    public void getPhenotypeResolvesAllPaths() {
        GenericTerm term = directTerm("ui.term_phenotype_display", "tpd_term_zdb_id");
        // isIncludeNormalPhenotype toggles an extra "AND phenoStats.tag = 'abnormal'" clause.
        assertNotNull(repository.getPhenotype(term, new Pagination(), false, false));
        assertNotNull(repository.getPhenotype(term, new Pagination(), false, true));
        assertNotNull(repository.getPhenotype(term, filterOn(
                "fishStat.geneSymbolSearch",
                "fishStat.fish.name",
                "fishStat.phenotypeStatementSearch",
                "fishStat.term.termName"), false, false));

        GenericTerm ancestor = ancestorTerm("ui.term_phenotype_display", "tpd_ancestor_term_ids");
        assertNotNull(repository.getPhenotype(ancestor, new Pagination(), true, false));
    }

    @Test
    public void getFishDiseaseModelsResolvesAllPaths() {
        GenericTerm term = directTerm("ui.zebrafish_models_display", "zmd_term_zdb_id");
        assertNotNull(repository.getFishDiseaseModels(term, new Pagination(), false));
        assertNotNull(repository.getFishDiseaseModels(term, filterOn(
                "fishModelDisplay.fish.displayName",
                "fishModelDisplay.disease.termName",
                "fishModelDisplay.conditionSearch"), false));

        GenericTerm ancestor = ancestorTerm("ui.zebrafish_models_display", "zmd_ancestor_term_ids");
        assertNotNull(repository.getFishDiseaseModels(ancestor, new Pagination(), true));
    }

    /**
     * The ZFIN-10412 regression: the order-by referenced chebiDisplay.term, but
     * ChebiFishModelDisplay maps the CHEBI term as `chebi` (omca_term_zdb_id).
     */
    @Test
    public void getFishDiseaseChebiModelsResolvesAllPaths() {
        GenericTerm term = directTerm("ui.zebrafish_models_chebi_association", "omca_term_zdb_id");
        assertNotNull(repository.getFishDiseaseChebiModels(term, false));

        GenericTerm ancestor = ancestorTerm("ui.zebrafish_models_chebi_association", "omca_ancestor_term_ids");
        assertNotNull(repository.getFishDiseaseChebiModels(ancestor, true));
    }

    @Test
    public void getPhenotypeChebiResolvesAllPaths() {
        GenericTerm term = directTerm("ui.chebi_phenotype_display", "cpd_term_zdb_id");
        assertNotNull(repository.getPhenotypeChebi(term, new Pagination(), null, false));
        assertNotNull(repository.getPhenotypeChebi(term, filterOn(
                "chebiPhenotype.conditionSearch",
                "chebiPhenotype.amelioratedExacerbatedPhenoSearch",
                "chebiPhenotype.fish.name",
                "chebiPhenotype.phenotypeStatementSearch",
                "chebiPhenotype.expConditionChebiSearch"), null, false));

        GenericTerm ancestor = ancestorTerm("ui.chebi_phenotype_display", "cpd_ancestor_term_ids");
        assertNotNull(repository.getPhenotypeChebi(ancestor, new Pagination(), null, true));
    }

    /**
     * The include-children branch must not bind one parameter per matching row: Postgres caps a
     * PreparedStatement at 65,535 parameters, and the widest ontology terms match far more rows
     * than that (86,664 for the widest phenotype term when this was written). Binding the ids
     * individually made /action/api/ontology/<term>/phenotype a hard 500 in production for the 11
     * terms over the limit, so exercise the WIDEST term in each table rather than a convenient one.
     * <p>
     * The all-terms case is the only one that can trip the limit -- the direct branch binds a
     * single term -- so this test only covers includeChildren=true.
     */
    @Test
    public void includeChildrenDoesNotBindOneParameterPerRow() {
        assertNotNull(repository.getGenesInvolved(
            widestTerm("ui.omim_phenotype_display", "opd_ancestor_term_ids"), new Pagination(), true));
        assertNotNull(repository.getPhenotype(
            widestTerm("ui.term_phenotype_display", "tpd_ancestor_term_ids"), new Pagination(), true, false));
        assertNotNull(repository.getFishDiseaseModels(
            widestTerm("ui.zebrafish_models_display", "zmd_ancestor_term_ids"), new Pagination(), true));
        assertNotNull(repository.getFishDiseaseChebiModels(
            widestTerm("ui.zebrafish_models_chebi_association", "omca_ancestor_term_ids"), true));
        assertNotNull(repository.getPhenotypeChebi(
            widestTerm("ui.chebi_phenotype_display", "cpd_ancestor_term_ids"), new Pagination(), null, true));
    }

    /** A Pagination whose filter map holds every given HQL path, so each lands in the query. */
    private Pagination filterOn(String... hqlPaths) {
        Pagination pagination = new Pagination();
        for (String path : hqlPaths) {
            pagination.addToFilterMap(path, "zzz");
        }
        return pagination;
    }

    /** The term that the fewest rows of this table point at directly. */
    private GenericTerm directTerm(String table, String termColumn) {
        return loadTerm(firstValue(
                "select " + termColumn + " from " + table +
                " where " + termColumn + " is not null" +
                " group by " + termColumn + " order by count(*) limit 1"));
    }

    /** The term appearing in the fewest rows' ancestor arrays -- keeps the query cheap. */
    private GenericTerm ancestorTerm(String table, String ancestorColumn) {
        return loadTerm(firstValue(
                "select t from " + table + ", unnest(" + ancestorColumn + ") t" +
                " group by t order by count(*) limit 1"));
    }

    /** The term appearing in the MOST rows' ancestor arrays -- the parameter-limit worst case. */
    private GenericTerm widestTerm(String table, String ancestorColumn) {
        return loadTerm(firstValue(
                "select t from " + table + ", unnest(" + ancestorColumn + ") t" +
                " group by t order by count(*) desc limit 1"));
    }

    private GenericTerm loadTerm(String termZdbID) {
        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(termZdbID);
        assertNotNull("no term row for " + termZdbID, term);
        return term;
    }

    // The SQL carries its own `limit 1`; setMaxResults would append a second limit clause.
    private String firstValue(String sql) {
        List<String> rows = HibernateUtil.currentSession()
                .createNativeQuery(sql, String.class)
                .getResultList();
        assumeFalse("no data for: " + sql, rows.isEmpty());
        return rows.get(0);
    }
}
