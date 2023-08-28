create or replace function regen_genofig_finish_cleanup()
returns text as $BODY$
DECLARE
    table_name text;
    index_name text;
    result text;
BEGIN
    result := 'indexes dropped: ';

    FOR index_name in (SELECT indexname FROM pg_indexes
                        WHERE indexname LIKE 'genotype_figure_fast_search_old_%'
                        AND schemaname = 'public')
    LOOP
        raise notice 'dropping index %', index_name;
        EXECUTE 'DROP INDEX IF EXISTS ' || index_name;
        result := result || ' ' || index_name;
    END LOOP;

    result := result || '; ';

    result := result || ' ' ||  'tables dropped: ';
    FOR table_name IN (SELECT tablename FROM pg_tables
                       WHERE tablename LIKE 'genotype_figure_fast_search_old_%'
                       AND schemaname = 'public')
    LOOP
        raise notice 'dropping table %', table_name;
        EXECUTE 'DROP TABLE IF EXISTS ' || table_name;
        result := result || ' ' || table_name;
    END LOOP;

    RETURN result;
END;
$BODY$ LANGUAGE plpgsql;
