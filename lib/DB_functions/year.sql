create or replace function year(this_date date)
  returns integer as $year$

begin
  return extract(year from this_date);
end;

$year$ LANGUAGE plpgsql;
