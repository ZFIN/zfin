create or replace function get_pub_mini_ref(pubZdbId text)
  returns text as $miniRef$

  declare authorList text := '';
   pubYear varchar(15) := '' ;
   miniRef varchar(60) := '';
   lname    varchar(60) :='' ;
   srcType    varchar(30) :='';
   delim    char(1);
   ch       char(1) := '';
   len      int;
   index    int := 1;
   first    boolean := 't';
   authorLength int := 0;

  begin 

  select authors,jtype, extract (year from pub_date)
    into authorList, srcType,pubYear
    from publication
    where zdb_id = pubZdbId;

  authorLength = char_length(authorList);

  len = char_length(authorList);
  while index <= authorLength loop
    ch = substring(authorList, index, 1);
    if (ch = delim) and (first) then  
      lname = substring(authorList,1,(index-1));
      first = 'f';
    elsif (ch = delim) and (not first) then    
      lname = lname || ' <i>et al.</i>'; 
      exit;
    end if;   
  end loop;

  if lname != '' then   
    miniRef = lname || ', ' || pubYear;
  elsif (srcType = 'Curation' and substring(authorList,1,4) = 'ZFIN') and pubZdbId not in ('ZDB-PUB-020723-1','ZDB-PUB-031118-3','ZDB-PUB-020724-1') then
    miniRef = 'ZFIN Curated Data';
  elsif (srcType = 'Curation' and substring(authorList,1,4) = 'ZFIN') and pubZdbId in ('ZDB-PUB-020723-1','ZDB-PUB-031118-3','ZDB-PUB-020724-1') then
    miniRef = 'ZFIN Electronic Annotation';
  else	
    miniRef = authorList;	
  end if;

  return miniRef;          

end
$miniRef$ LANGUAGE plpgsql
