-- install the Java UDR jar file 

execute procedure install_jar(
        "file:<!--|ROOT_PATH|-->/lib/DB_functions/Java/zeropad.jar", 
	"zeropad_jar");

-- register the Java UDRs 

create function "informix".zero_pad(varchar(255), int) 
	returns varchar(255)
        external name '<!--|DB_NAME|-->.<!--|DB_OWNER|-->.zeropad_jar:Zeropad.zero_pad(java.lang.String, int)'
        language java;

create function "informix".zero_pad(varchar(255)) 
	returns varchar(255)
	external name '<!--|DB_NAME|-->.<!--|DB_OWNER|-->.zeropad_jar:Zeropad.zero_pad(java.lang.String)'
	language java;

