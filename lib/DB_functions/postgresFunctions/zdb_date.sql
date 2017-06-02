
create or replace function zdb_date(t date) 
returns char(8) as $resulter$

declare resulter char(8);
    	y varchar(4) := extract(year from t);
	m varchar(2) := extract(month from t);
	d varchar(2) := extract(day from t);

begin 
if length(m) < 2 then 
	 m := '0'|| m;
end if;

if length(d) < 2 then 
	d := '0'|| d;
end if;

resulter := '-' || substring(y from '.{2}$') || m || d || '-';

return resulter;
end

$resulter$ LANGUAGE plpgsql;
