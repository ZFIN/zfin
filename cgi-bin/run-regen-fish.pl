#! /local/bin/perl
#
#  $Source$
#  $Id$
#
#  Call the regen_fishsearch () function and return its output to our caller.
#  This program is required because the regen_fishsearch () function contains
#  SQL calls that are not permitted in a SQL function called from within
#  the WebBlade.  This program adds the necessary extra level of indirection.
#
#  2000/10/05 DPC: This file was reworked in preparation for genericizing it.
#

$ENV{"INFORMIXDIR"}      = "<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSQLHOSTS"} = "$ENV{INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"ONCONFIG"}         = "<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSERVER"}   = "<!--|INFORMIX_SERVER|-->";
$dbaccess                = "$ENV{INFORMIXDIR}/bin/dbaccess";

$TEMP_IN    = "temp_in.sql";
$TEMP_DIR   = "/tmp";
$INFILE_DIR = "<!--|ROOT_PATH|-->/home/ZFIN/APP_PAGES/SQL_docs";

local ($result, $rows)= (0, 0);

 DO_DBACCESS:
{
    if (! open (SQL, ">$TEMP_DIR/$>$TEMP_IN"))
    {
	@dbout = ("run-regen-fish.pl:  Can't open temp file to pass to dbaccess:", "$!");
	last DO_DBACCESS;
    };
    print SQL "connect to '<!--|DB_NAME|-->';";
    if ( ! open (SQLIN, "$INFILE_DIR/regen-fishsearch-call.sql"))
    {
	@dbout = ("run-regen-fish.pl:  Couldn't open input script:", "$!");
	last DO_DBACCESS;
    };
    while (<SQLIN>)
    {
	print SQL $_;
    }
    close (SQL);
    close (SQLIN);
    @dbout = `$dbaccess - $TEMP_DIR/$>$TEMP_IN 2>&1`;

    # Use the following statement (and comment out the one above) for a 
    # quick test of code that executes when the SQL script succeeds.
    #		@dbout = (' 1 ', '1 row(s) retrieved.');
    # Use the following statement (and comment out the dbaccess call above) 
    # for a quick test of code that executes when the SQL script fails.
    #           @dbout = (' 1 ', '0 row(s) retrieved.');

    foreach $_ (@dbout)
    {
	$result = $1 if /^\s*([01])\s*$/;
	$rows = 1 if /^\s*1 row\(s\) retrieved.\s*$/i;
    }
}

print <<"End";
Content-type: text/html

<html>
<head>
<META HTTP-EQUIV="EXPIRES" CONTENT="Fri Feb 04 09:35:25 PDT 2000">

<title>Regenerating fishsearch table</title>
</head>
<body>
<h1>Regenerating fishsearch table</h1>
End
if ($result && $rows)
{
    print "The table was successfully regenerated.\n";
}
else
{
    print "An error occurred regenerating the table.  See following output:<p>\n";
    print join ("<BR>\n",@dbout);
    print "<P>\n";
}

print <<"End2";
<form method=post action="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->">
<input type=submit name=action value="Return to ZFIN home page">
<input type=hidden name=MIval value=aa-ZDB_home.apg>
</form>
</body>
</html>
End2

exit;
