create or replace function strip_omim(name varchar(200))
  returns varchar(200) as $replaced$

begin
  --remove { } and [ ]
  return replace(replace(replace(replace(name,'{',''),'}',''),'[',''),']','');

end



$replaced$ LANGUAGE plpgsql;
