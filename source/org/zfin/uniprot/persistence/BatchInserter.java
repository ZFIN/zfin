package org.zfin.uniprot.persistence;

import lombok.Getter;
import lombok.Setter;
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
 * It will insert the data in batches of 100.
 */
@Log4j2
@Getter
@Setter
public class BatchInserter {

    private static final String TEMP_TABLE_PREFIX = "temp_bulk_load_";
    private static final int BATCH_SIZE = 100;
    private final String baseTableName;
    private String tempTableOverride;
    private final List<Map<String, Object>> rowsOfKeyValuePairsToInsert;

    //use this to access column names to make sure we get the same order each time
    private final List<String> columnNames;

    /**
     *
     * @param baseTableName The table that will be bulk loaded into.  (eg. db_link)
     * @param rowsOfKeyValuePairsToInsert The data to be inserted.  Each row is a list of key-value pairs.
     *                                    The key is the column name, the value is the value to be inserted.
     */
    public BatchInserter(String baseTableName,
                          List<Map<String, Object>> rowsOfKeyValuePairsToInsert) {
        this.baseTableName = baseTableName;
        this.rowsOfKeyValuePairsToInsert = rowsOfKeyValuePairsToInsert;
        this.columnNames = new ArrayList<>(rowsOfKeyValuePairsToInsert.get(0).keySet());
    }

    public static void bulkLoadActiveDataZdbIDs(List<String> uniprotIds) {
        String sqlFormat = """
                INSERT INTO zdb_active_data
                (zactvd_zdb_id) VALUES
                ('%s')
                ON CONFLICT (zactvd_zdb_id)
                DO NOTHING
                """;

        List<List<String>> batches = ListUtils.partition(uniprotIds, BATCH_SIZE);
        for(List<String> batch : batches) {
            String combinedUniprotIDs = String.join("'), ('", batch);
            String sql = String.format(sqlFormat, combinedUniprotIDs);
            currentSession().createNativeQuery(sql).executeUpdate();
        }
    }

    public static void bulkInsert(String tableName, List<Map<String, Object>> tuples) {
        BatchInserter inserter = new BatchInserter(tableName, tuples);
        inserter.setTempTableOverride(tableName);
        inserter.loadBatchData();
    }

    public final void execute() {
        log.info("creating temp table for bulk load: " + tempTable());
        createTempTable();

        log.info("loading data into temp table for bulk load: " + tempTable());
        loadBatchData();

        log.info("inserting data from temp table into base table: " + baseTableName);
        insertFromTempTable();

        log.info("dropping temp table: " + tempTable());
        dropTempTable();
    }

    private void createTempTable() {
        String sql = String.format(
                "create temp table %s as select * from %s where false",
                tempTable(),
                baseTableName);
        log.info("create temp table sql: " + sql);
        HibernateUtil.currentSession().createNativeQuery(sql).executeUpdate();
    }

    public void loadBatchData() {
        ListUtils.partition(rowsOfKeyValuePairsToInsert, BATCH_SIZE)
                .forEach(this::loadSingleBatchOfDBLinksToBulkTable);
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

        NativeQuery query = currentSession().createNativeQuery(sql);

        int i = 1;
        for(Map<String, Object> row : rows) {
            for(String columnKey : this.getColumnNames()) {
                query.setParameter(i++, row.get(columnKey));
            }
        }
        query.executeUpdate();
    }

    private void insertFromTempTable() {
        String sql = String.format("""
                insert into %s (%s)
                select %s from %s
                """, baseTableName, getCommaSeparatedColumnNames(), getCommaSeparatedColumnNames(), tempTable());
        currentSession().createNativeQuery(sql).executeUpdate();
    }

    private void dropTempTable() {
        String sql = String.format("""
                drop table %s
                """, tempTable());
        currentSession().createNativeQuery(sql).executeUpdate();
    }


    // Template method
    private String tempTable() {
        if (tempTableOverride != null) {
            return tempTableOverride;
        }
        return TEMP_TABLE_PREFIX + baseTableName;
    }

    private String getCommaSeparatedColumnNames() {
        return String.join(", ", getColumnNames());
    }

    private List<String> getColumnNames() {
        return columnNames;
    }
}