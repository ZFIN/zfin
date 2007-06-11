#! /local/bin/perl

use Socket;
init();

#SET PARAMETERS
$db       = "Protein"; 		#getparms("Database", "Protein", "Choices: Nucleotide, Protein");
$term     = "txid7955"; 	#getparms("Entrez Query", "Danio+rerio[orgn]", "Choices: try also biomol+mrna[prop],biomol+genomic[prop]");
$rettype  = "gp"; 		#getparms("Format", "gp", "Choices: fasta, gb, gp");
$retmode  = "text"; 		#getparms("Encoding", "text", "Choices: xml, text, asn, html");
$out_file =  "<!--|ROOT_PATH|-->/server_apps/data_transfer/GenPept/sequences.gp"; 	#getparms("Output file", "refout", "");

open (OUT,">$out_file");

#run initial search and get number of records, web environment

$tool="esearch";
$results=getUrl("$ebase$tool.fcgi?usehistory=y&db=$db&term=$term&dopt=GenPept&email=bsprunge/@cs.uoregon.edu");
$results=~/<Count>(\d+)<\/Count>.*<QueryKey>(\d+).*<WebEnv>(.+)<\/WebEnv/s;
($nrecords,$qkey,$webenv)=($1,$2,$3);

#download in user-specified format and write to output file
$tool="efetch";
$ts=time;

#2005-01-03 
#download records in one request. 
#subsequent requests receive an error message from the ncbi server.
#$retmax=$nrecords;

for($retstart=0;$retstart<$nrecords;$retstart+=$retmax){

    $t=time-$ts;if(3-$t>0){sleep(4-$t);}   #max one call per 3 seconds for scripts at NCBI
    $p=int($retstart/$nrecords*10000)/100; #calculate percentage of records downloaded

    ## uncomment to view download progress
    print STDERR "Elapsed time: $t seconds\t$p%, $retstart records of $nrecords retrieved.\n";

    $results=getUrl("$ebase$tool.fcgi?usehistory=y&db=$db&retmax=$retmax&retstart=$retstart&rettype=$rettype&WebEnv=$webenv&query_key=$qkey&retmode=$retmode");
    print  OUT $results;

}

close OUT;

sub init(){
  $ebase="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
  $retmax=300;

}

sub getUrl() {

  local $url=shift;

  local $host = "www.ncbi.nlm.nih.gov";
  local $port = 80;
  local $socketaddr= 'S n a4 x8';
  local $results;

  $socketaddr= sockaddr_in $port, inet_aton $host or die "Bad hostname\n";
  socket SOCK, PF_INET, SOCK_STREAM, getprotobyname('tcp') or die "Bad socket\n";
  connect SOCK, $socketaddr or die "Bad connection\n";
  select((select(SOCK), $| = 1)[0]);

  print SOCK "PUT ".$url."\r\n";
 
  while ( <SOCK> ) {
    $results .= $_;
  }

  close SOCK;

  return $results;
}

sub getparms() {
  #$_[0] :: STRING question
  #$_[1] :: STRING default value
  #$_[2] :: STRING choices
  
  print "$_[0]: $_[2]\n [$_[1]]: ";
  local $rc = <>;
  chomp $rc;  #remove the line return
  if($rc eq "") { $rc = $_[1]; }
  return $rc;
}


