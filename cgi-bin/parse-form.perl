#!/local/bin/perl 
# ----------------------------------------------------------------------
#  file name:  parse-multipart.perl
#
#  Programmer:  Eck Doerry
#  Description: Parses the output from an ENCTYPE=multipart/form-data"
#	form into an array. It can handle input of 1+ files no problem, except
#	for it doesn't do multiple files passed in a single "file" input
#	statement (which is actually allowed, but rare). EAch file is stored
#	in a flat file "temp###". You can then do what you like with it
#	later in the script.
#
#  Date MM-DD-YY		Date of file creation
#
#  Modification History
#    Added a chunk for handling file-uploads to the subroutine parse-form, which was
#    originally from Jeff Hobbes
# -----------------------------------------------------------------------


sub parse_form {
    local (*data, $savedir) = @_;
    local ($i, $loc, $key, $val);

    # Read in text
    if ($ENV{REQUEST_METHOD} =~ /get/i) {	# Form send using GET
	$data = $ENV{'QUERY_STRING'};
    } else {			# Form send using POST
	read(STDIN, $data, $ENV{'CONTENT_LENGTH'});
    }

    # Now check if dealing with multipart form.
    if ($ENV{CONTENT_TYPE}=~ /multipart/i) { # It is multipart input

	#first snag the boundary string, which comes in as an env. variable.
	$ENV{CONTENT_TYPE} =~ /boundary=/;
	$bound = "--$'-{0,2}\r\n";  #the boundary string is everything after the = sign, plus
				# plus, for some reason, the actual boundary string
				# used has two more dashes on front, plus, the very
				# last boundary also has two extra dashes following
				# the boundary, which we also want to catch


	@data = split(/$bound/,$data);

	foreach $item (@data) {
	    @mpart= split(/\r\n/,$item); # Split by return/newlines
	    if ($vname = &getvar("name", $mpart[0])) { # still a valid part to process

		if (index($mpart[1],"Content-Type") != -1) {	#processing file upload
		    if (! ($varvalue= &getvar("filename",$mpart[0]))) {
			print "\nERROR: malformed expression in form data!\n";
			exit 0;	
		    }			

		    $varvalue = $savedir."/TMP".$varvalue;
		    open(OUTFILE,">$varvalue") || die "Couldnt open $varvalue";
		    foreach $i (3 .. $#mpart) {
			print OUTFILE "$mpart[$i]\r\n";
		    }
		    close(OUTFILE);
		    chmod 0666, $varvalue; # Make it world accessible

		    $data{$vname}=$varvalue; # Save filename=<localfilename>

		} elsif ($mpart[1]=="") { # We are processing a plain old variable

		    $varvalue="";
		    foreach $i (2 .. $#mpart) {
			$varvalue .= $mpart[$i]."\r\n";
		    }
		    chop($varvalue); # Chop off dangling \r\n from end
		    chop($varvalue);
		    $data{$vname}=$varvalue;

		} else {	# screwy input, do nothing and go for next multipart
		}

	    } else {		# it's a garbage multipart, so just do nothing
	    }
	  	
	}


    } else {			# its normal form input (i.e. not multipart)

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

    #Simple subroutine to pick out the value of a variable is a string 
    # containing "token=value" --> function returns the value of the named var.
    sub getvar {
	local($token,$astring) = @_;
	if ($astring =~ /$token=\"([^\"]*)\"/) {  
	    $1;
	} else {
	    0;  #Send back a false
	}
    }

}


1;
