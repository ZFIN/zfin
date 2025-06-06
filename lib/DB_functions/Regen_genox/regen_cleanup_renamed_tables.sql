-- call this method to clean up tables and indexes that were renamed by the regen process
create or replace function regen_cleanup_renamed_tables(table_prefix varchar)
    returns text as $BODY$
DECLARE
    table_name text;
    result text;
BEGIN

    --if table_prefix doesn't contain _fast_search_old_ then it's not a renamed table
    if (table_prefix not like '%_fast_search_old_%') then
        return 'this function should only be used to clean up tables named like ..._fast_search_old_...';
    end if;

    result := 'tables dropped: ';

    -- Drop Tables along with dependent objects
    FOR table_name IN (SELECT tablename FROM pg_tables
                       WHERE tablename LIKE table_prefix || '%'
                         AND schemaname = 'public')
        LOOP
            raise notice 'dropping table %', table_name;
            perform remove_foreign_keys('public', table_name);
            EXECUTE 'DROP TABLE IF EXISTS ' || table_name || ' CASCADE';
            result := result || ' ' || table_name;
        END LOOP;

    RETURN result;
END;
$BODY$ LANGUAGE plpgsql;