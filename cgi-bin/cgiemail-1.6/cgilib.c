/**********************************************************************
 *  cgi.c -- libcgi
 *
 * Copyright 1994, 1996, 1998
 * by the Massachusetts Institute of Technology
 * For copying and distribution information, please see the file
 * <mit-copyright.h>.
 **********************************************************************/
#include "mit-copyright.h"

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>
#include <signal.h>
#include <time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <fcntl.h>
#include "cgi.h"
#ifdef ENABLE_OWNER_BOUNCE
#include <pwd.h>
#endif /* ENABLE_OWNER_BOUNCE */

char *cgi_query=(char*)0;
time_t received_time;
char datebuf[80];

int cgi_alloc_form(formp)
     cgi_form *formp;
{
  char *env_var;

  /* zero out everything */
  memset(formp, 0, sizeof(cgi_form));

  /* get query string from environment */
  if (!cgi_query)
    {
      env_var=getenv("REQUEST_METHOD");
      if (!env_var)
	{
	  formp->errcond=1;
	  strcpy(formp->errmsg, "400 REQUEST_METHOD not set.");
	  return(1);
	}
      if (!strcmp(env_var, "GET"))
	{
	  cgi_query= getenv("QUERY_STRING");
	  if (!cgi_query)
	    {
	      formp->errcond=1;
	      strcpy(formp->errmsg,
		     "400 REQUEST_METHOD is GET, but QUERY_STRING is not set.");
	      return(1);
	    }
	}
      else
	{
	  int content_length, total=0, bytes_read;

	  content_length= atoi(getenv("CONTENT_LENGTH"));
	  cgi_query=malloc(content_length+1);
	  if (!cgi_query)
	    {
	      formp->errcond=1;
	      sprintf(formp->errmsg,
		      "503 Couldn't allocate %d bytes of memory.",
		      content_length+1);
	      return(1);
	    }
	  while (total < content_length)
	    {
	      bytes_read = read(0, cgi_query+total, content_length);

	      /* If the client is gone, just exit */
	      if (bytes_read <= 0)
		{
		  char *progname;

		  progname=getenv("SCRIPT_NAME");
		  if (!progname) progname="cgiemail";
		  if (bytes_read < 0) perror(progname);
		  else fprintf(stderr, "%s: read() returned 0.\n", progname);
		  return(1);
		}
	      total += bytes_read;
	    }

	  cgi_query[content_length]='\0';
	}
    }

  formp->source = cgi_query;
  if (! formp->source)
    {
      formp->errcond=1;
      strcpy(formp->errmsg, "400 Could not read form input.");
      return(1);
    }

  /* decide how much to allocate */
  formp->maxstorage = strlen(formp->source)+1;
  formp->maxfields = formp->maxstorage / 4 + 1;

  /* allocate */
  formp->storage = (char *) malloc(formp->maxstorage);
  if (!formp->storage)
    {
      formp->errcond=1;
      sprintf(formp->errmsg, "503 Couldn't allocate %d bytes of memory.",
	      formp->maxstorage);
      return(1);
    }
  formp->fields = (cgi_field *) malloc(formp->maxfields * sizeof(cgi_field));
  if (!formp->fields)
    {
      free(formp->storage);
      formp->errcond=1;
      sprintf(formp->errmsg, "503 Couldn't allocate %d bytes of memory.",
	      formp->maxfields * sizeof(cgi_field));
      return(1);
    }

  return(0);
}

void
cgi_free_form(formp)
     cgi_form *formp;
{
  if (formp)
    {
      if (formp->storage) free(formp->storage);
      if (formp->fields) free(formp->fields);
    }
  return;
}

/* Get the full pathname of a file parallel to the PATH_TRANSLATED file */
/* Return 0 on success, nonzero on failure. */

int
cgi_parallel_fname(basename, buf, buflen)
     char *basename, *buf;
     int buflen;
{
  int dlen;
  char *env_var, *dir_end;

  /* Get directory */
  env_var = getenv("PATH_TRANSLATED");
  if (!env_var)
    return 1;
  dir_end = strrchr(env_var, '/');
  if (!dir_end)
    return 1;
  dlen = dir_end - env_var + 1;

  if (1 + dlen + strlen(basename) > buflen)
    return 1;

  strncpy(buf, env_var, dlen);
  strcpy(buf+dlen, basename);
  return 0;
}

/* converts cr/lf pairs or cr by itself to lf, which Unix will handle better */
void
cgi_fix_crlf(string)
     char *string;
{
  char *ptr;

  while ((ptr=strchr(string, '\r')) != NULL)
    {
      if (*(ptr+1) == '\n') strcpy(ptr, ptr+1);
      else *ptr = '\n';
    }
  return;
}

/* returns 1 if string is filled-in filed, 0 if left blank */
int
cgi_nonblank(string)
     char *string;
{
  char *ptr;

  for(ptr=string; *ptr; ptr++)
    if (isgraph((int)(*ptr))) return(1);

  return(0);
}

/*
 * parsing has 4 states:
 * 0: beginning of name
 * 1: after beginning of name
 * 2: beginning of value
 * 3: after beginning of value
 */

int
cgi_parse_form(formp)
     cgi_form *formp;
{
  char *from_ptr, *to_ptr;
  cgi_field *field_ptr;
  char hex_string[3];
  int parse_state=0;
  int hex_int, i;
  struct tm *timeptr;

  /* Get date-time (RFC 822 sec. 5, RFC 1123 sec. 5.2.14) */
  /* FIXME: not compliant if lang is not English or TZ unknown */
  time(&received_time);
  timeptr=localtime(&received_time);
  strftime(datebuf, 79, "%a, %d %b %Y %H:%M:%S %Z", timeptr);

  /* initialize variables */
  field_ptr=formp->fields;
  formp->nfields=0;
  hex_string[2]='\0';

  /* iterate through all characters in query string */
  for (from_ptr=formp->source, to_ptr=formp->storage;
       *from_ptr != '\0' && to_ptr - formp->storage < formp->maxstorage;
       from_ptr++)
    {
      if (parse_state==0)
	{
	  /* we're at the 'n' in "name=value&name2=value2..." */
	  field_ptr->name = to_ptr;
	  formp->nfields++;
	  parse_state++;
	}
      if (parse_state==2)
	{
	  /* we're at the 'v' in "name=value&name2=value2..." */
	  field_ptr->value = to_ptr;
	  parse_state++;
	}
      if (from_ptr[0] == '%' && cgi_ishex((int)from_ptr[1]))
	{
	  /* hex representation like "%2B" in "four=2%2B2" */
	  hex_string[0] = from_ptr[1];
	  hex_string[1] = from_ptr[2];
	  sscanf(hex_string, "%x", &hex_int);
	  *to_ptr++ = (char)hex_int;
	  from_ptr += 1 + cgi_ishex((int)from_ptr[2]);
	  continue;
	}
      if (from_ptr[0] == '+')
	{
	  *to_ptr++ = ' ';
	  continue;
	}
      if (parse_state==1 && from_ptr[0] == '=')
	{
	  /* we're at the '=' in "name=value&name2=value2..." */
	  *to_ptr++ = '\0';
	  parse_state++;
	  continue;
	}	  
      if (from_ptr[0] == '&')
	{
	  *to_ptr++ = '\0';
	  field_ptr++;
	  if (field_ptr > formp->fields + formp->maxfields)
	    {
	      formp->errcond=1;
	      sprintf(formp->errmsg, "400 Exceeded %d fields.",
		      formp->maxfields);
	      return(1);
	    }
	  parse_state=0;
	  continue;
	}
      /* default */
      *to_ptr++ = *from_ptr;
    }

  /* success */
  *to_ptr='\0';
  if (parse_state==2) field_ptr->value = to_ptr;

  /* fix CR/LF */
  for (i=0; i<formp->nfields; i++)
    cgi_fix_crlf(formp->fields[i].value);

  /* Add "special" output variables */
  if (formp->maxfields - formp->nfields > 4)
    {
      formp->fields[formp->nfields].name = CGI_ERRMSG;
      formp->fields[formp->nfields++].value = formp->errmsg;
      formp->fields[formp->nfields].name = CGI_ERRINFO;
      formp->fields[formp->nfields++].value = formp->errinfo;
      formp->fields[formp->nfields].name = CGI_RELEASE;
      formp->fields[formp->nfields++].value = CGIEMAIL_RELEASE;
      formp->fields[formp->nfields].name = CGI_DATE;
      formp->fields[formp->nfields++].value = datebuf;
    }

  return(0);
}

/* only for debugging purposes */
void
cgi_print_form(formp, outstream)
     cgi_form *formp;
     FILE *outstream;
{
  int i;

  fprintf(outstream,
	  "source=\t%s\nmaxstorage=\t%d\nmaxfields=\t%d\nnfields=\t%d\n",
	  formp->source, formp->maxstorage, formp->maxfields, formp->nfields);
  for (i=0; i<formp->nfields; i++)
    printf("Field %s=\t%s\n", formp->fields[i].name, formp->fields[i].value);
  return;
}

char *
cgi_value(formp, name)
     cgi_form *formp;
     char *name;
{
  int i;

  for (i=0; i<formp->nfields; i++)
    {
      if (!strcmp(formp->fields[i].name, name))
	return(formp->fields[i].value);
    }
  return("");
}

/* Function to translate an integer character into an HTML entity string */
char *
cgi_char2entity(c)
     int c;
{
  static char retval[2];

  switch (c)
    {
    case (int)'&':
      return("&amp;");
    case (int)'<':
      return("&lt;");
    case (int)'>':
      return("&gt;");
    case (int)'"':
      return("&quot;");
    default:
      sprintf(retval, "%c", c);
      return(retval);
    }
}

/*
 * Non-alphanumeric chars that don't need %nn encoding in form values.
 * See also: http://www.w3.org/Addressing/URL/5_BNF.html
 */
static char url_safe[]="/:$-_@.";

void
cgi_string2url(instring, outstream)
     char *instring;
     FILE *outstream;
{
  while (*instring)
    {
      if (*instring == ' ')
	fputc('+', outstream);
      else
	{
	  if (isalnum(*instring) || strchr(url_safe, *instring))
	    fputc(*instring, outstream);
	  else
	    fprintf(outstream, "%%%02x", (int)*instring);
	}
      instring++;
    }
  return;
}

void
cgi_string2html(instring, outstream)
     char *instring;
     FILE *outstream;
{
  while (*instring)
    fputs(cgi_char2entity(*instring++), outstream);
  return;
}
  
void
cgi_stream2html(instream, outstream)
     FILE *instream, *outstream;
{
  int c;

  while ((c=fgetc(instream)) != EOF)
    fputs(cgi_char2entity(c), outstream);
  return;
}

/* Output the value(s) of a CGI field name; return number found */
int
cgi_output_value(formp, name, formatstr, outstream)
     cgi_form *formp;
     char *name;
     char *formatstr;
     FILE *outstream;
{
  int i, nfound=0;

  for (i=0; i < formp->nfields; i++)
    {
      if (!strcmp(formp->fields[i].name, name))
	{
	  if (nfound) fputc(' ', outstream);
	  if (formatstr)
	    {
	      if (!strcmp(formatstr, CGI_HTMLENCODE))
		cgi_string2html(formp->fields[i].value, outstream);
	      else
		{
		  if (!strcmp(formatstr, CGI_URLENCODE))
		    cgi_string2url(formp->fields[i].value, outstream);
		  else
		    fprintf(outstream, formatstr, formp->fields[i].value);
		}
	    }
	  else
	    fputs(formp->fields[i].value, outstream);
	  if (cgi_nonblank(formp->fields[i].value)) nfound++;
	}	  
    }
  return(nfound);
}

void
cgi_concat_errno(string)
     char *string;
{
#if HAVE_STRERROR
  strcat(strcat(string, " - "), strerror(errno));
#else
  char errstring[16];

  sprintf(errstring, " (errno: %d)", errno);
  strcat(string, errstring);
#endif
  return;
}

int
cgi_template_fill(formp, templatefile)
     cgi_form *formp;
     char *templatefile;
{
  FILE *tfp;
  char varname[CGI_VARNAME_MAX];
  char formatstr[CGI_VARNAME_MAX];
  int varnamelen=0, formatlen=0, nfound=0, substitutions=0;
  int inchar, parse_state=0;

#if ENABLE_CGIENV
  int cgienv=0;
  char *envval;
#endif /* ENABLE_CGIENV */

  /* open template file */
  tfp = fopen(templatefile, "r");
  if (!tfp)
    {
      formp->errcond=1;
      strcpy(formp->errmsg, "500 Could not open template");
      cgi_concat_errno(formp->errmsg);
      strncpy(formp->errinfo, templatefile, CGI_ERRMSG_MAX);
      return(1);
    }

  /* open temporary file for filled-in template */
  if (formp->tmpf) rewind(formp->tmpf);
  else formp->tmpf = tmpfile();
  if (!formp->tmpf)
    {
      formp->errcond=1;
      strcpy(formp->errmsg, "500 Could not open temporary file.");
#ifdef P_tmpdir
      /* Try to make the error message more informative */
      if (access(P_tmpdir, W_OK))
	{
	  strcpy(formp->errinfo, P_tmpdir);
	  cgi_concat_errno(formp->errinfo);
	}
#endif
      return(1);
    }

  while ((inchar=fgetc(tfp)) != EOF
	 && isspace(inchar))
    { /* Skip leading whitespace */ }

  if (inchar == EOF)
    {
      formp->errcond=1;
      strcpy(formp->errmsg, "500 Empty template file");
      strncpy(formp->errinfo, templatefile, CGI_ERRMSG_MAX);
      return(1);
    }

  /*
   * parsing states:
   * 0: echo       sending literally except for CRLF conversion or \, [
   * 1: quote      sending literally no matter what
   * 2: variable   reading variable name
   * 3: format     reading format
   */

  do  
    {
      if (parse_state==1)	/* quote */
	{
	  fputc(inchar, formp->tmpf);
	  parse_state=0;
	  continue;
	}
      if (parse_state==0)	/* echo */
	{
	  /* Convert CR or CRLF to LF */
	  if (inchar == (int)'\r')
	    {
	      fputc('\n', formp->tmpf);
	      inchar=fgetc(tfp);
	      if (inchar == (int)'\n') continue;
	    }
	  if (inchar == (int)'\\')
	    {
	      parse_state=1;
	      continue;
	    }
	  if (inchar == (int)'[')
	    {
#if ENABLE_CGIENV
	      cgienv=0;
#endif
	      parse_state=2;
	      varnamelen=0;
	      formatlen=0;
	      continue;
	    }
	  fputc(inchar, formp->tmpf);
	  continue;
	}
      if (parse_state==2)	/* variable name */
	{
	  if (inchar == (int)'%')
	    {
	       formatlen=1;
	       strcpy(formatstr, "%");
	       parse_state=3;
	       continue;
	    }

#if ENABLE_CGIENV
	  if (inchar == (int)'$' && varnamelen==0)
	    {
	      cgienv=1;
	      continue;
	    }
#endif /* ENABLE_CGIENV */

	  if (inchar == (int)']')
	    {
	      varname[varnamelen]='\0';
#if ENABLE_CGIENV
	      if (cgienv)
		{
		  cgienv=0;
		  envval=getenv(varname);
		  if (!envval) envval="";
		  else substitutions++;
		  if (formatlen > 0)
		    fprintf(formp->tmpf, formatstr, envval);
		  else fputs(envval, formp->tmpf);
		}
	      else
#endif /* ENABLE_CGIENV */
		{
		  if (formatlen > 0)
		    nfound = cgi_output_value(formp,
					      varname,
					      formatstr,
					      formp->tmpf);
		  else nfound = cgi_output_value(formp,
						 varname,
						 NULL,
						 formp->tmpf);
	          if (!nfound && cgi_required(varname))
		    {
		      formp->errcond=1;
		      strcpy(formp->errmsg,
			      "400 Required field left blank");
		      strcpy(formp->errinfo, varname);
		      return(1);
		    }
		  substitutions += nfound;
		}
	      parse_state=0;
	      continue;
	    }
	  varname[varnamelen++]=(char) inchar;
	  if (varnamelen > CGI_VARNAME_MAX)
	    {
	      fclose(tfp);
	      formp->errcond=1;
	      strcpy(formp->errmsg, "403 Variable Name too long");
	      strncpy(formp->errinfo, varname, varnamelen);
	      strcpy(formp->errinfo + varnamelen, "...");
	      return(1);
	    }
	  continue;
	}
      if (parse_state==3)	/* format */
        {
	  if (inchar == (int)',')
	    {
	      formatstr[formatlen]='\0';
	      parse_state=2;
	      continue;
	    }
	  formatstr[formatlen++]=(char) inchar;
	  if (formatlen > CGI_VARNAME_MAX)
	    {
	      fclose(tfp);
	      formp->errcond=1;
	      strcpy(formp->errmsg, "403 Format String too long");
	      strncpy(formp->errinfo, formatstr, formatlen);
	      strcpy(formp->errinfo + formatlen, "...");
	      return(1);
	    }
	  continue;
	}
    } while ((inchar=fgetc(tfp)) != EOF);

  fclose(tfp);

  /* If there were no variable substitutions in this template,
     someone may be trying to use cgiemail to get at restricted pages */
  if (!substitutions)
    {
      formp->errcond=1;
      strcpy(formp->errmsg, "403 No variable substitutions in template");
      strncpy(formp->errinfo, templatefile, CGI_ERRMSG_MAX);
      return(1);
    }

  return 0;
}

/*
 * Make sure pclose() can get the exit status of the child process.
 * Some HTTP servers block SIGCHLD, causing pclose() to always return -1.
 * 
 * Thank you Guido van Rossum (guido@CNRI.Reston.VA.US)
 http://www-db.stanford.edu/~hassan/hymail/pythonlist/python_1995_q4/1317.html
 */

void
cgi_pclose_fix()
{
#ifdef HAVE_SIGPROCMASK
  /* POSIX signal mask */
  sigset_t mysigmask;

  sigemptyset(&mysigmask);
  sigaddset(&mysigmask, SIGCHLD);
  sigprocmask(SIG_UNBLOCK, &mysigmask, 0);

  /* Under Irix 5.3 we can be killed if sendmail died prematurely -brlewis */
  sigemptyset(&mysigmask);
  sigaddset(&mysigmask, SIGPIPE);
  sigprocmask(SIG_BLOCK, &mysigmask, 0);
#else
  /* Hopefully this works for old versions of SunOS.  We'll see. */
  signal(SIGCHLD, SIG_DFL);
#endif
}

int
cgi_mail_template(formp, templatefile)
     cgi_form *formp;
     char *templatefile;
{
  FILE *mypipe, *errorfp;
  int retval;
  char buf[BUFSIZ], command[BUFSIZ];
  int nbytes;
  char *envvar, errorfile[L_tmpnam];
  int old_stdout, old_stderr, errorfd;
#ifdef ENABLE_OWNER_BOUNCE
  char *owner = NULL;
  struct passwd *pw;
  struct stat st;

  /* Get owner of template */
  if (0 == stat(templatefile, &st))
    {
      pw = getpwuid(st.st_uid);
      if (pw)
	owner = pw->pw_name;
    }
#endif /* ENABLE_OWNER_BOUNCE */

  /* fill in template */
  retval=cgi_template_fill(formp, templatefile);
  if (retval) return(retval);

  /* Get a temporary file for error messages */
  tmpnam(errorfile);
  errorfd=open(errorfile, O_WRONLY|O_CREAT|O_EXCL, 0644);
  if (errorfd != -1)
    {
      old_stdout=dup(1);
      dup2(errorfd, 1);
      old_stderr=dup(2);
      dup2(errorfd, 2);
    }      

  /* open pipe to sendmail */
  cgi_pclose_fix();
  strcat(strcpy(command, PATH_SENDMAIL), " -oi -t");
  if (strstr(cgi_value(formp, "cgiemail-mailopt"), "sync"))
    strcat(command, " -odi");	/* synchronous delivery */
#ifdef ENABLE_OWNER_BOUNCE
  if (owner)
    strcat(strcat(command, " -f "), owner); /* envelope set to owner */
#endif /* ENABLE_OWNER_BOUNCE */
  mypipe = popen(command, "w");

  /* Restore stdout/stderr */
  if (errorfd != -1)
    {
      dup2(old_stdout, 1);
      close(old_stdout);
      dup2(old_stderr, 2);
      close(old_stderr);
      close(errorfd);
    }

  /* Check for failed popen */
  if (!mypipe)
    {
      formp->errcond=1;
      strcpy(formp->errmsg, "500 Could not open sendmail pipe");
      cgi_concat_errno(formp->errmsg);
      strncpy(formp->errinfo, command, CGI_ERRMSG_MAX);
      return(1);
    }

  /* put "Received: from HOST with HTTP;\n\tdate\n" */
  /* See RFC 822 appendix D. */

  envvar = getenv("REMOTE_HOST");
  if (!envvar || !(*envvar)) envvar = getenv("REMOTE_ADDR");
  if (envvar && *envvar)
    {
      fputs("Received: from ", mypipe);
      fputs(envvar, mypipe);
      envvar=getenv("SERVER_NAME");
      if (envvar && *envvar)
	{
	  fputs(" by ", mypipe);
	  fputs(envvar, mypipe);
	}
      fputs(" with HTTP;\n\t", mypipe);
      fputs(datebuf, mypipe);
      fputs("\n", mypipe);
    }

#ifdef ENABLE_XHEADERS
  /* Add the X-Mailer: header */
  fputs("X-Mailer: cgiemail ", mypipe);
  fputs(CGIEMAIL_RELEASE, mypipe);
  if (NULL != (envvar = getenv("HTTP_REFERER")))
    {
      fputs("\n\t(form=\"", mypipe);
      fputs(getenv("HTTP_REFERER"), mypipe);
      fputs("\")", mypipe);
    }
  /* SCRIPT_NAME is always set; skip envvar check */
  fputs("\n\t(action=\"", mypipe);
  fputs(getenv("SCRIPT_NAME"), mypipe);
  if (NULL != (envvar = getenv("PATH_INFO")))
    fputs(getenv("PATH_INFO"), mypipe);
  fputs("\")\n", mypipe);
#endif

  /* send filled-in template */
  rewind(formp->tmpf);
  do {
    if ((nbytes = fread(buf, sizeof(char), BUFSIZ, formp->tmpf)) > 0)
      fwrite(buf, sizeof(char), nbytes, mypipe);
  } while (nbytes == BUFSIZ);

  /* close */
  retval = pclose(mypipe);

  /*
   * Get exit status.  Note that on SunOS 4, WEXITSTATUS(stat)
   * only works if &stat is legal, so WEXITSTATUS(pclose(mypipe))
   * won't compile.
   */
  retval = WEXITSTATUS(retval);

  if (retval)
    {
      /*
       * The return value of pclose is not always meaningful.
       * However, sendmail usually does not write to stdout/stderr on success.
       */

      /* If we can't open the errorfile, something went wrong. */
      errorfp = fopen(errorfile, "r");
      if (!errorfp)
	{
	  formp->errcond=1;
	  sprintf(formp->errmsg,
		  "500 sendmail exit %d - check httpd error logs", retval);
	  strcpy(formp->errinfo, command);
	}
      else
	{
	  /* See if there was any error message */
	  fread(formp->errinfo, sizeof(char), CGI_ERRMSG_MAX, errorfp);
	  if (cgi_nonblank(formp->errinfo))
	    {
	      formp->errcond=1;
	      sprintf(formp->errmsg, "500 sendmail exit %d with error message",
		      retval);
	    }
	}
    }

  unlink(errorfile);
  return(formp->errcond);
}

void
cgi_output_failure(formp, msg)
     cgi_form *formp;
     char *msg;
{
  char *failure;

  /* maybe use customized failure message */
  failure=cgi_value(formp, CGI_FAILURE);
  if (failure && *failure)
    {
      if (strpbrk(failure, ":/"))
	{			/* Probably a URL.  Redirect. */
	  cgi_redirect(failure);
	  return;
	}
      else
	{			/* Probably a template file.  Use it. */
	  char buf[BUFSIZ];
	  int nbytes;

	  if (0 == cgi_parallel_fname(failure, buf, BUFSIZ)
	      && 0 == cgi_template_fill(formp, buf))
	    {
	      rewind(formp->tmpf);
	      puts("Content-Type: text/html\r\n\r");
	      do
		{
		  nbytes = fread(buf, sizeof(char), BUFSIZ, formp->tmpf);
		  if (nbytes)
		    {
		      if (!fwrite(buf, sizeof(char), nbytes, stdout))
			break;
		    }
		}
	      while (nbytes == BUFSIZ);
	      return;
	    }
	}
    }

  /* use generic failure message */
  puts("Content-Type: text/html\r");
  printf("Status: %s\r\n\r\n", formp->errmsg);
  puts("<HEAD><TITLE>Error</TITLE></HEAD>");
  printf("<BODY><H1>Error</H1>%s<P>", msg);
  puts("<BLOCKQUOTE><STRONG><SAMP>");
  puts(formp->errmsg);
  puts("</SAMP></STRONG><P>");
  if (*(formp->errinfo))
    {
      puts("<PRE>");
      puts(formp->errinfo);
      puts("</PRE>");
    }
  puts("</BLOCKQUOTE>");
  (void) cgi_output_value(formp, CGI_ADDENDUM, NULL, stdout);
  puts("<P><EM>cgiemail ");
  puts(CGIEMAIL_RELEASE);
  puts("</EM></BODY>");
  return;
}

/*
 * See RFC 2068, section 10.3 for information on how HTTP redirection
 * should work.  As of 1997, an explicit Status: 303 message doesn't
 * do the job.  For now, just hope httpd does the right thing with
 * Location (see <URL:http://hoohoo.ncsa.uiuc.edu/cgi/out.html>).
 */

void
cgi_redirect(url)
     char *url;
{
  printf("Location: %s\r\n\r\n", url);
  return;
}

void
cgi_output_success(formp, msg)
     cgi_form *formp;
     char *msg;
{
  char *success;

  /* maybe use customized success message */
  success=cgi_value(formp, CGI_SUCCESS);
  if (success && *success)
    {
      if (strpbrk(success, ":/"))
	{			/* Probably a URL.  Redirect. */
	  cgi_redirect(success);
	  return;
	}
      else
	{			/* Probably a template file.  Use it. */
	  char buf[BUFSIZ];
	  int nbytes;

	  if (0 == cgi_parallel_fname(success, buf, BUFSIZ)
	      && 0 == cgi_template_fill(formp, buf))
	    {
	      rewind(formp->tmpf);
	      puts("Content-Type: text/html\r\n\r");
	      do
		{
		  nbytes = fread(buf, sizeof(char), BUFSIZ, formp->tmpf);
		  if (nbytes)
		    {
		      if (!fwrite(buf, sizeof(char), nbytes, stdout))
			break;
		    }
		}
	      while (nbytes == BUFSIZ);
	      return;
	    }
	  else
	    {
	      return cgi_output_failure(formp, msg);
	    }
	}
    }

  /* use generic success message */
  puts("Content-Type: text/html\r\n\r");
  puts("<HEAD><TITLE>Success</TITLE></HEAD>");
  printf("<BODY>%s<P><HR>", msg);
  puts("<PRE>");
  rewind(formp->tmpf);
  cgi_stream2html(formp->tmpf, stdout);
  if (*cgi_value(formp, "debug-source"))
    {
      puts("<HR>");
      puts(formp->source);
      puts("<HR>");
    }
  puts("</PRE><P>");
  (void) cgi_output_value(formp, CGI_ADDENDUM, NULL, stdout);
  puts("<P><EM>cgiemail ");
  puts(CGIEMAIL_RELEASE);
  puts("</EM></BODY>");

  return;
}

int
cgi_standard_email()
{
  cgi_form form;
  char *template_filename;

  template_filename = getenv("PATH_TRANSLATED");
  if (!template_filename || !(*template_filename))
    {
      cgi_redirect(CGI_NOPATH);
      return(1);
    }

  if (cgi_alloc_form(&form) ||
      cgi_parse_form(&form) ||
      cgi_mail_template(&form, template_filename))
    {
      cgi_output_failure(&form, "No email was sent due to an error.");
      cgi_free_form(&form);
      return(1);
    }

  cgi_output_success(&form, "The following email message was sent.");
  cgi_free_form(&form);
  return(0);
}

int
cgi_standard_echo()
{
  cgi_form form;
  char *template_filename;

  template_filename = getenv("PATH_TRANSLATED");
  if (!template_filename || !(*template_filename))
    {
      cgi_redirect(CGI_NOPATH);
      return(1);
    }

  if (cgi_alloc_form(&form) ||
      cgi_parse_form(&form) ||
      cgi_template_fill(&form, template_filename))
    {
      cgi_output_failure(&form, "Form was not processed due to an error.");
      cgi_free_form(&form);
      return(1);
    }

  cgi_output_success(&form, "Processed form looks like this:");
  cgi_free_form(&form);
  return(0);
}

int
cgi_standard_file()
{
  cgi_form form;
  char *template_filename, incoming_filename[BUFSIZ];
  int retval;

  template_filename = getenv("PATH_TRANSLATED");
  if (!template_filename || !(*template_filename))
    {
      cgi_redirect(CGI_NOPATH);
      return(1);
    }

  if (cgi_alloc_form(&form) ||
      cgi_parse_form(&form) ||
      cgi_template_fill(&form, template_filename))
    {
      cgi_output_failure(&form, "Form was not processed due to an error.");
      cgi_free_form(&form);
      return(1);
    }

  if (0 == cgi_parallel_fname(CGI_INFILE, incoming_filename, BUFSIZ))
    {
      if (!access(incoming_filename, W_OK))
	{
	  FILE *fp;
	  char buf[BUFSIZ];
	  int nbytes;
	  struct stat st;

#ifndef ENABLE_FILELINK
	  if (lstat(incoming_filename, &st))
	    {
	      form.errcond=1;
	      strcpy(form.errmsg, "500 Could not stat file");
	      cgi_concat_errno(form.errmsg);
	      strncpy(form.errinfo, incoming_filename, CGI_ERRMSG_MAX);
	      goto failure;
	    }
	  else
	    {
	      if (S_ISLNK(st.st_mode))
		{
		  form.errcond=1;
		  strcpy(form.errmsg, "500 File is symbolic link");
		  strncpy(form.errinfo, incoming_filename, CGI_ERRMSG_MAX);
		  goto failure;
		}
	      else
		if (st.st_nlink > 1)
		{
		  form.errcond=1;
		  strcpy(form.errmsg, "500 File is hard link");
		  strncpy(form.errinfo, incoming_filename, CGI_ERRMSG_MAX);
		  goto failure;
		}
	    }
#endif
	  rewind(form.tmpf);
	  fp = fopen(incoming_filename, "a");
	  if (fp)
	    {
	      do
		{
		  if ((nbytes = fread(buf, sizeof(char),
				      BUFSIZ, form.tmpf)) > 0)
		    fwrite(buf, sizeof(char), nbytes, fp);
		} while (nbytes == BUFSIZ);
	      if (fclose(fp))
		{
		  form.errcond=1;
		  strcpy(form.errmsg, "500 Write to file failed");
		  cgi_concat_errno(form.errmsg);
		  strncpy(form.errinfo, incoming_filename, CGI_ERRMSG_MAX);
		}
	    }
	  else
	    {
	      form.errcond=1;
	      strcpy(form.errmsg, "500 Could not append file");
	      cgi_concat_errno(form.errmsg);
	      strncpy(form.errinfo, incoming_filename, CGI_ERRMSG_MAX);
	    }
	}
      else
	{
	  form.errcond=1;
	  strcpy(form.errmsg, "500 No write access to file");
	  cgi_concat_errno(form.errmsg);
	  strncpy(form.errinfo, incoming_filename, CGI_ERRMSG_MAX);
	}
    }

#ifndef ENABLE_FILELINK
failure:
#endif
  if (form.errcond)
    cgi_output_failure(&form, "Form was not processed due to an error.");
  else
    cgi_output_success(&form, "The following information was written:");
  retval = form.errcond;
  cgi_free_form(&form);
  return retval;
}
