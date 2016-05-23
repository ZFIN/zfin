create function strip_omim(name varchar(200))
  returning varchar(200);
  --remove { } and [ ]
  return replace(replace(replace(replace(name,'{',''),'}',''),'[',''),']','');
end function;



