#! /private/bin/perl
#	This perl utility processes the web log files on the command line,
#	and attempts to lookup the domain names for all the IP addresses in
#	the logs. An associative array is kept to speed things up.
#
#	Clif Cox 6/17/98

# print "Your arguments are: " . join(" ", @ARGV) . "\n";
@args = @ARGV; @ARGV = ();

foreach $file (@args) {
	open(IN, "${file}") || die "Can't open $file file: $!";
	open(OUT, ">$file.domlu$$") || die "Can't open $file.domlu$$ file: $!";

	while (<IN>) {
		@_ = s/(\d+)\.(\d+)\.(\d+)\.(\d+)/&lookup($1, $2, $3, $4)/ge;
		print OUT;
	}
	close IN; close OUT;

	($_, $in_l, $in_w) = split(/\s+/, `/bin/wc $file`);
	($_, $out_l, $out_w) = split(/\s+/, `/bin/wc $file.domlu$$`);
	if (($in_l == $out_l) && ($in_w == $out_w)) { rename("$file.domlu$$", $file) }
	else { print STDERR "ERROR files $file, and $file.domlu$$ don't match!\n" }
}


sub lookup {
	local ($a, $b, $c, $d) = @_;
	local ($name);

	return $name if $name = $hash{"$a.$b.$c.$d"};
	@_ = gethostbyaddr(pack('C4', $a, $b, $c, $d), 2);
	$name = $_[0] ? $_[0] : "$a.$b.$c.$d";
	$hash{"$a.$b.$c.$d"} = $name;
	$name;
}

