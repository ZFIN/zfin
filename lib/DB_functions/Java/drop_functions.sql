-- drop functions

drop function pad_string(varchar(255), int);
drop function pad_string(varchar(255));

-- uninstall the Java UDR jar file 

execute procedure remove_jar("zeropad_jar", 0);

