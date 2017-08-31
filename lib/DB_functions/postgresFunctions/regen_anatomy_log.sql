create or replace function  regen_anatomy_log(log_message text)
returning text as $log$

  declare echoCommand text;

  begin 
  echoCommand = 'echo "' || get_time() || ' ' || log_message ||
		       '" >> /tmp/regen_anatomy_log.<!--|DB_NAME|-->';
  system echoCommand;

end ;

$log$ LANGUAGE plpgsql;
