#! /private/bin/perl

# This files translates an .apg page into a comperable .js file. All Datablade
# tags and coding are removed. Datablade tags must be at the start of lines
# unless preceded by spaces. When Datablade tags are nested, inner tags do not
# need to start lines.


open (APG, "$ARGV[0]") or die "Usage: apgTojs.pl filename.\n";

$flag = 0;
while ($line = <APG>) {
  chop $line;
  #escape all quotes
  $line =~ s/\"/\\\"/g;
  $line =~ s/\'/\\\'/g;

  #flag opening Datablade tags
  while ( $line =~ /\<\?[a-z]/gi ) {
    $flag++;
  }

  print "document.write(\"", $line, "\");\n" if ( $flag == 0 );

  #unflag closing Datablade tags
  while ( $line =~ /\<\?\//g ) {
    $flag--;
  }
}

close (APG);
exit;
