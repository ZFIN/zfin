#!/private/bin/perl -w


open (SOURCES, "< ./source.unl")
    or die "Cannot open source.unl \n";

open SOURCE_MDFD, "> ./source_mdfd" 
    or die "Cannot open the source_mdfd file\n";

@sources = <SOURCES>;

$new_line = "\n";

foreach $source (@sources) {

    # replace pipe with nothing,replace new line with nothing
    $source =~ s/$new_line//g;
    $source =~ s/\s\Z//;
    $old_source = $source;

    $source =~ s/ZDB-\D+-\d{1,6}-\d{1,6}|//;   
    $source =~ s/\|//g;
    
    if ($source =~ m/(pre-pub online)/) {
	$prepub = $1 ;
	$source =~ s/pre-pub online//g;
    }
    elsif ($source !~ m/\spre-pub online/) {
	$prepub = '' ;
    }

    if ($source =~ /(\D+)\s\d+/) {
        $new_abbrev = $1 ;
    }

    elsif ($source !~ m/\d/) {
	$new_abbrev = $source;
    }

    if ($source =~ /(\d+):/) {
	$new_volume = $1;
    }
    elsif ($source =~  /\D+(\d+\(\d+\)):.+/){
	$new_volume = $1;
    }
    elsif ($source =~ /\D+(\d+\(\d+\-\d+\)):.+/){
	$new_volume = $1;
    }
    elsif ($source =~ /\D+/) {
	$new_volume = "";
    }


    if ($source=~ /.+:(\d+\-\d+)/) {
	$new_pages = $1;
    }
    elsif ($source =~ /.+:(\d+)/) {
	$new_pages = $1;
    }
    elsif ($source =~ /.+(\d+\-\d+)/) {
	$new_pages = $1;
    }
    elsif ($source =~ /.+:(\D+\d.*)/) {
	$new_pages = $1;
    }
    elsif ($source !~ m/:\d.*/) {
	$new_pages = "" ;
    }

    elsif ($source =~ /(p\.\s\d.*)/){
	$new_pages = $1;
    }
    elsif ($source =~ /(pp\.\s\d.*)/){
	$new_pages = $1;
    }
    elsif ($source =~ /(pp\s\d.*)/){
	$new_pages = $1;
    }
    elsif ($source =~ /(p\s\d.*)/){
	$new_pages = $1;
    }

    elsif ($source !~ m/:/) {
	$new_pages = "" ;
    }
     
#he moved files to a report 

    print SOURCE_MDFD "$old_source$new_abbrev|$new_volume|$new_pages|$prepub|\n";
}

close SOURCES;
close SOURCE_MDFD;
exit;
