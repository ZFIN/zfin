-- drop functions

drop function zero_pad(varchar(255), int);
drop function zero_pad(varchar(255));

-- uninstall the Java UDR jar file 

execute procedure remove_jar("zeropad_jar", 0);

