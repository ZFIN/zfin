#!/private/bin/perl -wT # changed tec 01-18-01
#!/local/bin/perl 
# ----------------------------------------------------------------------
#  file name:  urlencode.perl
#
#  Programmer:  Eck Doerry
#  Description: A simple subroutine to turn a bunch of name/value pairs
# which are stored in the input array, DATA, into a traditional urlencoded
# string, such as that produced by a normal POST or GET form. The string is what is
# returned as the function value.
#
#  Modification History
#    
# -----------------------------------------------------------------------



sub build_urlencoded {
    local (*data) = @_;
    local ($paramstr, $tmp, $param);

    $paramstr = "";
    foreach $param (keys (%data)) {
	$tmp = $data{$param};
	
	# First convert all but basic chars to hex escapes
	$tmp =~ s/([^\w-., ])/"%".sprintf("%2x",unpack("C",$1))/ge; 
	$param =~ s/([^\w-., ])/"%".sprintf("%2x",unpack("C",$1))/ge;  
	
	$tmp = $param."=".$tmp;	# Glue them together, now that you've escaped any
				# internal = and + signs.
	
	$tmp =~ s/% /%0/g;		# sprintf should  pad with 0's but it doesn't
				        # so replace its spaces with 0's manually
	
	$tmp =~ s/ /\+/g;		# Convert any remaining spaces to pluses
	
	$paramstr .= $tmp."&";	# Glue onto paramstr with & seperator
    }				
    chop($paramstr);		# Hack off the last ampersand

    $paramstr;
}


1;
