#!/private/bin/perl -T

# removed -w flag after debugging because it started causing many
# uninitialized variable errors that were not worth fixing (all $question... vars)

use CGI;
use MIME::Lite;

my $cgi = new CGI;
my $contact = 'Judy Sprague at <a href="mailto:judys@cs.uoregon.edu">judys@cs.uoregon.edu</a>';
my $getHelp = "For assistance, please contact $contact. ";
my $domain = '<!--|DOMAIN_NAME|-->';
my $debug_mode = 0; # displays environment variables (0=off, 1=on)
$ENV{PATH} = '/usr/sbin:/usr/bin'; # untainted

### START MAIN

# security check: is form request method POST (not GET)?
if ($ENV{'REQUEST_METHOD'} eq 'POST')
{
  # security check: was data received from authorized website?
  if ($ENV{'HTTP_REFERER'} =~ /$domain\/zf_info\/news\/usersurvey2005.html/)
  {
    &writeData;
    &beginHTML;
    &message;
    &endHTML;
  }
  else { &dienice("Security violation: This script was accessed from an unauthorized website.  Please check the URL, and try again.  $getHelp \n");}
}
else { &dienice("Security violation: Data was not submitted via the POST method.  $getHelp \n");}

### END MAIN

sub writeData {

  my $header  = "current use" . "\t";
  $header .= "compared to last year" . "\t";
  $header .= "importance-genes" . "\t";
  $header .= "importance-phenotypes" . "\t";
  $header .= "importance-expression" . "\t";
  $header .= "importance-mapping" . "\t";
  $header .= "importance-orthology" . "\t";
  $header .= "importance-sequences" . "\t";
  $header .= "importance-anatomy" . "\t";
  $header .= "importance-pubs" . "\t";
  $header .= "importance-GO" . "\t";
  $header .= "importance-community info" . "\t";
  $header .= "importance-nomenclature" . "\t";
  $header .= "importance-obtain fish lines" . "\t";
  $header .= "importance-obtain clones" . "\t";
  $header .= "importance-submit fish lines" . "\t";
  $header .= "importance-zebrafish book" . "\t";
  $header .= "importance-jobs" . "\t";
  $header .= "importance-meetings" . "\t";
  $header .= "importance rank comments" . "\t";
  $header .= "phenotype support comments" . "\t";
  $header .= "ease of use-genes" . "\t";
  $header .= "ease of use-phenotypes" . "\t";
  $header .= "ease of use-expression" . "\t";
  $header .= "ease of use-mapping" . "\t";
  $header .= "ease of use-orthology" . "\t";
  $header .= "ease of use-sequences" . "\t";
  $header .= "ease of use-anatomy" . "\t";
  $header .= "ease of use-pubs" . "\t";
  $header .= "ease of use-GO" . "\t";
  $header .= "ease of use-community info" . "\t";
  $header .= "ease of use-nomenclature" . "\t";
  $header .= "ease of use-obtain fish lines" . "\t";
  $header .= "ease of use-obtain clones" . "\t";
  $header .= "ease of use-submit fish lines" . "\t";
  $header .= "ease of use-zebrafish book" . "\t";
  $header .= "ease of use-jobs" . "\t";
  $header .= "ease of use-meetings" . "\t";
  $header .= "ease of use comments" . "\t";
  $header .= "other resource suggestions" . "\t";
  $header .= "look and feel suggestions" . "\t";
  $header .= "other comments" . "\t";
  $header .= "name" . "\t";
  $header .= "email" . "\t";
  $header .= "referer" . "\t";
  $header .= "remote address" . "\t";
  $header .= "remote host" . "\t";
  $header .= "user agent" . "\t";
  $header .= "date" . "\t";
  $header .= "";

  $headerfile = "/private/ZfinLinks/Surveys/UserSurvey2005/header.txt";

  if (!(-e $headerfile )) {
    open (LOG,">$headerfile") or &dienice("File IO: Could not open $headerfile for writing.\n");
    print LOG $header, "\n";
    close LOG;
  }

  # Survey Question Variables
  $question1 = $cgi->param('question1');
  $question2 = $cgi->param('question2');
  $question3a = $cgi->param('question3a');
  $question3b = $cgi->param('question3b');
  $question3c = $cgi->param('question3c');
  $question3d = $cgi->param('question3d');
  $question3e = $cgi->param('question3e');
  $question3f = $cgi->param('question3f');
  $question3g = $cgi->param('question3g');
  $question3h = $cgi->param('question3h');
  $question3i = $cgi->param('question3i');
  $question3j = $cgi->param('question3j');
  $question3k = $cgi->param('question3k');
  $question3l = $cgi->param('question3l');
  $question3m = $cgi->param('question3m');
  $question3n = $cgi->param('question3n');
  $question3o = $cgi->param('question3o');
  $question3p = $cgi->param('question3p');
  $question3q = $cgi->param('question3q');
  $question3comment = $cgi->param('question3comment');
  $question4comment = $cgi->param('question4comment');
  $question6a = $cgi->param('question6a');
  $question6b = $cgi->param('question6b');
  $question6c = $cgi->param('question6c');
  $question6d = $cgi->param('question6d');
  $question6e = $cgi->param('question6e');
  $question6f = $cgi->param('question6f');
  $question6g = $cgi->param('question6g');
  $question6h = $cgi->param('question6h');
  $question6i = $cgi->param('question6i');
  $question6j = $cgi->param('question6j');
  $question6k = $cgi->param('question6k');
  $question6l = $cgi->param('question6l');
  $question6m = $cgi->param('question6m');
  $question6n = $cgi->param('question6n');
  $question6o = $cgi->param('question6o');
  $question6p = $cgi->param('question6p');
  $question6q = $cgi->param('question6q');
  $question6comment = $cgi->param('question6comment');
  $question7comment = $cgi->param('question7comment');
  $question8comment = $cgi->param('question8comment');
  $question10comment = $cgi->param('question10comment');
  $name = $cgi->param('name');
  $email = $cgi->param('email');

  # General Environment Variables of User
  $referer = $cgi->referer();
  $remote_addr = $cgi->remote_addr();
  $remote_host = $cgi->remote_host();
  $user_agent = $cgi->user_agent();

  # Date Submitted (system date)
  $date = `date`;
  chomp($date);

  # Translate etraneous whitespace to actual space
  $question3comment =~ s/[\t\n\r\f]/ \\\\ /g;
  $question4comment =~ s/[\t\n\r\f]/ \\\\ /g;
  $question6comment =~ s/[\t\n\r\f]/ \\\\ /g;
  $question7comment =~ s/[\t\n\r\f]/ \\\\ /g;
  $question8comment =~ s/[\t\n\r\f]/ \\\\ /g;
  $question10comment =~ s/[\t\n\r\f]/ \\\\ /g;

  my $data = '';

  $data  = $question1 . "\t";
  $data .= $question2 . "\t";
  $data .= $question3a . "\t";
  $data .= $question3b . "\t";
  $data .= $question3c . "\t";
  $data .= $question3d . "\t";
  $data .= $question3e . "\t";
  $data .= $question3f . "\t";
  $data .= $question3g . "\t";
  $data .= $question3h . "\t";
  $data .= $question3i . "\t";
  $data .= $question3j . "\t";
  $data .= $question3k . "\t";
  $data .= $question3l . "\t";
  $data .= $question3m . "\t";
  $data .= $question3n . "\t";
  $data .= $question3o . "\t";
  $data .= $question3p . "\t";
  $data .= $question3q . "\t";
  $data .= $question3comment . "\t";
  $data .= $question4comment . "\t";
  $data .= $question6a . "\t";
  $data .= $question6b . "\t";
  $data .= $question6c . "\t";
  $data .= $question6d . "\t";
  $data .= $question6e . "\t";
  $data .= $question6f . "\t";
  $data .= $question6g . "\t";
  $data .= $question6h . "\t";
  $data .= $question6i . "\t";
  $data .= $question6j . "\t";
  $data .= $question6k . "\t";
  $data .= $question6l . "\t";
  $data .= $question6m . "\t";
  $data .= $question6n . "\t";
  $data .= $question6o . "\t";
  $data .= $question6p . "\t";
  $data .= $question6q . "\t";
  $data .= $question6comment . "\t";
  $data .= $question7comment . "\t";
  $data .= $question8comment . "\t";
  $data .= $question10comment . "\t";
  $data .= $name . "\t";
  $data .= $email . "\t";
  $data .= $referer . "\t";
  $data .= $remote_addr . "\t";
  $data .= $remote_host . "\t";
  $data .= $user_agent . "\t";
  $data .= $date . "\t";

  $processid = $$;  # special value for system process id
  $tmpfilename = time . "-" . $processid . "-" . int(rand(89)+10);  # generates random name

  $path = "/private/ZfinLinks/Surveys/UserSurvey2005";
  $filename = $path . "/$tmpfilename.txt";

  open (LOG,">$filename") or &dienice("File IO: Could not open $filename for writing.\n");
  print LOG $data, "\n";
  close LOG;

  chmod (0600,"$filename") or &dienice("chmod Failed");

  system "rm $path/results.txt";
  system "cat $path/header.txt $path/1*.txt > $path/results.txt";

  $results = "$path/results.txt";

  &emailresults('judys@cs.uoregon.edu','usersurvey2005@zfin.org',"new result posted","$results");

  return 1;
}

sub dienice {
   my($msg) = @_;
   &beginHTML;
   print "  <FONT FACE=Arial><h2>Error</h2>\n";
   print $msg;
   print "  </FONT>";
   &endHTML;
   exit;
}

sub emailresults() {
  my ($to, $from, $subject, $filename) = @_;
  ### Create a new multipart message:
  $msg = MIME::Lite->new(
             From    =>$from,
             To      =>$to,
             Cc      =>'giglias@uoneuro.uoregon.edu paea@cs.uoregon.edu',
             Subject =>$subject,
             Type    =>'multipart/mixed'
             );

  ### Add parts (each "attach" has same arguments as "new"):
  $msg->attach(Type     =>'TEXT',
             Data     =>"Please find latest results attached."
             );
  $msg->attach(Type     =>'TEXT',
             Path     =>$results,
             Filename =>'results.txt',
             Disposition => 'attachment'
             );

  $msg->send;

  return 1;
}

$import = '@import';

sub beginHTML
{
  print "Content-type: text/html\n\n";

print <<END;
<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML//EN">
<HTML>
<HEAD>
<META HTTP-EQUIV="EXPIRES" CONTENT="-2d">
<TITLE>ZFIN User Survey 2005</TITLE>

<script language="JavaScript" src="/client_sniff.js"></script>

<SCRIPT> 
  if (is_nav4) {
    document.write('<LINK rel=stylesheet type="text/css" href="/nn4_zfin_style.css">');
  } else {
    document.write('<LINK rel=stylesheet type="text/css" href="/zfin_style.css">');
  }

</SCRIPT>

<!-- <STYLE type="text/css">
  $import url(/zfin_style.css);
</STYLE> -->

</HEAD>
<BODY leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" >

<!-- Set colors --> 
<!-- Begin heading box -->
<TABLE class="header" bgcolor=<!--|LINKBAR_COLOR|--> cellpadding=0 cellspacing=0 width=100%>
  <TR bgcolor=<!--|LINKBAR_COLOR|-->>
    <TD rowspan=2 bgcolor=<!--|LINKBAR_COLOR|-->  width=80 align=left valign=bottom  >
      <IMG src="/images/zfinlogo.gif" width=80 border=0> 
    </TD>
    <TD width=100% bgcolor=#FFFFFF>
      <IMG src="/images/zfintxt.gif" border=0>
    </TD>
  </TR>
  <TR bgcolor=<!--|LINKBAR_COLOR|-->>
    <TD class="login" bgcolor=<!--|LINKBAR_COLOR|--> colspan=2>
      <TABLE border=0 height=20 cellspacing=0 cellpadding=0 width=100%>
        <TR>
          <TD>
            <DIV class="header">
              <b>The Zebrafish Information Network</b>
            </DIV>
          </TD>
        </TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>

<!-- End heading box -->

END

}

sub message {
  print '  <div style="padding:25px;"><h3>Thank you!</h3>', "\n";
  print '  <p>Your survey has been submitted to ZFIN.  We value your input.<br>', "\n";
  print '  Click <a href="/', $domain, '">here</a> to go to ZFIN.</p></div>', "\n";
}

sub endHTML
{
  if ($debug_mode) {
    print "random identifier == ", $tmpfilename, "<br>", "\n";
    foreach $name (sort keys %ENV) {
      print $name, " == ", $ENV{$name}, "<br>", "\n";
    }
  }

print <<END;
      <TABLE class="footer" bgcolor=<!--|LINKBAR_COLOR|--> width=100%>
        <TR bgcolor=<!--|LINKBAR_COLOR|-->>
          <TD bgcolor=<!--|LINKBAR_COLOR|-->  >
            <DIV class="header">
              <P align=center>
              Development of the Zebrafish Database is generously supported by
              <br>the NIH (P41 HG002659). 
              <br>
              Copyright <font color="#FFFFFF">&copy</font> University of Oregon, 1994-2004, Eugene, Oregon.
              <br> <font size="-2">ZFIN logo design by Kari Pape, </font><font size="-2">University of Oregon</font>
              </P>
            </DIV>
          </TD>
        </TR>
      </TABLE>


	</body>
</html>
END

}

