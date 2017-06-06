create or replace function get_pub_mini_ref(pubZdbId varchar(50))

  returns varchar(60) as miniRef

  declare authorList       text :='';
   pubYear varchar(15) :='' ;
   miniRef varchar(60) :='';
   lname    varchar(60) :='' ;
   srcType    varchar(30) :='';
   delim    char(1);
   ch       char(1) :='';
   len      int;
   index    int;
   first    boolean :='t';

  begin 

  select authors,jtype, year(pub_date)
    into authorList, srcType,pubYear
    from publication
    where zdb_id = pubZdbId;

  len = length(authorList);
  for index = 1 to len
    ch = substr(authorList, index, 1);
    if (ch = delim) and (first) then  
      lname = substr(authorList,1,(index-1));
      first = "f";
    elif (ch = delim) and (not first) then    
      lname = lname || " <i>et al.</i>"; 
      exit for;
    end if;   
  end for;

  if lname != "" then   
    miniRef = lname || ", " || pubYear;
  elsif (srcType = "Curation" and substr(authorList,1,4) = "ZFIN") and pubZdbId not in ('ZDB-PUB-020723-1','ZDB-PUB-031118-3','ZDB-PUB-020724-1') then
    miniRef = "ZFIN Curated Data";
  elsif (srcType = "Curation" and substr(authorList,1,4) = "ZFIN") and pubZdbId in ('ZDB-PUB-020723-1','ZDB-PUB-031118-3','ZDB-PUB-020724-1') then
    miniRef = "ZFIN Electronic Annotation";
  else	
    miniRef = authorList;	
  end if;

  return miniRef;          

end function;
