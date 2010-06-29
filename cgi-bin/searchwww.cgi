#!/private/bin/perl 
# -wT # changed tec 01-18-01
#
# NOTE: AS OF 2000/11/16 THIS IS THE ONLY CGI SCRIPT FROM THE ZFIN WEB SITE
#       THAT RUNS ON THE MIRROR SITES AS WELL.  ANY CHANGE TO THIS SCRIPT
#       MUST ALSO BE PROPAGATED TO THE MIRROR SITES.
#

## This is a simple program that takes a single string argument on the command 
## line and searches all HTML files in the current directory for that string.


## Standard subroutine to parse POST form input, placing form variables into normal perl variables.

sub parse_form {
    local (*data, $type) = @_;
    local ($i, $loc, $key, $val);

    # Read in text
    if ($type =~ /get/i) {
	$data = $ENV{'QUERY_STRING'};
    } else {
	read(STDIN, $data, $ENV{'CONTENT_LENGTH'});
    }

    @data = split(/&/,$data);

    foreach $i (0 .. $#data) {
	# Convert plus's to spaces
	$data[$i] =~ s/\+/ /g;

	
	# Convert %XX from hex numbers to alphanumeric
	$data[$i] =~ s/%(..)/pack("c",hex($1))/ge;
	
	# Split into key and value.
	$loc = index($data[$i],"=");
	$key = substr($data[$i],0,$loc);
	$val = substr($data[$i],$loc+1);
	$data{$key} .= '\0' if (defined($data{$key})); # \0 is the multiple sep
	$data{$key} .= $val;
    }
    return 1;
}




# Subroutine to print out specified found line with a hot link to the
# file that contains it.
sub html_link {
    local($found_line,$found_file,$position,$search_string_lc)=@_;


# Now chop the found line down to a reasonable size -- don't print whole line, which may stretch for paragraphs!
    $start_index=$position-80;
    if ($start_index<0) {$start_index=0};
    $end_index=$position+80;
    if ($end_index>length($found_line)) {$end_index=length($found_line)};
    $output_line=substr($found_line,$start_index,$end_index-$start_index);

    $position=$position-$start_index;

    @outline=split(/ /,$output_line);
    $num_words=@outline;
    if (index(lc($outline[0]),$search_string_lc)<0) {$outline[0]=""};
    if (index(lc($outline[$num_words-1]),$search_string_lc)<0) {
	$outline[$num_words-1]="";
    }
    $output_line=join(" ",@outline);

    
    # Knock the HTTP_root path off front of filename so that it becomes
    # a viable hotlink.
    $found_file =~ m#$HTTP_root(.*)#;
    $link_path=$1;
    
    # highlight the search string within found line.
    $output_line=~ s/($search_string_lc)/<font color="#ff0000">$1<\/font>/gi;
    
    # Now just print the hotlink!
    print "<li><A HREF=\"$link_path\">$link_path</A>: ...$output_line...\n";
}



# Subroutine to search any specified directory
sub do_directory {

    local ($search_string_lc,$directory,$indent)=@_; #input params

    # Must declare all subroutine variables as local to avoid global/local
    # naming conflicts!
    local($wd,$myfile,$next_line,$next_line_lc,$found_index);


    $indent="$indent     ";  # Add some more indentation

#    print "$indent SEARCHING $directory\n";

    # This used to print an error if the chdir failed.
    # Changed it to just ignore directories it can't cd into on the
    # theory that if we make a directory inaccessible, then we don't
    # want it searched.
    if (chdir($directory)) {  # Get there first!
      $wd=`pwd`;
      chop($wd);

      foreach $myfile (<*>) {
	$full_pathname="$wd/$myfile";
	if (($myfile =~ /.*\.html$/) && ($myfile=~/^[A-Za-z0-9]/)) {
	    open(INFILE,"$myfile") || die "Can't open $myfile!\n";
#	    print "$indent Doing HTML: $myfile\n";
	    
	    while ($next_line=<INFILE>) {
		$next_line_lc = lc($next_line);
		$found_index=index($next_line_lc,$search_string_lc);
		if ($found_index!=-1) {
		    $next_line_lc =~ s/<[^>]*>//g; # take out HTML tags
# If it still contains  search_string_lc, print it
		    if ($found_index=index($next_line_lc,$search_string_lc)) {
			$next_line =~ s/<[^>]*>//g; # take out HTML tags
			&html_link($next_line,$full_pathname,$found_index,
				   $search_string_lc);
			$hit_count++;
		    }
		}
	    }
	}
	elsif (-d $myfile && ($myfile=~/^[A-Za-z0-9]/)) {
#	    print "$indent Doing subdirectory: $myfile\n";
	    &do_directory($search_string_lc,$full_pathname,$indent);
	    chdir($directory) || print "Return chdir to $directory failed!\n";  # restore current dir!
	}
      }
    }
#    print "$indent DONE, returning to parent dir\n";    
}




#  BEGIN MAIN PROGRAM 

# Change this to your HTTP root
$HTTP_root="<!--|ROOT_PATH|-->/home";

# Change this to whatever directory within HTTP server you want it to start
# searching in.
$start_directory="$HTTP_root/zf_info";
$indent="";

# Call parse-form to parse the form variables (in this case just the 
# search string) into PERL array.
# &parse_form(*form_data, "post", "dumpfiles");
&parse_form(*form_data, "get", "dumpfiles");
$search_string=$form_data{search_string};

$hit_count=0;

# Okay, now start the HTML output

print "Content-type: text/HTML\n\n<HTML>";

 print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/header.js'>
 </script>";

print "<table width='100%'><TR><TD width='80%'><font size=+1><b>RESULTS: Search for $search_string</b></font></TD><TD width='20%'><a href=#modify>Modify Search</a></TD></TR></table>\n\n<UL>";
 
# Call do_directory on the first target directory to start things off!
$search_string_lc = lc($search_string);
&do_directory($search_string_lc,$start_directory,$indent);

print "</UL>\n\n<b>Total of $hit_count matches found.</b>\n";
print "<p>";

print "<Table class=search width=100%>";
print "<TR>";
print "<Td class=titlebar colspan=2>";
print "<a name=modify></a>Modify your search.";
print "</Td>";
print "</TR>";
print "<FORM  METHOD=get action='/cgi-bin/searchwww.cgi'>";

print "<TR>";
print "<TD>";
print "<input type=text name=search_string size=15 value='$search_string'>";
print "<input type=submit value='Start Search'>";
print "</TD>";
print "</TR></TABLE>";

print "</form>";


print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script></HTML>";


