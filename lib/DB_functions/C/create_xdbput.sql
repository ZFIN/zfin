create function xdbput(
	blastdb_abbrev varchar(30),
	blastdb_path varchar(255),
	blastdb_type varchar(5),
	accession varchar(50)
)	returning lvarchar
  with (class = 'UGP')
  external name "<!--|ROOT_PATH|-->/lib/DB_functions/xdbput.so"
  language c
  end function;

update statistics for function xdbput;

-- *  It will always return the same results if
--    it is passed the same parameters (NOT VARIANT).
-- * It is able to handle null parameters (HANDLESNULLS).
-- * It is thread safe (PARALLELIZABLE). ... double check

-- http://publib.boulder.ibm.com/infocenter/idshelp/v10/index.jsp?topic=/com.ibm.admin.doc/admin243.htm

-- * run in a protected space  (CLASS = UDR) from the rest of the engine
--   must have line in ONCONFIG similar to:
--   VPCLASS     UDR  ,num=1,noage
