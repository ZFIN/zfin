-- install the Java UDR jar file 

execute procedure install_jar(
        "file:<!--|ROOT_PATH|-->/lib/DB_functions/Java/zeropad.jar", 
	"kevdb.kschaper.zeropad_jar", 0);

-- register the Java UDRs 

create function pad_string(varchar(255), int) 
	returns varchar(255)
        external name 'kevdb.kschaper.zeropad_jar:Zeropad.pad_string(java.lang.String, int)'
        language java;

create function pad_string(varchar(255)) 
	returns varchar(255)
	external name 'kevdb.kschaper.zeropad_jar:Zeropad.pad_string(java.lang.String)'
	language java;

