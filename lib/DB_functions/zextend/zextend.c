/*
	ZFIN database external user defined functions.
	
	lower		Lower cases an lvarchar
	upper		Upper cases an lvarchar
	sysexec		runs a system program and returns results from STDOUT
	replace		replaces N occurrences of one substring with another
	get_id		Generates a new ID by appending a number to a string
	concat		Concatenates to strings, the Informix concat seems brokeen
	html_breaks	Replaces newlines with "<P>" which formats it for html
	now		returns current timestamp
	expr		returns it's single argument to get the parser to eval it
	get_random_cookie Generate a random string of printable characters

	Notes: Could be a little more efficent. Add in meaningfull error returns
		Change text errors to standard error codes

	$Author$	$Date$	$Revision$
	$Source$
*/

#define		_REENTRANT
/* #define		_POSIX_PTHREAD_SEMANTICS */

#include <stdio.h>
#include <string.h>
#include <time.h>		/* For get_random_cookie */
#include <sys/times.h>		/* For get_random_cookie */
#include <unistd.h>		/* For get_random_cookie */
#include <pthread.h>		/* For get_random_cookie */
#include <mi.h>
#include <milib.h>
#include "md5.c"		/* For get_random_cookie */

#define		MAXREAD		2000
#define		MAXLEN		200
#define		SYSMSG	"select execweb_executable from execWeb where execweb_id = \'%s\';"
#define		NO_MEMORY(fun)	mi_db_error_raise(NULL, MI_SQL,\
				"UGEN2", "FUNCTION%s", #fun, NULL)
#define		EXCEPTION(msg)	mi_db_error_raise (NULL, MI_EXCEPTION, msg)
#define		CHECK(condition, string)\
				if ( ! (condition)) mi_db_error_raise (NULL,\
				MI_EXCEPTION, string)
/*
   Illustra's documentation alludes to this without explicitly saying it (but it's true):
   any error condition for an SQL command isn't raised until mi_query_finish () 
   processing.
   
   Use the following macro to check for correct execution of any SQL-related mi_XXX
   command.
*/
#define		CHECK_QUERY(condition, string)\
				if ( ! (condition)) {mi_db_error_raise (NULL,\
				MI_EXCEPTION, string); mi_query_finish (conn);}


/* More stuff for get_random_cookie
*/
#define COOKIE_LENGTH 21			/* Longest usable is 21 */
#define COOKIE_MAX 24				/* Don't change this! */

/* Intraprocess only, it's not clear wether this is correct under Informix */
pthread_mutex_t cookie_mutex = PTHREAD_MUTEX_INITIALIZER;

static unsigned char noiz[] = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,
19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,
45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,
71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,
97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,
117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,
137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,
157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,
177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,
197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,
217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,
237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255};

/* This is a list of 64 characters which can be indexed by a 6 bit value */
static char charList[] =
	{"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._"};

/* End of get_random_cookie stuff
*/


/* char *ctime_r(const time_t *clock, char *buf); */


typedef struct BUFLST buflst;
struct BUFLST {
	buflst *next; /* *prev; */
	mi_integer max, len;
	char data[1];
};
static buflst *appbuf(buflst *buf, char *data, unsigned len);
static buflst *newbuf(mi_integer size);
static void freebuf(buflst *cur);
static mi_integer send_sql (MI_CONNECTION *conn, MI_SAVE_SET **ss, char *sql);
static void get_results (MI_CONNECTION *conn, MI_SAVE_SET *ss);

/*
 *	*** Functions ***
*/


/*	Lower		Lower cases an lvarchar
	Called with an lvarchar, returns an lvarchar.
	Could probably skip copying the lvarchar to double the speed.
*/
mi_lvarchar *lower(mi_lvarchar *lv) {
	mi_lvarchar	*new;
	char		*p;
	int		i;

	if ( !(new =  mi_var_copy(lv)) ) NO_MEMORY(lower);
	p = mi_get_vardata(new); i = mi_get_varlen(new);
	while(i--) if (p[i] >= 'A' && p[i] <= 'Z') p[i] |= 0x20;
	return new;
}


/*	Upper		Upper cases an lvarchar
	Called with an lvarchar, returns an lvarchar
	Could probably skip copying the lvarchar to double the speed.
*/
mi_lvarchar *upper(mi_lvarchar *lv) {
	mi_lvarchar	*new;
	char		*p;
	int		i;

	if ( !(new =  mi_var_copy(lv)) ) NO_MEMORY(upper);
	p = mi_get_vardata(new); i = mi_get_varlen(new);
	while(i--) if (p[i] >= 'a' && p[i] <= 'z') p[i] &= ~0x20;
	return new;
}


/*	sysexec		Executes a system program and returns output
	Called with an lvarchar command line, and Returns STDOUT
	from the program as an lvarchar.
	Note: If the program generates an error the whole server exits! (FIXED)
*/
mi_lvarchar *sysexec(mi_lvarchar *cmd, mi_lvarchar *args) {
	FILE		*outf;
	char		cmdbuf[MAXLEN], *cmds, *p;
	int		total = 0;
	buflst		head, *buf= &head;
	MI_CONNECTION	*conn;
	MI_SAVE_SET	*ss;
	MI_ROW		*row;
	MI_DATUM	colval;
	mi_integer	collen, error;
	mi_lvarchar	*lv;

	conn = mi_open(NULL, NULL, NULL);	/* Open connection */
	if (conn == NULL) EXCEPTION("ERROR: conn is NULL\n");
	
if (1) {
	if (sizeof(SYSMSG) + mi_get_varlen(cmd) >= MAXLEN)	/* Check size */
		EXCEPTION("Command ID is too long");
	sprintf (cmdbuf, SYSMSG, mi_lvarchar_to_string(cmd));	/* Build it */
	if (send_sql(conn, &ss, cmdbuf) != 1)			/* Send it */
		EXCEPTION("Invalid sysexec command");
	row = mi_save_set_get_first(ss, &error);		/* Get it */
	if (!row) EXCEPTION("Can't get row from save set!");
	mi_value(row, 0, &colval, &collen);


	/*	NOTE: There are some SECURITY CONCERNS here. For starters we
		should remove all special chars from the args list. Also we might
		want to check the ownership and permissions of the command before
		we execute it.
	*/

	/* Build command and force stderr to stdout, and exit value of zero */
	if (strlen(colval) + mi_get_varlen(args) + 15 >= MAXLEN)
		EXCEPTION("Command and args are too long");
	sprintf(cmdbuf, "%s %s 2>&1; exit 0", colval, mi_lvarchar_to_string(args));
} else {	/* TEST */
	sprintf(cmdbuf, "%s %s 2>&1; exit 0", mi_lvarchar_to_string(cmd), mi_lvarchar_to_string(args));
}
	/* Exec command */
 	if (!(outf = popen(cmdbuf, "r")))
 		EXCEPTION("Can't execute command");

	/* Collect output */
	while(!feof(outf) && !ferror(outf)) {
		if ( !(buf = buf->next = newbuf(MAXREAD)) ) NO_MEMORY(sysexec);
		total += buf->len = fread(buf->data, 1, MAXREAD, outf);
	}
	
	/* Get lvarchar to hold it */
	if (	!(lv = mi_new_var(total)) ||
		!(p = mi_get_vardata(lv)) ) NO_MEMORY(sysexec);
		
	/* Copy data */
	for (buf = head.next; buf; buf = buf->next) {
		memcpy(p, buf->data, buf->len); p += buf->len;
	}
	
	/* Clean up */
	mi_set_varlen(lv, total);
	pclose(outf); mi_close(conn); mi_free(cmds); freebuf(head.next);
	return lv;
}


/*	replace		Replace up to N occurrences of substring old with
	substring new in string src. If N is 0 then all occurrences are replaced.
	Called with three lvarchars and an integer. Returns an lvarchar.
*/
mi_lvarchar *replace(mi_lvarchar *old, mi_lvarchar *new,
		     mi_lvarchar *src, mi_integer n) {
	char		*old_s, *new_s, *src_s, *sptr, *p;
	buflst		*head, *buf;
	unsigned	old_len, new_len;

	/* Copy all varchars to strings, it's too bad we have to do this.*/
	if (	!(old_s = mi_lvarchar_to_string(old)) ||
		!(new_s = mi_lvarchar_to_string(new)) ||
		!(src_s = mi_lvarchar_to_string(src)) ||
		!(head = buf = newbuf(MAXREAD)) ) NO_MEMORY(replace);

	old_len = mi_get_varlen(old); new_len = mi_get_varlen(new);
	n = n ? n : ~0;		/* If zero replace all */

	for(sptr = src_s; *sptr; sptr = p + old_len) {
		if (n && (p = strstr(sptr, old_s))) {
			buf = appbuf(buf, sptr, p - sptr);
			buf = appbuf(buf, new_s, new_len);
			if (n != ~0) --n;
		} else {
			buf = appbuf(buf, sptr,
			  src_s + mi_get_varlen(src) - sptr);
			break;
		}
 	}
 
 	if (!buf) NO_MEMORY(replace);			/* EXIT IF ERROR! */
 
 	/* Get size of result, usually in just one buffer? */
 	for (n = 0, buf = head; buf; buf = buf->next) n += buf->len;

 	/* Get lvarchar to hold it, (we can reuse src at this point) */
	if (	!(src = mi_new_var(n)) ||
		!(p = mi_get_vardata(src)) ) NO_MEMORY(replace);

	/* Copy data */
	for (buf = head; buf; buf = buf->next) {
		memcpy(p, buf->data, buf->len); p += buf->len;
	}
	
	/* Clean up */
	mi_set_varlen(src, n);
	mi_free(old_s); mi_free(new_s); mi_free(src_s); freebuf(head);
	return src;
}


/*	appbuf		Append data to list of buffers allocating more as
	needed. Called with current buffer, pointer to data, length of data.
	Returns pointer to current buffer.
	Note: memcpy needs to behave correctly with a length of zero.
*/
static buflst *appbuf(buflst *buf, char *data, unsigned len) {
	unsigned		cnt;

	while(len) {
		if (!buf) return NULL;			/* EXIT if error! */

		cnt = len <= (cnt = buf->max - buf->len) ? len : cnt;
		memcpy(buf->data + buf->len, data, cnt);
		buf->len += cnt; data += cnt; len -= cnt;
		if (buf->len == buf->max) buf = buf->next = newbuf(MAXREAD);
	}
	return buf;
}


/*	concat		Concatenates two strings (lvarchars)
	The Informix concat function seems to be broken, oh well
	Returns an lvarchar.
*/
mi_lvarchar *conc(mi_lvarchar *pre, mi_lvarchar *post) {
	int		n, m;
	char		*p;
	mi_lvarchar	*lv;

	n = mi_get_varlen(pre); m = mi_get_varlen(post);

	/* Get lvarchar to hold it */
	if (	!(lv = mi_new_var(n + m)) ||
		!(p = mi_get_vardata(lv)) ) NO_MEMORY(concat);

	/* Copy arguments to result */
	memcpy(p, mi_get_vardata(pre), n);
	memcpy(p + n, mi_get_vardata(post), m);

	/* Finish up */
	mi_set_varlen(lv, n + m);
	return lv;
}


/*	get_id		Generates a new unique ID by concatenateing
	the argument to an interger, Called with string, returns string
	NOTE: FAKED for now, will not guarentee uniqueness untill rewriten
*/
mi_lvarchar *get_id(mi_lvarchar *name) {
	static int	id;
	char		buf[25];

	sprintf(buf, "%d", id++);

	return conc(name, mi_string_to_lvarchar(buf));
}


/*	html-breaks		Replaces newlines in text with <P>, This
	prepares it to be displayed in html. Called with an lvarchar.
	Returns an lvarchar.
*/
mi_lvarchar *html_breaks(mi_lvarchar *text) {
	mi_lvarchar	*old, *new, *res;
	
	res = replace(old = mi_string_to_lvarchar("\n"),
		new = mi_string_to_lvarchar("<P>"), text, 0);
	mi_var_free(old); mi_var_free(new);
	return res;
}


/*	now		Returns the current datetime something like the
	Illustra now.
	Returns a datetime;
*/
mi_datetime *now() {
	time_t		seconds;
	char		buf[25];
	
	time(&seconds);			/* What time is it Now? */

	cftime(buf, "%Y-%m-%d %T.000", &seconds);
	return mi_string_to_datetime(buf, "datetime year to fraction(3)");
}


/*	expr	Does nothing except return it's argument. This allows you to
		create a context in which to evaluate expressions in SQL as long
		as the resulting type is refered to by a pointer rather than
		passed as an actual value.
		You can create lots of function signitures with this one routine.
*/
void *expr(void *arg) {
	return arg;
}


/*	get_random_cookie	Generates a random cookie of printable
	characters to be passed to a web browser. Returns an lvarchar.
*/
mi_lvarchar *get_random_cookie () {
    int i, j, row;
    time_t seconds;
    long pid;
    static unsigned count = 0;
    struct tms timeVals;
    unsigned char in_hash[16], out_hash[16];
    struct MD5Context context;
    char cookie [COOKIE_MAX + 1];
    mi_lvarchar *var;
    
    /*	
       Implementation notes: This function attempts to generate the most
       random cookie possible.  We are hindered somewhat because we don't
       want to store the last seed in the database. If we did so, we
       would slow down the process of establishing an initial connection
       and make this function more complex and perhaps less reliable.
       However it does seem resonable to store a noise pool if you will, in
       a static array. This code is based on noiz-0.5 by Henry Strickland.
  
       We try to use as unique a noise source as possible.  Since time(NULL)
       returns seconds, two people who request a cookie at the same clock
       time will get exactly the same cookie.  We get around this by adding
       in a bunch of CPU time statistics, which should make every proces's
       noise unique.

       The above method still won't handle multiple calls from the same
       process in a short time.  To get around that, we add in a count
       of the number of times we've been called.
    */
    time ( &seconds  );
    times( &timeVals );
    pid= getpid();
    ++count;
    MD5Init( &context );
    MD5Update( &context, (unsigned char*) &noiz, 256 );
    MD5Update( &context, (unsigned char*) &seconds, sizeof seconds );
    MD5Update( &context, (unsigned char*) &timeVals, sizeof timeVals );
    MD5Update( &context, (unsigned char*) &pid, sizeof pid );
    MD5Update( &context, (unsigned char*) &count, sizeof count );
    MD5Final(in_hash, &context );

    /* Stir the state of the noize pool
    */
    for (row=0; row<16; row++ ) {
            MD5Init( &context );
            MD5Update( &context, noiz+16*row, 16 );
            MD5Update( &context, in_hash, 16 );
            MD5Update( &context, in_hash, row+1 );
            MD5Final( out_hash, &context );
            pthread_mutex_lock(&cookie_mutex);		/* Lock while writing */
            for ( i=0; i<16; i++ ) {
                    noiz[16*row+i] ^= out_hash[i];
            }
            pthread_mutex_unlock(&cookie_mutex);	/* Unlock */
    }


    /* Convert in_hash to printable characters
    */
    for (i = j = 0; j < COOKIE_LENGTH; i += 3)
    {
	cookie [j++] = charList[0x3f & in_hash[i]];
	cookie [j++] = charList[in_hash[i] >> 6   | 0x3c & in_hash[i+1] << 2];
	cookie [j++] = charList[in_hash[i+1] >> 4 | 0x30 & in_hash[i+2] << 4];
	cookie [j++] = charList[in_hash[i+2] >> 2];
    }
    cookie [COOKIE_LENGTH] = '\0';

    /* Get lvarchar to hold it */
    if (!(var = mi_string_to_lvarchar(cookie))) NO_MEMORY(get_random_cookie);
    return var;
}


/*	newbuf		Get a new linked buffer
	Called with requested size, Returns pointer to buffer, or 0 if not
	available. Sets max to max storage (same as requested), but len may or
	may not be used, up to you.
*/
static buflst *newbuf(mi_integer size) {
	buflst		*new;

	if ( !(new = mi_alloc(size+sizeof(buflst))) ) return NULL;
	new->max = size;
	new->next = NULL;
	new->len = 0;
	return new;
}


/*	freebuf		Free a list of linked buffers
	Called with pointer to head of list, Returns nothing.
*/
static void freebuf(buflst *next) {
	buflst		*cur;

	while(cur = next) {
		next = cur->next;
		mi_free(cur);
	}
}


/*
 *	Send SQL statement, returns number of rows returned
*/
static mi_integer send_sql(MI_CONNECTION *conn, MI_SAVE_SET **ss, char *sql) {
	int		count;

	*ss = mi_save_set_create(conn);		/* Create save set */
	if (!*ss) EXCEPTION("Can't create save set\n");

	if (MI_ERROR == mi_exec(conn, sql, 0))	/* Send SQL statemnet */
		EXCEPTION("Can't send SQL in send_sql\n");

	get_results(conn, *ss);			/* Get results */
	count = mi_save_set_count(*ss);

	if (count == MI_ERROR)
		EXCEPTION("Can't get count of save set in send_sql\n");

	if (MI_ERROR == mi_query_finish(conn))
		EXCEPTION("Can't finish query in send_sql\n");

	return count;
}


/*
 *	Get results of SQL statement and insert into a save set
*/
static void get_results (MI_CONNECTION *conn, MI_SAVE_SET *ss) {
	mi_integer	result, error;
	MI_ROW		*row;
	
	while ((result = mi_get_result(conn)) != MI_NO_MORE_RESULTS) {
		switch(result) {
	case MI_ERROR:
		EXCEPTION("could not get results (result MI_ERROR)");
		break;
	case MI_DDL:
	case MI_DML:
		break;
	case MI_ROWS:
		row = mi_next_row (conn, &error);
		if (row) mi_save_set_insert(ss, row);
		break;
	default:
		EXCEPTION("unknown result from mi_get_result");
		break;
	}}
}

/*
		EXCEPTION("Case MI_DML from mi_get_result");
		count = mi_result_row_count(conn);
		if (count == MI_ERROR) EXCEPTION("Can't get row count.\n");
		if (count > 1) EXCEPTION("More than one row returned.\n");
		sprintf(buf, "count = %d\n", count);
		mi_db_error_raise (NULL, MI_MESSAGE, buf);

		rowdesc = mi_get_row_desc_without_row(conn);
		CHECK_QUERY (rowdesc != NULL, "mi_get_row_desc failed");
		numcols = mi_column_count (rowdesc);
		CHECK_QUERY (numcols != MI_ERROR, "mi_column_count failed");
		sprintf(buf, "numcols = %d\n", numcols);
		EXCEPTION(buf);
		sprintf(buf, "colname = %s\n", mi_column_name(rowdesc, 0));
		EXCEPTION(buf);
		EXCEPTION(mi_column_name(mi_get_row_desc(row), 0));
*/
