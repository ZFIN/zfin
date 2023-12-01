package org.zfin.uniprot.persistence;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.query.NativeQuery;
import org.zfin.framework.HibernateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * This class is used to bulk load data into a table.
 * It is used to load data into tables that have a ZDB ID column.
 * It will generate ZDB IDs for the new rows based on data type.
 * It will also create entries in the zdb_active_data table for the new ZDB IDs.
 * It will insert the data in batches of 100.
 */
@Log4j2
public class BatchProcessor {

    private static final String TEMP_TABLE_PREFIX = "temp_bulk_load_";
    private static final int BATCH_SIZE = 100;
    private final String baseTableName;
    private final List<Map<String, Object>> rowsOfKeyValuePairsToInsert;
    private final String zdbIdColumnName;
    private final String zdbType;

    //use this to access column names to make sure we get the same order each time
    private final List<String> columnNames;

    /**
     *
     * @param baseTableName The table that will be bulk loaded into.  (eg. db_link)
     * @param zdbIdColumnName The name of the column that will be populated with ZDB IDs.  (eg. dblink_zdb_id)
     * @param zdbType The type of ZDB ID that will be generated.  (eg. DBLINK, MRKRGOEV, etc.)
     * @param rowsOfKeyValuePairsToInsert The data to be inserted.  Each row is a list of key-value pairs.
     *                                    The key is the column name, the value is the value to be inserted.
     */
    public BatchProcessor(String baseTableName,
                          String zdbIdColumnName,
                          String zdbType,
                          List<Map<String, Object>> rowsOfKeyValuePairsToInsert) {
        this.baseTableName = baseTableName;
        this.zdbIdColumnName = zdbIdColumnName;
        this.zdbType = zdbType;
        this.rowsOfKeyValuePairsToInsert = rowsOfKeyValuePairsToInsert;
        this.columnNames = new ArrayList<>(rowsOfKeyValuePairsToInsert.get(0).keySet());
    }

    public final void execute() {
        log.debug("creating temp table for bulk load: " + tempTable());
        createTempTable();

        log.debug("loading data into temp table for bulk load: " + tempTable());
        loadBatchData();

        log.debug("adding ZDB IDs to temp table for bulk load: " + tempTable());
        setZdbIdField();

        log.debug("inserting ZDB IDs into active data table");
        insertActiveData();

        log.debug("inserting data from temp table into base table: " + baseTableName);
        insertFromTempTable();

        log.debug("dropping temp table: " + tempTable());
        dropTempTable();
    }

    private void createTempTable() {
        String sql = String.format(
                "create temp table %s as select * from %s where false",
                tempTable(),
                baseTableName);
        log.debug("create temp table sql: " + sql);
        HibernateUtil.currentSession().createSQLQuery(sql).executeUpdate();
    }

    private void loadBatchData() {
        ListUtils.partition(rowsOfKeyValuePairsToInsert, BATCH_SIZE)
                .forEach((batch) -> loadSingleBatchOfDBLinksToBulkTable(batch));
    }

    /**
     * Generates SQL like:
     * insert into temp_bulk_load_dblink
     * (
     * dblink_acc_num,
     * dblink_linked_recid,
     * )
     * VALUES
     * (?, ?),
     * (?, ?),
     * ...
     * (?, ?)
     *
     * @param rows The data to be inserted.  Each row is a list of key-value pairs that represent a column name and the value for that column.
     */
    private void loadSingleBatchOfDBLinksToBulkTable(List<Map<String, Object>> rows) {
        String sqlOuterTemplate = String.format("""
                insert into %s
                  (
                  %s
                  ) VALUES
                """, tempTable(), getCommaSeparatedColumnNames());

        List<String> sqlInnerTemplates = new ArrayList<>();

        //create rows of '?',
        // eg. (?, ?, ?, ?, ?, ?),
        //     (?, ?, ?, ?, ?, ?),
        //     ...
        String singleRowOfValuesPlaceholder = "(" + getColumnNames().stream().map(c -> "?").collect(Collectors.joining(", ")) + ")";
        rows.forEach(a -> sqlInnerTemplates.add(singleRowOfValuesPlaceholder));
        String sql = sqlOuterTemplate + String.join(",\n", sqlInnerTemplates);

        NativeQuery query = currentSession().createSQLQuery(sql);

        int i = 1;
        for(Map<String, Object> row : rows) {
            for(String columnKey : this.getColumnNames()) {
                query.setParameter(i++, row.get(columnKey));
            }
        }
        query.executeUpdate();
    }

    private void setZdbIdField() {
        String sql = String.format("""
                update %s
                set %s = get_id('%s');
                """, tempTable(), zdbIdColumnName, zdbType);
        currentSession().createSQLQuery(sql).executeUpdate();
    }

    private void insertActiveData() {
        String sql = String.format("""
                insert into zdb_active_data
                select %s from %s
                """, zdbIdColumnName, tempTable());
        currentSession().createSQLQuery(sql).executeUpdate();
    }

    private void insertFromTempTable() {
        String sql = String.format("""
                insert into %s
                select * from %s
                """, baseTableName, tempTable());
        currentSession().createSQLQuery(sql).executeUpdate();
    }

    private void dropTempTable() {
        String sql = String.format("""
                drop table %s
                """, tempTable());
        currentSession().createSQLQuery(sql).executeUpdate();
    }


    // Template method
    private String tempTable() {
        return TEMP_TABLE_PREFIX + baseTableName;
    }

    private String getCommaSeparatedColumnNames() {
        return String.join(", ", getColumnNames());
    }

    private List<String> getColumnNames() {
        return columnNames;
    }
}