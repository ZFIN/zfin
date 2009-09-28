/*
 * Defines xdbput() function.
 *
 *
 * TODO:
    o stress test.
    o think about other parameters path,start,stop, [others?] multiple programs or 1?
    o think about memory allocation for result
    o what happens when a huge sequence(s) are returned?

 * loaded as a UDR into it's own VPCLASS (see onconfig & create function )
 *   echo "select xdbputa('zfin_cdna,'n',dblink_acc_num)from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-14'" | dbaccess tomdb@wanda
*/

#include <mi.h>
#include <milib.h>

mi_lvarchar *xdbput(
    mi_lvarchar *blastdb_abbrev,
    mi_lvarchar *blastdb_path,
    mi_lvarchar *blastdb_type,
    mi_lvarchar *fasta_file
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
  const int LENGTH = 512000; // read length
  char *return_status; // status string
  mi_lvarchar *mi_return_status ;
  mi_integer fastasize = -1;

  /* TODO: sanity/security check the input */

  /* build up the command string:
  	allow duplicates -d
  	hardcode path for now ...
  */
  sprintf(cmd,
    "/private/apps/wublast/xdformat -%s -a %s/%s /tmp/%s 2>/dev/null",
        mi_lvarchar_to_string(blastdb_type),
        mi_lvarchar_to_string(blastdb_path),
        mi_lvarchar_to_string(blastdb_abbrev),
        mi_lvarchar_to_string(fasta_file)
  );

  /* give the db time to do it's other work */
   mi_yield();

   /* TODO: can this be pulled, or do we need the status here? */
  /* popen creates a pipe so we can read the output of the program we are invoking */
  if (in = popen(cmd, "r")){
     mi_free(cmd);
     /* allocate some space */
     return_status = (char *)mi_alloc(LENGTH);

     mi_yield();
     /* read the output of cmd */
     fastasize = fread(return_status,LENGTH,1,in);
     fflush(in);
     mi_yield();
     /* close the pipe */
     pclose(in);
     mi_yield();
     mi_return_status = mi_string_to_lvarchar(return_status);
     free(return_status);
     return  mi_return_status;
  } else  {
  	mi_free(cmd);
  	return (mi_lvarchar *)NULL;
  }
}
