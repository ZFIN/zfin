#!/private/bin/perl

my $mailprog = '/usr/sbin/sendmail -t -oi -oem';
my ($day, $month, $year) = (localtime)[3,4,5];
$month += 1;
$year +=1900;

chdir "/common/scratch/zblast";
my $retcode = `/usr/bin/find . -name "W*" -type d -ctime 1 -print >> /tmp/dailyJobs.in`;

open IN, "</tmp/dailyJobs.in" or die "Cannot open /tmp/dailyJobs.in for read";
open OUT, ">>/tmp/dailyJobs.out" or die "Cannot open file to write";

while (<IN>) {
    my $dir = $_;
    next if ($dir !~ /^\./);

    my $line_time = `tail -3 $dir/blastwu.txt | grep Start`;
    if ($line_time =~ /Start:.+(\d+:\d+:\d+).+End:.+(\d+:\d+:\d+)/) {
	my $start_time = $1;
	my $end_time = $2;
	my $elapsed = $2 - $1;
    }
    print OUT "$dir\t$start_time\t$end_time\t$elapsed\n";

}
close IN;
close OUT;

$retcode = `sort -t \t -k2,3 /tmp/dailyJobs.out > /Users/informix/usageReport/Joblist.txt`;

#rm /tmp/dailyJobs.in;
#rm /tmp/dailyJobs.out;

exit;

