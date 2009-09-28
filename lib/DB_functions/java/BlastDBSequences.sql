drop function getBlastDBSequencesForDBLink;

execute procedure remove_jar("BlastDBSequences_jar", 0);

execute procedure install_jar(
	"file:<!--|ROOT_PATH|-->/lib/DB_functions/java/BlastDBSequences.jar", "BlastDBSequences_jar", 0);

-- register the Java UDRs

create function getBlastDBSequencesForDBLink(varchar(50))	returning lvarchar
  WITH (class='JVP')
  EXTERNAL NAME 'BlastDBSequences_jar:BlastDBSequences.getSequencesForDBLink(java.lang.String)'
  language java ;
