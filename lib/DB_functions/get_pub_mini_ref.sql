-- Builds a publication's short citation ("Lastname <i>et al.</i>, year") from
-- already-known author/type/year values, without touching the publication
-- table. The BEFORE INSERT/UPDATE triggers on publication must call this
-- form with NEW's own values: a trigger that instead looks the row up by
-- zdb_id (see get_pub_mini_ref below) sees the pre-statement row, so it
-- silently computes from stale data whenever authors/jtype/pub_date are
-- being changed by the very statement that fired it (ZFIN-10430).
create or replace function get_pub_mini_ref_from_values(authorList text, srcType text, pubYear text, pubZdbId text)
  returns text as $miniRef$

  declare miniRef text := '';
   lname    text :='' ;
   delim    char(1) := ',';
   ch       char(1) := '';
   index    int := 1;
   first    boolean := 't';
   authorLength int := 0;

  begin

  authorLength = char_length(authorList);

  while index <= authorLength loop
    ch = substring(authorList, index, 1);
    if (ch = delim) and (first) then
      lname = substring(authorList,1,(index-1));
      first = 'f';
    elsif (ch = delim) and (not first) then
      lname = lname || ' <i>et al.</i>';
      exit;
    end if;
    index = index +1 ;
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
$miniRef$ LANGUAGE plpgsql;

-- Back-compat form for callers that only have a zdb_id (ad-hoc queries,
-- historical migrations such as PUB-392.sql). Do NOT call this from the
-- publication INSERT/UPDATE triggers -- see note above.
create or replace function get_pub_mini_ref(pubZdbId text)
  returns text as $miniRef$

  declare authorList text := '';
   pubYear varchar(15) := '' ;
   srcType    varchar(30) :='';

  begin

  select authors, jtype, extract (year from pub_date)
    into authorList, srcType, pubYear
    from publication
    where zdb_id = pubZdbId;

  return get_pub_mini_ref_from_values(authorList, srcType, pubYear, pubZdbId);

end
$miniRef$ LANGUAGE plpgsql;
