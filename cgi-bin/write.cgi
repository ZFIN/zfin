#!/private/bin/perl -wT # changed tec 01-18-01
#!/local/bin/perl

#Date: 10/22

#A program to write out an image   to an HTML file 
#and log username and referring HTML page to a logfile

#put image of small dot in $image
#the logfile is $logfile


$image="dummy.gif"; 
$logfile="logfile";


$!=1;#unbuffer the output
open(IMG,"$image") || die "Couldnt open that image"; #open a dummy image
print "Content-type: image/gif\n\n";  # tell the server the MIME type

if (!(-e $logfile)) {  #does the logfile already exist ?
    open(LOG,"> $logfile") || die "Couldnt open file .logfile";
} 
else { #if it does append data to the end
    open(LOG,"> $logfile") || die "Couldnt open file .logfile";
}
print <IMG>;
$datestr=`date`;
chop($datestr);
$customer=$ENV{"HTTP_COOKIE"};
$customer=~ s/.*CUSTOMER\=//g;
print LOG "$customer:$ENV{HTTP_REFERER}:$ENV{REMOTE_HOST}:$datestr\n";
close(LOG);

close(IMG);





