/**********************************************************************
 *  cgilibcso.c -- libcgi portion for finger/CSO stuff
 *
 * Copyright 1994 by the Massachusetts Institute of Technology
 * For copying and distribution information, please see the file
 * <mit-copyright.h>.
 **********************************************************************/
#include "mit-copyright.h"

#define CGI_FINGER_MAX 32768

#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <ctype.h>
#include "cgi.h"
#include "cgicso.h"

char *
cgi_next_line_in_string(string, linebuf, linebuflen)
     char *string, *linebuf;
     int linebuflen;
{
  char *ptr;

  ptr=strchr(string, '\n');

  if (ptr)
    {
      int linelen;

      linelen = ptr - string;
      if (linelen >= linebuflen) linelen = linebuflen-1;
      strncpy(linebuf, string, linelen);
      linebuf[linelen]='\0';
      return(ptr+1);
    }
  strncpy(linebuf, string, linebuflen);
  return((char *)0);
}

/* returns 1 if field found, 0 otherwise */
/* fills in valuebuf with field value on match */
int
cgi_cso_field(linebuf, fieldname, valuebuf, buflen)
     char *linebuf, *fieldname, *valuebuf;
     int buflen;
{
  char *ptr;

  ptr=strchr(linebuf, ':');
  if (!ptr) return(0);		/* not of form name: value */

  /* check if field name matches */
  if (strncmp(fieldname, ptr-strlen(fieldname), strlen(fieldname)))
    return(0);			/* doesn't match */

  /* match! */
  strncpy(valuebuf, ptr+2, buflen);
  return(1);
}

void
cgi_cso_header(formp, query)
     cgi_form *formp;
     char *query;
{
#ifdef CSOHEADER
  char buf[BUFSIZ];
  int nbytes;
#endif

  /* print top of HTML doc */
  puts("Content-Type: text/html\n");

#ifdef CSOHEADER
  /* First try filling a template file; must contain [query] */
  if (!cgi_template_fill(formp, CSOHEADER))
    {
      rewind(formp->tmpf);
      do {
	if ((nbytes = fread(buf, sizeof(char), BUFSIZ, formp->tmpf)) > 0)
	  fwrite(buf, sizeof(char), nbytes, stdout);
      } while (nbytes == BUFSIZ);
      return;
    }
#endif
  fputs("<HEAD><TITLE>Results of directory query: \"", stdout);
  fputs(query, stdout);
  puts("\"</TITLE></HEAD>");
  fputs("<BODY><H1>Results of directory query: \"", stdout);
  fputs(query, stdout);
  puts("\"</H1>");
  return;
  /* be sure to close </BODY> */
}

void
cgi_cso_footer()
{
#ifdef CSOFOOTER
  FILE *fp;
  char buf[BUFSIZ];
  int nbytes;

  /* Try to open CSOFOOTER and dump contents to stdout. */
  if (0 != (fp=fopen(CSOFOOTER, "r")))
    {
      do {
	if ((nbytes = fread(buf, sizeof(char), BUFSIZ, fp)) > 0)
	  fwrite(buf, sizeof(char), nbytes, stdout);
      } while (nbytes == BUFSIZ);
      fclose(fp);
      return;
    }
#endif
  puts("</BODY>");
  return;
}

void
cgi_url_print(linebuf)
     char linebuf[];
{
  /* Look for URL field or any field like http://... */
  if (!strncmp(linebuf, "       url:", 11) || strstr(linebuf, "://"))
    {
      char *ptr1;

      fwrite(linebuf, 1, 12, stdout);

      /* make the URL into a hyperlink */
      fputs("<a href=\"", stdout);
      for(ptr1=linebuf+12; *ptr1 && !isspace(*ptr1); ptr1++)
	putchar((int) *ptr1);
      fputs("\">", stdout);
      fwrite(linebuf+12, 1, ptr1-(linebuf+12), stdout);
      fputs("</a>", stdout);

      /* print the rest of the line */
      puts(ptr1);
    }
  else puts(linebuf);
  return;
}

int
cgi_standard_cso()
{
  cgi_form form;
  char *query, fingerbuf[CGI_FINGER_MAX], *ptr;
  char linebuf[256], namebuf[256], deptbuf[256],
  aliasbuf[256], titlebuf[256], yearbuf[256], emailbuf[256];
  int i, fieldlen, motdlen=0;
  char *fingerhost;

  if (cgi_alloc_form(&form) ||
      cgi_parse_form(&form))
    {
      cgi_output_failure(&form, "Request was not processed due to an error.");
      cgi_free_form(&form);
      return(1);
    }
  query=cgi_value(&form, "query");

#ifdef CGI_CSO_HARDCODE
  fingerhost=CGI_CSO_FINGERHOST;
#else
  /* make sure form has required values */
  fingerhost=cgi_value(&form, "fingerhost");
  if (!*fingerhost)
    {
      form.errcond=1;
      strcpy(form.errmsg, "400 Required field missing: fingerhost");
      cgi_output_failure(&form, "Request was not processed due to an error.");
      cgi_free_form(&form);
      return(1);
    }
#endif

  /* get info from finger */
  if (finger(query, fingerhost, fingerbuf, CGI_FINGER_MAX) < 0)
    {
      form.errcond=1;
      sprintf(form.errmsg, "500 Could not finger %s@%s", query, fingerhost);
      cgi_concat_errno(form.errmsg);
      cgi_output_failure(&form, "Request was not processed due to an error.");
      cgi_free_form(&form);
      return(1);
    }

  cgi_fix_crlf(fingerbuf);

  /* go through query looking for "There were NN matches" */
  ptr=fingerbuf; 
  while ((ptr=cgi_next_line_in_string(ptr, linebuf, 256)) != (char *)0)
    {
      if (!strncmp(linebuf, "There were ", 11)) break;
      motdlen = ptr - fingerbuf;
    }

  /* not found -- print results with special handling of certain fields */
  if (!ptr)
    {
      cgi_cso_header(&form, query);
      linebuf[0]='\0';
      ptr=strstr(fingerbuf, "There was 1 match to your request");
      if (!ptr) ptr = fingerbuf;
      motdlen = ptr - fingerbuf;
      if (motdlen) ptr=cgi_next_line_in_string(ptr, linebuf, 256);
      printf("<p>%s</p>\n<PRE>", linebuf);

      while ((ptr=cgi_next_line_in_string(ptr, linebuf, 256)) != (char *)0)
	{
	  /* Skip alias field */
	  if (cgi_cso_field(linebuf, "alias", aliasbuf, 256))
	      continue;

	  /* make mailto: for known email address */
	  if (cgi_cso_field(linebuf, "email", emailbuf, 256)
	      && strcmp(emailbuf, "Unknown"))
	    {
	      printf("%10s: <A HREF=\"mailto:%s\">%s</A>\n", "email",
		     emailbuf, emailbuf);
	      continue;
	    }

	  /* make hyperlink if appropriate; otherwise print the line */
	  cgi_url_print(linebuf);
	}
    }
  else
    {
      /* found -- print nice HTML list of matches */
      deptbuf[0] = '\0';		/* no dept. found yet */
      cgi_cso_header(&form, query);
      printf("<p>%s</p>\n<PRE>", linebuf);
      while ((ptr=cgi_next_line_in_string(ptr, linebuf, 256)) != (char *)0)
	{
	  if (cgi_cso_field(linebuf, "department", deptbuf, 256)) continue;
	  if (cgi_cso_field(linebuf, "name", namebuf, 256)) continue;
	  if (cgi_cso_field(linebuf, "title", titlebuf, 256)) continue;
	  if (cgi_cso_field(linebuf, "year", yearbuf, 256)) continue;
	  if (cgi_cso_field(linebuf, "alias", aliasbuf, 256))
	    {
	      /* put hyperlink to more specific query */
	      fputs("<A HREF=\"", stdout);
	      fputs(getenv("SCRIPT_NAME"), stdout);
	      fputs("?query=alias%3D", stdout);
	      fputs(aliasbuf, stdout);
	      fputs("\">", stdout);
	      fputs(namebuf, stdout);

	      /* put periods up to next field */
	      fputs("</A>...", stdout);
	      fieldlen = 24-strlen(namebuf);
	      for(i=0; i<fieldlen; i++) putchar((int) '.');

	      /* put "dept, year n", "dept", or "Year n" as appropriate */
	      /* fall back on "title" if available */
	      if (*deptbuf)
		{
		  if (*yearbuf)
		    {
		      fputs(deptbuf, stdout);
		      fputs(", year ", stdout);
		      puts(yearbuf);
		    }
		  else puts(deptbuf);
		}
	      else
		if (*yearbuf)
		  {
		    fputs("Year ", stdout);
		    puts(yearbuf);
		  }
		else
		  if (*titlebuf) puts(titlebuf);
		  else puts("");
	      deptbuf[0] = titlebuf[0] = yearbuf[0] = '\0';
	    }
	}
    }

  puts("</PRE>");
  fwrite(fingerbuf, sizeof(char), motdlen, stdout);
  cgi_cso_footer();
  return(0);
}
