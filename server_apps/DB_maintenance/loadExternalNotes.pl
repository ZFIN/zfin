#!/opt/zfin/bin/perl
#
# loadExternalNotes.pl
#
# For ZFIN-6262 and bulk external note loading in the future

$note1 = "The construct was inserted on the plus strand of the genome. The gene in which the insertion occurred is also on the plus strand.";

$note2 = "The construct was inserted on the minus strand of the genome. The gene in which the insertion occurred is on the plus strand.";

$note3 = "The construct was inserted on the plus strand of the genome. The gene in which the insertion occurred is on the minus strand.";

$note4 = "The construct was inserted on the minus strand of the genome. The gene in which the insertion occurred is also on the minus strand.";

chdir "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/";

system("/bin/rm -f notesInput");
system("/bin/rm -f notes");

system("scp /research/zarchive/load_files/notesInput <!--|ROOT_PATH|-->/server_apps/DB_maintenance/");

open (INPUT, "notesInput") ||  die "Cannot open notesInput : $!\n";

@lines = <INPUT>;

open (OUTPUT, ">notes") || die "Cannot open notes : $!\n";

foreach $line (@lines) {
  if ($line) {

    @fields = split(/\s+/, $line);
  
    print OUTPUT "$fields[0]|$fields[1]|";

    if ($fields[2] eq '1') {
       print OUTPUT "$note1\n";
    } elsif ($fields[2] eq '2') {
       print OUTPUT "$note2\n";
    } elsif ($fields[2] eq '3') {
       print OUTPUT "$note3\n";
    } elsif ($fields[2] eq '4') {
       print OUTPUT "$note4\n";
    } 

  }

}


close INPUT;

close OUTPUT;

system("psql -d <!--|DB_NAME|--> -a -f loadExternalNotes.sql");

exit;


