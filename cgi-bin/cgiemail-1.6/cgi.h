/**********************************************************************
 *  cgi.h -- header for libcgi
 *  Generated automatically from cgi.h.in by configure.
 *
 * Copyright 1994, 1996 by the Massachusetts Institute of Technology
 * For copying and distribution information, please see the file
 * <mit-copyright.h>.
 **********************************************************************/
#ifndef _CGI_H
#define _CGI_H
#include "mit-copyright.h"

#include <stdio.h>
#include <sys/types.h>
#include "config.h"

typedef struct _cgi_field
{
  char *name;
  char *value;
} cgi_field;

#define CGI_ERRMSG_MAX (1023)	/* maximum length of an error message */
#define CGI_VARNAME_MAX (255)	/* max length of a variable name */

typedef struct _cgi_form
{
  char *source;			/* QUERY_STRING env variable or stdin */
  char *storage;		/* where to put translated strings */
  cgi_field *fields;
  size_t maxstorage, maxfields, nfields;
  int errcond;
  char errmsg[CGI_ERRMSG_MAX+1]; /* one line, including HTTP status code */
  char errinfo[CGI_ERRMSG_MAX+1]; /* additional info for error page */
  FILE *tmpf;			/* temporary file for template output */
} cgi_form;

/* characters in a hex representation are digits plus A-F, a-f */
#define cgi_ishex(c) ((isdigit(c) || ((int)'A' <= (c) && (c) <= (int) 'F') || ((int)'a' <= (c) && (c) <= (int) 'f')) ? 1 : 0)

/* prefix for names of required fields ("required" means "must be nonblank") */
#define CGI_REQ_PREFIX "required"
#define CGI_REQ_PREFIX_LEN (8)
#define cgi_required(name) (!strncmp(name, CGI_REQ_PREFIX, CGI_REQ_PREFIX_LEN))

/* this field specifies html to be appended to success or error messages */
/* e.g. <INPUT TYPE="hidden" NAME="addendum" VALUE="Thank you!"> */
#define CGI_ADDENDUM "addendum"

/* this field specifies a URL to point to on success */
/* e.g. <INPUT TYPE="hidden" NAME="success" VALUE="http://web.mit.edu/"> */
#define CGI_SUCCESS "success"

/* this field specifies a URL to point to on failure */
/* e.g. <INPUT TYPE="hidden" NAME="failure" VALUE="http://web.mit.edu/"> */
#define CGI_FAILURE "failure"

/* Formatting directive for HTML-encoding an input value */
#define CGI_HTMLENCODE "%H"

/* Formatting directive for URL-encoding an input value */
#define CGI_URLENCODE "%U"

/* path to sendmail */
#define PATH_SENDMAIL "/usr/lib/sendmail"

/* release version */
#define CGIEMAIL_RELEASE "1.6"

/* "special" output variables for success/failure templates */
#define CGI_ERRMSG "cgierrmsg"
#define CGI_ERRINFO "cgierrinfo"
#define CGI_RELEASE "cgirelease"
#define CGI_DATE "cgidate"

/* URL for redirect when no PATH_TRANSLATED is available */
#define CGI_NOPATH "http://web.mit.edu/wwwdev/cgiemail/nopath.html"

/* File for cgifile to append to */
#define CGI_INFILE "incoming.txt"

#include "cgi-ptypes.h"
#endif /* _CGI_H */
