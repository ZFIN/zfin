package org.zfin.database.presentation;

import org.junit.Test;
import org.zfin.util.DatabaseJdbcStatement;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class DatabaseColumnViewControllerTest {

    @Test
    public void testNoJoinedJdbcStatement() {
        DatabaseQueryFormBean formBean = new DatabaseQueryFormBean();
        List<String> columnNames = new ArrayList<String>(1);
        columnNames.add("phenox_fig_zdb_id");
        List<String> columnValues = new ArrayList<String>(1);
        columnValues.add("ZDB-FIG-100910-6");
        formBean.setColumnName(columnNames);
        formBean.setColumnValue(columnValues);
        ForeignKey foreignKey = ForeignKey.PHENOX_FIG;
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
        DatabaseJdbcStatement statement = controller.createJoinedJdbcStatement(formBean, foreignKey, true);

        assertEquals("select count(*) from phenotype_experiment " +
                "where phenox_fig_zdb_id = 'ZDB-FIG-100910-6'", statement.getQuery());

    }

    @Test
    public void testSingleJoinedJdbcStatement() {
        DatabaseQueryFormBean formBean = new DatabaseQueryFormBean();
        List<String> columnNames = new ArrayList<String>(1);
        columnNames.add("phenox_fig_zdb_id");
        List<String> columnValues = new ArrayList<String>(1);
        columnValues.add("ZDB-FIG-100910-6");
        formBean.setColumnName(columnNames);
        formBean.setColumnValue(columnValues);
        ForeignKey foreignKey = ForeignKey.PHENOS_PHENOX;
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
        DatabaseJdbcStatement statement = controller.createJoinedJdbcStatement(formBean, foreignKey, true);

        assertEquals("select count(*) from phenotype_statement, phenotype_experiment " +
                "where phenox_fig_zdb_id = 'ZDB-FIG-100910-6' and phenox_pk_id = phenos_phenox_pk_id", statement.getQuery());

    }

    @Test
    public void testDoubleJoinedJdbcStatement() {
        DatabaseQueryFormBean formBean = new DatabaseQueryFormBean();
        List<String> columnNames = new ArrayList<String>(1);
        columnNames.add("phenox_fig_zdb_id");
        List<String> columnValues = new ArrayList<String>(1);
        columnValues.add("ZDB-FIG-100910-6");
        formBean.setColumnName(columnNames);
        formBean.setColumnValue(columnValues);
        ForeignKey foreignKey = ForeignKey.PHENOX_TERM_1_A;
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
        DatabaseJdbcStatement statement = controller.createJoinedJdbcStatement(formBean, foreignKey, true);

        assertEquals("select count(*) from phenotype_experiment, phenotype_statement, term where " +
                "phenox_fig_zdb_id = 'ZDB-FIG-100910-6' and phenox_pk_id = phenos_phenox_pk_id and phenos_entity_1_superterm_zdb_id = term_zdb_id", statement.getQuery());

    }

/*
    @Test
    public void testIndirectForeignKey() {
        ForeignKey foreignKey = ForeignKey.WH_FAS_PFIGG;
        Integer id = 428;
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
        DatabaseJdbcStatement statement = controller.createJdbcStatement(id, foreignKey, true);

        assertEquals("select count(*) from phenotype_figure_group, fish_annotation_search where fas_pk_id = 428 and " +
                "fas_pheno_figure_group = pfigg_group_name", statement.getQuery());

    }

    @Test
    public void testIndirectForeignKeyDetails() {
        DatabaseQueryFormBean formBean = new DatabaseQueryFormBean();
        List<String> columnNames = new ArrayList<String>(1);
        columnNames.add(ForeignKey.WH_FAS_PFIGG.getForeignKey());
        List<String> columnValues = new ArrayList<String>(1);
        Integer id = 428;
        columnValues.add("428");
        formBean.setColumnName(columnNames);
        formBean.setColumnValue(columnValues);
        ForeignKey foreignKey = ForeignKey.WH_FAS_PFIGG;
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
        DatabaseJdbcStatement statement = controller.createJoinedJdbcStatement(formBean, foreignKey, false);

        assertEquals("select * from phenotype_figure_group, fish_annotation_search where fas_pk_id = '428' and " +
                "fas_pheno_figure_group = pfigg_group_name", statement.getQuery());

    }
*/

    @Test
    public void testTripleJoinedJdbcStatement() {
        DatabaseQueryFormBean formBean = new DatabaseQueryFormBean();
        List<String> columnNames = new ArrayList<String>(1);
        columnNames.add("fig_source_zdb_id");
        List<String> columnValues = new ArrayList<String>(1);
        columnValues.add("ZDB-PUB-010705-7");
        formBean.setColumnName(columnNames);
        formBean.setColumnValue(columnValues);
        ForeignKey foreignKey = ForeignKey.PHENOX_TERM_1_A;
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
        DatabaseJdbcStatement statement = controller.createJoinedJdbcStatement(formBean, foreignKey, true);

        assertEquals("select count(*) from figure, phenotype_experiment, phenotype_statement, term where " +
                "fig_source_zdb_id = 'ZDB-PUB-010705-7' and fig_zdb_id = phenox_fig_zdb_id and " +
                "phenox_pk_id = phenos_phenox_pk_id and phenos_entity_1_superterm_zdb_id = term_zdb_id", statement.getQuery());

    }

    @Test
    public void testTripleJoinedJdbcStatementDoubleTable() {
        DatabaseQueryFormBean formBean = new DatabaseQueryFormBean();
        List<String> columnNames = new ArrayList<String>(1);
        columnNames.add("xpatres_xpatex_zdb_id");
        List<String> columnValues = new ArrayList<String>(1);
        columnValues.add("ZDB-GENOX-090731-5");
        formBean.setColumnName(columnNames);
        formBean.setColumnValue(columnValues);
        ForeignKey foreignKey = ForeignKey.RECORD_ATTR_GENOX;
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
        DatabaseJdbcStatement statement = controller.createJoinedJdbcStatement(formBean, foreignKey, true);

        assertEquals("select count(*) from figure, phenotype_experiment, phenotype_statement, term where " +
                "fig_source_zdb_id = 'ZDB-PUB-010705-7' and fig_zdb_id = phenox_fig_zdb_id and " +
                "phenox_pk_id = phenos_phenox_pk_id and phenos_entity_1_superterm_zdb_id = term_zdb_id", statement.getQuery());

    }
}
