#if defined(__STDC__) || defined(__cplusplus)
# define P_(s) s
#else
# define P_(s) ()
#endif


/* cgilib.c */
int cgi_alloc_form P_((cgi_form *formp));
void cgi_free_form P_((cgi_form *formp));
int cgi_parallel_fname P_((char *basename, char *buf, int buflen));
void cgi_fix_crlf P_((char *string));
int cgi_nonblank P_((char *string));
int cgi_parse_form P_((cgi_form *formp));
void cgi_print_form P_((cgi_form *formp, FILE *outstream));
char *cgi_value P_((cgi_form *formp, char *name));
char *cgi_char2entity P_((int c));
void cgi_string2url P_((char *instring, FILE *outstream));
void cgi_string2html P_((char *instring, FILE *outstream));
void cgi_stream2html P_((FILE *instream, FILE *outstream));
int cgi_output_value P_((cgi_form *formp, char *name, char *formatstr, FILE *outstream));
void cgi_concat_errno P_((char *string));
int cgi_template_fill P_((cgi_form *formp, char *templatefile));
void cgi_pclose_fix P_((void));
int cgi_mail_template P_((cgi_form *formp, char *templatefile));
void cgi_output_failure P_((cgi_form *formp, char *msg));
void cgi_redirect P_((char *url));
void cgi_output_success P_((cgi_form *formp, char *msg));
int cgi_standard_email P_((void));
int cgi_standard_echo P_((void));
int cgi_standard_file P_((void));

/* cgilibcso.c */
char *cgi_next_line_in_string P_((char *string, char *linebuf, int linebuflen));
int cgi_cso_field P_((char *linebuf, char *fieldname, char *valuebuf, int buflen));
void cgi_cso_header P_((cgi_form *formp, char *query));
void cgi_cso_footer P_((void));
void cgi_url_print P_((char linebuf[]));
int cgi_standard_cso P_((void));

#undef P_
