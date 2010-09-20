#!/private/bin/perl -w 
#------------------------------------------------------------------------
# 
# Removes all the MI tags, and anything they enclose from a file.
#
# We use this only to convert the ZFIN home page from a dynamically generated
# app page into a static page.  In the case of the home page, this produces
# a viable static page.  For most of our app pages, this script would 
# produce a meaningless static page.
#
# We need a static web page for two reasons:
#  1. To support the static web page update process (i.e. the frost process)
#  2. Mirror sites (if we decide to continue to support them) need a 
#     a static home page.
#
# Usage:
#  
#  stripmicode.pl  < input_file  > output_file
#
#  The input file is passed in stdin, and the output goes to standard out.
#
# Returns
#
#  0 No errors were detected.
# >0 Errors were detected.  See stderr for details.
#
# $Id: stripmicode.pl,v 1.2 2002-07-30 01:15:53 informix Exp $
# $Source: /research/zusers/ndunn/CVSROOT/Commons/bin/stripmicode.pl,v $

use English;


#----------------------------------------------------------------------
# Get Next Token.  Returns next token from input
#
# Params
#  none
#
# Globals used
#  <STDIN>        - input is read from stdin
#  $globalLine    - holds current line from file
#  $globalLinePos - current position in $globalLine
#
# Returns 
#  tokenType  - MISTART, MIEND, TEXT, or FILEEND
#  textValue  - empty if Token type is not text

sub getNextToken() {

    my $tokenType = $UNKNOWN; 
    my $currentChar;
    my $startPos;
    my $textValue = "";

    if ($globalLinePos == -1) {

	$globalLinePos = 0;
	# need to read in line
	if (eof(STDIN)) {
	    return ($FILEEND, $textValue);   # !!!! EARLY EXIT  !!!!!
	}
	$globalLine = <STDIN>;
    }

    $startPos = $globalLinePos;
    my $miPos = index($globalLine, "<?", $startPos);
    
    if ($miPos >= 0) {

	# There is a MI tag somewhere in line.  

	if ($miPos == $startPos) {
	    # MI tag occurs at beginning of rest of line

	    if (length(substr($globalLine, $globalLinePos)) >= 8 &&
		substr($globalLine, $globalLinePos, 8) eq "<?MIELSE") {
		$tokenType = $MIELSE;
		$globalLinePos += 8;
	    }
	    elsif (length(substr($globalLine, $globalLinePos)) >= 4 &&
		   substr($globalLine, $globalLinePos, 4) eq "<?MI") {
		$tokenType = $MISTART;
		$globalLinePos += 4;
	    }
	    elsif (length(substr($globalLine, $globalLinePos)) >= 5 &&
		   substr($globalLine, $globalLinePos, 5) eq "<?/MI") {
		$tokenType = $MIEND;
		$globalLinePos += 5;
	    }
	    # search until closing > is found
	    my $miEndPos;
	    while (-1 == 
		   ($miEndPos = index($globalLine, ">", $globalLinePos))) {
		if (eof(STDIN)) {
		    return ($FILEEND, $textValue);   # !!!! EARLY EXIT  !!!!!
		}
		$globalLine = <STDIN>;
		$globalLinePos = 0;
	    }
	    $globalLinePos = $miEndPos + 1;
	}
	else {
	    # Token is text, grab resst of line up to MI tag
	    $tokenType = $TEXT;
	    $textValue = substr($globalLine, $startPos, $miPos - $startPos);
	    $globalLinePos = $miPos;
	}
    }
    else {
	# No MI tag in line
	$tokenType = $TEXT;
	$textValue = substr($globalLine, $startPos);
	$globalLinePos = -1;
    }
    return ($tokenType, $textValue);
}    

#------------------------------------------------------------------------
# Main.
#

# Define Token types

$UNKNOWN = 0;
$MISTART = 1;
$MIEND   = 2;
$MIELSE  = 3;
$TEXT    = 4;
$FILEEND = 5;

# Globals

$globalLine = "";
$globalLinePos = -1;    # -1 indicates no current line.

my $tokenType;
my $textValue;
my $miDepth = 0;

($tokenType, $textValue) = getNextToken();

while ($tokenType != $FILEEND) {

    if ($tokenType == $MISTART) {
	$miDepth++;
    }
    elsif ($tokenType == $MIEND) {
	$miDepth--;
    }
    else {    # TEXT
	if ($miDepth == 0) {
	    print ($textValue);
	}
    }
    ($tokenType, $textValue) = getNextToken();
}

exit $miDepth;
