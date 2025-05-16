package org.zfin.datatransfer.ncbi;

class NCBICharacterizationProcess {
    public static void main(String[] args) {
        NCBICharacterizationProcess tester = new NCBICharacterizationProcess();
        tester.init();
        tester.run();
    }

    private void init() {
        System.out.println("### Make sure DB is Run and reachable by hostname 'db'");
        System.out.println("""
                ### You may also want to set up a private network between db and temporary compile container like:
                docker network create --internal zfin_isolated
                docker network connect zfin_isolated zfin_org-db-1
                cd $SOURCEROOT/server_apps
                docker run -u root --rm -it --network zfin_isolated -v `pwd`:/opt/server_apps   --add-host db:172.19.0.2  zfin_org-compile bash
                """);
    }

    private void run() {
        dumpExistingDatabase();
        waitForInput();

        dumpFunctions();
        waitForInput();

        createTempDatabase();
        waitForInput();

        copyToSchemaBefore();
        waitForInput();

        initializeDocker();
        waitForInput();

        copyToSchemaAfter();
        waitForInput();

        deleteUnnecessaryRows();
        waitForInput();

        compareResultsForRetainedRows();
        waitForInput();

        compareResultsForChangedLengths();
        waitForInput();

        finalAddedResults();
        waitForInput();

        deleteUnnecessaryRowsForAttribution();
        waitForInput();
    }

    private void dumpExistingDatabase() {
        System.out.println("### Run command to capture current state of DB");
        System.out.println("### Only includes the tables that are necessary for ncbi load");
        String command = """
                pg_dump -h db -Fc zfindb -t accession_bank -t accession_bank_accbk_pk_id_seq -t foreign_db_data_type -t marker -t expression_experiment2 -t foreign_db_contains -t foreign_db -t db_link -t marker_relationship -t record_attribution -t reference_protein -t zdb_active_data -t zdb_object_type -t dblink_seq -f ./input/slimdb.bak
                """;
        System.out.println(command);
    }

    private void dumpFunctions() {
        System.out.println("### Run command to capture functions");
        String command = """
                ### This command will dump a minimal set of functions
                psql -h db zfindb -o functions.sql -A -t -c "SELECT pg_get_functiondef(f.oid) || ';' FROM pg_catalog.pg_proc f WHERE proname in ('get_id', 'get_id_and_insert_active_data');"
                
                ###
                ### This command will dump a more extensive set of functions
                ### psql -U postgres -h db zfindb -o functions.sql -A -t -c "SELECT pg_get_functiondef(f.oid) || ';' FROM pg_catalog.pg_proc f WHERE proname in ('get_id', 'get_id_and_insert_active_data','db_link', 'expression_experiment2', 'marker_abbrev_after_update', 'marker_abbrev_insert', 'marker_abbrev_update', 'marker_audit_insert', 'marker_audit_update', 'marker_name_order', 'marker_name', 'marker_relationship', 'marker', 'record_attribution_sync_modified_at', 'record_attribution', 'zdb_object_type', 'p_check_zdb_object_table', 'scrub_char', 'get_dblink_acc_num_display', 'p_dblink_has_parent', 'p_check_caps_acc_num', 'checkDblinkTranscriptWithdrawn', 'checkdblinktranscriptwithdrawn', 'get_genbank_dblink_length_type');"                
                """;
        System.out.println(command);
    }

    private void createTempDatabase() {
        System.out.println("### Run command to create the before state of the database");
        String command = """
                psql -h db -U postgres -c "drop database ncbi WITH (FORCE)"
                psql -h db -U postgres -c "create database ncbi"
                
                ### These commands might fail due to being a partial DB load. Hopefully ignoring is fine
                psql -h db -U postgres -d ncbi -f ./input/functions.sql
                pg_restore -U postgres -h db -d ncbi -c ./input/slimdb.bak
                
                ### Run them again due to chicken/egg issues:
                psql -h db -U postgres -d ncbi -f ./input/functions.sql
                pg_restore -U postgres -h db -d ncbi -c ./input/slimdb.bak
                """;
        System.out.println(command);
    }

    private void copyToSchemaBefore() {
        System.out.println("### Populating temp DB");
        String command = """
                psql -h db -d ncbi <<EOF
                create schema before;
                
                create table before.marker (LIKE public.marker INCLUDING ALL);
                create table before.expression_experiment2 (LIKE public.expression_experiment2 INCLUDING ALL);
                create table before.foreign_db_contains (LIKE public.foreign_db_contains INCLUDING ALL);
                create table before.foreign_db (LIKE public.foreign_db INCLUDING ALL);
                create table before.db_link1 (LIKE public.db_link INCLUDING ALL);
                create table before.marker_relationship (LIKE public.marker_relationship INCLUDING ALL);
                create table before.record_attribution (LIKE public.record_attribution INCLUDING ALL);
                create table before.reference_protein (LIKE public.reference_protein INCLUDING ALL);
                create table before.zdb_active_data (LIKE public.zdb_active_data INCLUDING ALL);
                create table before.zdb_object_type (LIKE public.zdb_object_type INCLUDING ALL);
                
                insert into before.marker select * from  public.marker;
                insert into before.expression_experiment2 select * from  public.expression_experiment2;
                insert into before.foreign_db_contains select * from  public.foreign_db_contains;
                insert into before.foreign_db select * from  public.foreign_db;
                insert into before.db_link1 select * from  public.db_link;
                insert into before.marker_relationship select * from  public.marker_relationship;
                insert into before.record_attribution select * from  public.record_attribution;
                insert into before.reference_protein select * from  public.reference_protein;
                insert into before.zdb_active_data select * from  public.zdb_active_data;
                insert into before.zdb_object_type select * from  public.zdb_object_type;
                EOF
                """;
        System.out.println(command);
    }

    private void initializeDocker() {
        System.out.println("### Run command to capture functions");
        System.out.println("### Replace the db ip address with internal ip address for db container");
        String command = """
                # copy relevant input files into $SOURCEROOT/server_apps/data_transfer/NCBIGENE
                # clear-artifacts.sh
                # cp .......
                # cd into the server_apps directory (necessary due to dependency on perl_libs)
                cd $SOURCEROOT/server_apps
                docker run -u root --rm -it --network zfin_isolated -v `pwd`:/opt/server_apps --add-host db:172.19.0.2 zfin_org-compile bash
                mkdir -p /opt/zfin/source_roots/zfin.org/home/WEB-INF/
                echo 'JAVA_HOME=/opt/java/openjdk' > /opt/zfin/source_roots/zfin.org/home/WEB-INF/zfin.properties
                cd /opt/server_apps/data_transfer/NCBIGENE
                EMAIL_TO_FILE=true \\
                WORKING_DIR=. \\
                NO_SLEEP=1 \\
                SKIP_DOWNLOADS=1 \\
                LOAD_NCBI_ONE_WAY_GENES=false \\
                PGHOST=db \\
                DB_NAME=ncbi \\
                PGUSER=gradle \\
                perl NCBI_gene_load.pl
                """;
        System.out.println(command);
    }

    private void copyToSchemaAfter() {
        System.out.println("### Copy the public schema to a new schema called 'after'");
        System.out.println("### These commands can be run outside of container");
        String command = """
                psql -h db -d ncbi <<EOF
                create schema after;
                
                create table after.marker (LIKE public.marker INCLUDING ALL);
                create table after.expression_experiment2 (LIKE public.expression_experiment2 INCLUDING ALL);
                create table after.foreign_db_contains (LIKE public.foreign_db_contains INCLUDING ALL);
                create table after.foreign_db (LIKE public.foreign_db INCLUDING ALL);
                create table after.db_link2 (LIKE public.db_link INCLUDING ALL);
                create table after.marker_relationship (LIKE public.marker_relationship INCLUDING ALL);
                create table after.record_attribution (LIKE public.record_attribution INCLUDING ALL);
                create table after.reference_protein (LIKE public.reference_protein INCLUDING ALL);
                create table after.zdb_active_data (LIKE public.zdb_active_data INCLUDING ALL);
                create table after.zdb_object_type (LIKE public.zdb_object_type INCLUDING ALL);
                
                insert into after.marker select * from  public.marker;
                insert into after.expression_experiment2 select * from  public.expression_experiment2;
                insert into after.foreign_db_contains select * from  public.foreign_db_contains;
                insert into after.foreign_db select * from  public.foreign_db;
                insert into after.db_link2 select * from  public.db_link;
                insert into after.marker_relationship select * from  public.marker_relationship;
                insert into after.record_attribution select * from  public.record_attribution;
                insert into after.reference_protein select * from  public.reference_protein;
                insert into after.zdb_active_data select * from  public.zdb_active_data;
                insert into after.zdb_object_type select * from  public.zdb_object_type;
                EOF
                """;
        System.out.println(command);
    }

    private void deleteUnnecessaryRows() {
        System.out.println("### This deletes all rows in the db_link table that have not changed in the before and after");
        System.out.println("### Those represent no action that needs to be taken");
        String command = """
                psql -h db ncbi <<EOF
                create temp table to_delete as select dblink_zdb_id as id from (
                select b.dblink_zdb_id
                from after.db_link a join before.db_link b
                  on a.dblink_zdb_id = b.dblink_zdb_id) as subquery;
                
                \\copy (select * from to_delete) to 'ids_same_before_and_after.csv' with csv header;  

                delete from before.db_link where dblink_zdb_id in (select id from to_delete);
                delete from after.reference_protein;                  --need to drop this for foreign keys
                delete from after.expression_experiment2;             --need to drop this for foreign keys
                delete from after.db_link where dblink_zdb_id in (select id from to_delete);
                drop table to_delete;
                EOF
                """;
        System.out.println(command);

    }

    private void compareResultsForRetainedRows() {
        String aColumns = dblinkColumnsWithPrefix("a");
        String bColumns = dblinkColumnsWithPrefix("b");

        System.out.println("### Try this sql to compare the results (matches rows from before and after with the same values except for IDs)");
        System.out.println("### These should be NO-OPs but the current load reloads existing data with new id and timestamp");
        String command = "psql -h db ncbi <<EOF\n" +
                "create table retained_rows as \n" +
                "select " + bColumns + ", " + aColumns +
                """
                from after.db_link a join before.db_link b
                on a.dblink_linked_recid = b.dblink_linked_recid
                and
                a.dblink_acc_num = b.dblink_acc_num
                and
                a.dblink_fdbcont_zdb_id = b.dblink_fdbcont_zdb_id
                and
                ((a.dblink_length = b.dblink_length) or (a.dblink_length is null and b.dblink_length is null))
                AND
                a.dblink_zdb_id <> b.dblink_zdb_id ;
                
                \\copy (select * from retained_rows) to 'retained_rows.csv' with csv header;
                delete from before.db_link where dblink_zdb_id in (select b_dblink_zdb_id from retained_rows);
                delete from after.db_link where dblink_zdb_id in (select a_dblink_zdb_id from retained_rows);
                EOF
                """;
        System.out.println(command);
    }

    private void compareResultsForChangedLengths() {
        String aColumns = dblinkColumnsWithPrefix("a");
        String bColumns = dblinkColumnsWithPrefix("b");

        System.out.println("### Try this sql to compare the results where only the length has changed");
        System.out.println("### These should be UPDATES");
        String command =
                "psql -h db ncbi <<EOF\n" +
                        "create table changed_lengths as " +
                        "select " + bColumns + ", " + aColumns +
                        """
                        from after.db_link a join before.db_link b
                          on a.dblink_linked_recid = b.dblink_linked_recid
                         and
                         a.dblink_acc_num = b.dblink_acc_num
                         and
                         a.dblink_fdbcont_zdb_id = b.dblink_fdbcont_zdb_id
                         and
                         ((a.dblink_length <> b.dblink_length) or (a.dblink_length is null and b.dblink_length is not null) or (a.dblink_length is not null and b.dblink_length is null))
                         AND
                         a.dblink_zdb_id <> b.dblink_zdb_id ;
                         
                         \\copy (select * from changed_lengths) to 'changed_lengths.csv' with csv header;
                         delete from before.db_link where dblink_zdb_id in (select b_dblink_zdb_id from changed_lengths);
                         delete from after.db_link where dblink_zdb_id in (select a_dblink_zdb_id from changed_lengths);
                        EOF
                        """;
        System.out.println(command);
    }

    private void finalAddedResults() {
        System.out.println("### All added db_links");
        String command = """
                 \\copy (select * from after.db_link) to 'added_dblinks.csv' with csv header;
                 \\copy (select * from before.db_link) to 'deleted_dblinks.csv' with csv header;
                """;
        System.out.println(command);
    }

    private void deleteUnnecessaryRowsForAttribution() {
        System.out.println("### Deleting unnecessary rows for attribution (unchanged before/after)");
        String command = """
                create temp table to_del as
                select b.recattrib_pk_id from  before.record_attribution b
                join after.record_attribution a
                on a.recattrib_pk_id = b.recattrib_pk_id;
                
                delete from before.record_attribution where recattrib_pk_id in (select recattrib_pk_id from to_del);
                delete from after.record_attribution where recattrib_pk_id in (select recattrib_pk_id from to_del);
                """;
    }

    private String dblinkColumnsWithPrefix(String prefix) {
        String template = """
                x.dblink_linked_recid as x_dblink_linked_recid, 
                x.dblink_acc_num as x_dblink_acc_num, 
                x.dblink_info as x_dblink_info, 
                x.dblink_zdb_id as x_dblink_zdb_id, 
                x.dblink_acc_num_display as x_dblink_acc_num_display, 
                x.dblink_length as x_dblink_length, 
                x.dblink_fdbcont_zdb_id as x_dblink_fdbcont_zdb_id
                """;
        return template.replaceAll(" x_", " " + prefix + "_").replaceAll("x.", prefix + ".");
    }

    private void waitForInput() {
//        System.out.println("Press enter to continue...");
//        Scanner scanner = new Scanner(System.in);
//        scanner.nextLine();
    }


}