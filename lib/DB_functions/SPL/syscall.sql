-- for when the results of a system call are not used
-- this may replace sysexec()
  
create dba procedure "informix".syscall (cmd varchar(100), arg varchar(100))
	define command varchar(201);
	let command = cmd || " " ||arg;
	system command;
end procedure;

grant execute on procedure "informix".syscall  to "public" as "informix";


