create or replace function setTscriptLoadId(vTscriptMrkrZdbId text, vTscriptLoadId text)  returns varchar as $vTscriptLoadId$ 

begin
       if (vTscriptLoadId is null) then
       	  vTscriptLoadId := vTscriptMrkrZdbId;
       end if;

       return vTscriptLoadId;

end
$vTscriptLoadId$ LANGUAGE plpgsql;
