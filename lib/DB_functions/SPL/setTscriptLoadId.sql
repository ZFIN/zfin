create function setTscriptLoadId  (vTscriptMrkrZdbId varchar(50),
       				 vTscriptLoadId varchar(50))

       returning varchar(50);

       if (vTscriptLoadId is null) then
       	  let vTscriptLoadId = vTscriptMrkrZdbId;
       end if;


       return vTscriptLoadId;

end function;