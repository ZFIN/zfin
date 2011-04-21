/*
 * get_id Generates a new ID by appending a number to a string
*/

#define		_REENTRANT

#include <string.h>
#include <time.h>
#include <mi.h>
#include <milib.h>

#define		MAXLEN		200
#define		SEQ_START	"1"
#define		NO_MEMORY(fun)	mi_db_error_raise(NULL, MI_SQL,\
				"UGEN2", "FUNCTION%s", #fun, NULL)
#define		EXCEPTION(msg)	mi_db_error_raise (NULL, MI_EXCEPTION, msg)


/*
 * Get results of SQL statement and insert into a save set
 * Returns number of rows affected or returned, and -1 for an error.
 */

static int 
get_results (MI_CONNECTION *conn, 
	     MI_SAVE_SET *ss) 
{
  mi_integer      count, result, error;
  MI_ROW          *row;
        
  while ((result = mi_get_result(conn)) != MI_NO_MORE_RESULTS) {
    switch(result) 
      {
      case MI_ERROR:
	EXCEPTION("could not get results (result MI_ERROR)");
	return -1;
	/* break; */
      case MI_DDL:
	break;
      case MI_DML:
	count = mi_result_row_count(conn);
	if (count == MI_ERROR) {
	  EXCEPTION("Can't get row count.\n");
	  return -1;
	}
	break;
      case MI_ROWS:
	row = mi_next_row (conn, &error);
	if (row) {
	  mi_save_set_insert(ss, row);
	}
	break;
      default:
	EXCEPTION("unknown result from mi_get_result");
	break;
      }
  }
  return count;
}


/*
 *      Send SQL statement, returns number of rows returned
 */

static mi_integer 
send_sql(MI_CONNECTION *conn, 
	 MI_SAVE_SET **ss, 
	 char *sql) 
{
  int             count;

  *ss = mi_save_set_create(conn);         /* Create save set */
  if (!*ss) {
    EXCEPTION("Can't create save set\n");
  }
  if (MI_ERROR == mi_exec(conn, sql, 0)) { /* Send SQL statemnet */
    EXCEPTION("Can't send SQL in send_sql\n");
  }
  count = get_results(conn, *ss);                 /* Get results */
  if (!count) {
    count = mi_save_set_count(*ss);
  }
  if (count == MI_ERROR) {
    EXCEPTION("Can't get count of save set in send_sql\n");
  }
  if (MI_ERROR == mi_query_finish(conn)) {
    EXCEPTION("Can't finish query in send_sql\n");
  }
  return count;
}


/* get_id Generates a new unique ID of the format:
 * ZDB-name-YYMMDD-seq, where name is the argument, date is the 
 * current date and seq is unique for that name and date. Seq starts
 * over when the date changes. 
 *Returns an lvarchar.
 */
mi_lvarchar *
get_id(mi_lvarchar *name) 
{
  char		cmdbuf[MAXLEN], daybuf[11], buf[50], *name_s;
  time_t	seconds;
  MI_CONNECTION	*conn;
  MI_SAVE_SET	*ss;
  MI_ROW	*row;
  MI_DATUM	day, num;
  mi_integer	collen, error, daylen;

  if (!(name_s = mi_lvarchar_to_string(name))) {
    NO_MEMORY(get_id);
  }	
  conn = mi_open(NULL, NULL, NULL);	/* Open connection */
  if (conn == NULL) {
    EXCEPTION("ERROR: conn is NULL\n");
  }
 
  sprintf (cmdbuf, 				/* Update sequence */
	   "select %s_seq.nextval from single;",
	   name_s);
  if (send_sql(conn, &ss, cmdbuf) != 1) {

    /* The sequence does not exist.  Must create a new sequence for each new
       type of ZDB id.
     */
    sprintf (cmdbuf,
	     "Attemtpt to generate ZDB ID for unknown object type: %s", name_s);
    EXCEPTION(cmdbuf);
  }
  sprintf (cmdbuf, 			/* Get values */
	   "select zobjtype_day, %s_seq.currval from zdb_object_type where zobjtype_name = \'%s\'",
	   name_s,name_s);
  if (send_sql(conn, &ss, cmdbuf) != 1) {
    EXCEPTION("Cant select row in get_id");
  }
  row = mi_save_set_get_first(ss, &error);
  if (!row) {
    EXCEPTION("Can't get row from save set!");
  }
  mi_value(row, 0, &day, &daylen);
  mi_value(row, 1, &num, &collen);

  time(&seconds);			/* What is today's date? */
  cftime(daybuf, "%m/%d/%Y", &seconds);

  if (strcmp(daybuf, day)) {

    /* last time this name was used was another day so start over */

    sprintf (cmdbuf, 			/* Clear seq number */
	     "update zdb_object_type set zobjtype_day = today where zobjtype_name = \'%s\'; alter sequence %s_seq restart with 2",
	     name_s,name_s,SEQ_START);
    if (send_sql(conn, &ss, cmdbuf) != 1) {			/* Send it */
      EXCEPTION("Cant clear sequence count in get_id");
    }
    strcpy(day, daybuf); num = SEQ_START;
  }

  /* Put date in YYMMDD format */

  if(daylen != 10) {
    EXCEPTION("Date returned is not correct length in get_id");
  }
  strncpy(daybuf, (char *)day+8, 2); strncpy(daybuf+2, day, 2);
  strncpy(daybuf+4, (char *)day+3, 2); daybuf[6] = 0;

  sprintf(buf, "ZDB-%s-%s-%s", name_s, daybuf, num);
  return mi_string_to_lvarchar(buf);
}
