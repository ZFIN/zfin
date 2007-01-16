create function get_pub_mini_ref(pubZdbId varchar(50))

  returning varchar(60);

  define authorList       lvarchar;
  define pubYear varchar(15);
  define miniRef varchar(60);
  define lname    varchar(60);
  define srcType    varchar(30);
  define delim    char(1);
  define ch       char(1);
  define len      int;
  define index    int;
  define first    boolean;

  let authorList = "";
  let pubYear = "";
  let miniRef = "";
  let lname = "";
  let srcType = "";
  let ch = "";
        
  let delim = ",";
  let first = "t";

  select authors,jtype, year(pub_date)
    into authorList, srcType,pubYear
    from publication
    where zdb_id = pubZdbId;

  let len = length(authorList);
  for index = 1 to len
    let ch = substr(authorList, index, 1);
    if (ch = delim) and (first) then  
      let lname = substr(authorList,1,(index-1));
      let first = "f";
    elif (ch = delim) and (not first) then    
      let lname = lname || " <i>et al.</i>"; 
      exit for;
    end if    
  end for

  if lname != "" then   
    let miniRef = lname || ", " || pubYear;
  elif (srcType = "Curation" and substr(authorList,1,4) = "ZFIN") then
    let miniRef = "ZFIN Curated Data";
  else	
    let miniRef = authorList;	
  end if

  return miniRef;          

end function;
