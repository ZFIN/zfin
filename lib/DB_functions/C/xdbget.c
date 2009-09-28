/*
 * Defines xdbget() function.
 *
 *
 * TODO:
    o stress test.
    o think about other parameters location,start,stop, [others?] multiple programs or 1?
    o think about memory allocation for result
    o what happens when a huge sequence(s) are returned?

 * loaded as a UDR into it's own VPCLASS (see onconfig & create function )
 *   echo "select xdbgeta('zfin_cdna,'n',dblink_acc_num)from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-14'" | dbaccess tomdb@wanda
*/

#include <mi.h>
#include <milib.h>

mi_lvarchar *xdbget(
    mi_lvarchar *blastdb_abbrev,
    mi_lvarchar *blastdb_path,
    mi_lvarchar *blastdb_type,
    mi_lvarchar *accession
    /*,mi_integer seq_len         */
){
  FILE *in;
  char *cmd =(char *)mi_alloc(2040);
  /* make length of lvarchar?
    ...titan or dups could be a problem
    if the range is given we can use that
    titan is ~400,000 bp,
    longest BAC is AL928708=503,239 bp
  */
  const int LENGTH = 512000;
  char *fasta;
  mi_lvarchar *mi_fasta;
  mi_integer fastasize = -1;

  /* TODO: sanity/security check the input */

  /* build up the command string:
  	allow duplicates -d
  */
  sprintf(cmd,
    "/private/apps/wublast/xdget -d -%s %s/%s %s 2>/dev/null",
        mi_lvarchar_to_string(blastdb_type),
        mi_lvarchar_to_string(blastdb_path),
        mi_lvarchar_to_string(blastdb_abbrev),
        mi_lvarchar_to_string(accession)
  );

  /* give the db time to do it's other work */
   mi_yield();

  /* popen creates a pipe so we can read the output of the program we are invoking */
  if (in = popen(cmd, "r")){
     mi_free(cmd);
     /* allocate some space */
     fasta = (char *)mi_alloc(LENGTH);

     /* try wiping the memory locations */
     /*for (int i = LENGTH;i;i--){fasta[i] = '\0';}*/

     mi_yield();
     /* read the output of cmd */
     fastasize = fread(fasta,LENGTH,1,in);
     fflush(in);
     mi_yield();
     /* close the pipe */
     pclose(in);
     mi_yield();
     mi_fasta = mi_string_to_lvarchar(fasta);
     free(fasta);
     return  mi_fasta;
  } else  {
  	mi_free(cmd);
  	return (mi_lvarchar *)NULL;
  }
}
