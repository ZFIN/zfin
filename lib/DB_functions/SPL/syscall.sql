-- for when the results of a system call are not used
-- this may replace sysexec()
  
drop procedure syscall;
create procedure syscall (cmd varchar(100), arg varchar(100))
	define command varchar(201);
	let command = cmd || " " ||arg;
	system command;
end procedure;

update statistics for procedure syscall;
