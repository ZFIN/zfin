drop function signature;
drop function count_down;
execute procedure remove_jar("env_jar", 0);

--create database mydb;

-- install the Java UDR jar file (customize the URL for your installation)

execute procedure install_jar(
	"file:/research/zcentral/www_homes/hoover/lib/DB_functions/java/Env.jar", "env_jar", 0);

-- register the Java UDRs

create function signature(int, char(20), varchar(20), boolean) 
	returns lvarchar
        external name 'env_jar:Env.signature(int, java.lang.String, java.lang.String, boolean)'
        language java;

create function count_down(int) returns lvarchar
	with (iterator)
        external name 'env_jar:Env.countDown(int)'
        language java;

-- test the Java UDRs 

execute function signature(1, "2", "3", "t");

execute function count_down(10);