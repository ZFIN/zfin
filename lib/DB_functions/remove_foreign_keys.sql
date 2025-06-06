create or replace function remove_foreign_keys(schema_name text, table_name text)
returns void as $$
declare
fk record;
begin
for fk in
select conname
from pg_constraint
where contype = 'f'
  and conrelid = (quote_ident(schema_name) || '.' || quote_ident(table_name))::regclass
    loop
        raise notice 'dropping foreign key from table %I : %I', table_name, fk.conname;
        execute format('alter table %I.%I drop constraint %I', schema_name, table_name, fk.conname);
    end loop;
end;
$$ language plpgsql;