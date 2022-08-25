#!/opt/zfin/bin/perl

#
#  sp_parser.pl
#
#  This script parse the checked SWISS-PROT records, get each field in
#  the record into different files with informix loadable format.

use DBI;
use Cwd;

use FindBin;
use lib "$FindBin::Bin/../../";
use ZFINPerlModules qw(assertEnvironment);
assertEnvironment('DB_NAME', 'PGHOST');

my $first = 1;       #indicate the beginning of DR(the end of CC)

my (@ext_dr, @cc, $cc, @sp_ac, $sp_ac, $sp_ac_list, $embl, $gene, $single_gene, @gene_array, $kw);


# Files to be loaded into zfin.
open DBLINK, ">dr_dblink.unl" or die "Cannot open dr_dblink.unl:$!";
open ACC, ">ac_dalias.unl" or die "Cannot open ac_dalias.unl:$!";
#one sp record might be associated with >1 genes, don't process GN lines
#open GNAME, ">gn_dalias.unl" or die "Cannot open gn_dalias.unl:$!";
open COMMT, ">cc_external.unl" or die "Cannot open cc_external.unl:$!";
open KEYWD, ">kd_spkeywd.unl" or die "Cannot open kd_spkeywd.unl:$!";

my $zfindbname = "$ENV{'DB_NAME'}";
my $dbhost = "$ENV{'PGHOST'}";
my $username = "";
my $password = "";

my $dbh = DBI->connect ("DBI:Pg:dbname=$zfindbname;host=$dbhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$kwMultLinesConcatenated = " ";
while (<>) {
 #ID   O12938      PRELIMINARY;      PRT;   412 AA.
 if (/^ID.*\s+(\d+)\sAA\./) {
     $len=$1;
     next;
  }

  #AC   O12990; O73880;
  #AC   O42345;
  if (/^AC\s+(.*)/) {
      $sp_ac_list = $sp_ac_list.$1.' ';
      next;
  }

  #GN   rag1 or RAG1 or RAG-1.
  #   or
  #GN   Name=otx1l; Synonyms=otx3;
  #   or
  #GN   Name=pax2a; Synonyms=pax2.1, noi, paxzf-b;
  #if (/^GN\s+(.*)/) {
  #  $gn = $1;  chop($gn);              #chop period sign
  #  if ($gn =~ /Name=/i) {
  #	if ($gn =~ /Synonyms=(.*)/i) {
  #	    $gn = $1;
  #	    @sp_gname = split(/,\s+/i, $gn);
  #	}
  #  }else {
  #	@sp_gname = split(/\s+or\s+/i, $gn);
  #  }
  #  next;
  #}
  # skip unused lines (RX, RL would present in rare cases)
  if (/^GN/ || /^RX/ || /^RL/) {
     next;
  }


  #DE   Tyrosine-protein kinase Jak1 (EC 2.7.1.112) (Janus kinase 1) (Jak-1).q
  # since Jul 22, 2008:
  #DE            EC=3.4.24.81;
  # see http://www.chem.qmul.ac.uk/iubmb/enzyme/
  if (/^DE\s+EC=(.*);/) {

          $ecnumberLine = $1;

          if ($ecnumberLine =~ m/\{/) {   ## EC=2.7.4.6 {ECO:0000256|RuleBase:RU004013};
             @ecLineSplit = split(/\{/, $ecnumberLine);
             $ecWithBracket = $ecLineSplit[0];
             $ecWithBracket =~ s/\s+$//;     ## strip trailing space
             $ecnumber = $ecWithBracket if $ecWithBracket =~ m/[\d\.\-]*/;
          } else {                            ## EC=2.7.8.2;
             $ecnumber = $ecnumberLine if $ecnumberLine =~ m/[\d\.\-]*/;
          }
  }

  #CC   -!- SUBCELLULAR LOCATION: Nuclear (By similarity).
  #CC   -!- SIMILARITY: CONTAINS 2 LIM DOMAINS. THE LIM DOMAIN BINDS 2 ZINC
  #CC       IONS.
  if (/^CC\s+-!-\s(.*)/) {
      my $ccinfo = $1;  #$1 would lost if there is a match in the following if clause
      # put each item of the comments into array
      push (@cc, $cc) if ($cc && $cc!~/CAUTION/ && $cc!~/ALTERNATIVE PRODUCTS/);

      $cc = $ccinfo;
      next;
  }
  if (/^CC\s+(.*)/) {
      $cc = $cc.' '.$1;       #concatenate and form one item
      next;
  }

  #DR   EMBL; U71094; AAC60366.1; -.	ZDB-GENE-990415-235
  #DR   ZFIN; ZDB-GENE-020711-2; lmyc1.
  #DR   InterPro; IPR004321; RAG2.
  #DR   Pfam; PF03089; RAG2; 1.
  # ok records from the sp_check always get one gene id, either in the
  # DR EMBL line as a result of GP match or in a single line as a result
  # of GB match.
  # ok records from curators have at lease one DR ZFIN lines

  if (/^DR\s+EMBL/) {
      @dr = split;
      $_ = pop(@dr);         #get zfin accession number for the SP record
      $single_gene = $_ if /ZDB/ ;
      next;
  }

  #\t\tGB match: ZDB-GENE-*-*     #sp_check.pl write out this line
  if (/GB match: (.*)/) {
      $single_gene = $1;
      next;
  }

  if (/^DR\s+ZFIN;\s+(.+);\s/ ) {
      push @gene_array, $1;
      next;
  }

  # if there is a EMBL match, and only one ZFIN line, use the EMBL match.
  if (@gene_array < 2 && $single_gene) {
     $gene_array[0] = $single_gene;
  }

  if (/^DR/ ) {
      @dr = split;
      $dbname = $dr[1]; chop($dbname);
      $acc_num = $dr[2]; chop($acc_num);

      foreach $gene (@gene_array) {
          if(
              $dbname eq "InterPro"
              or
              $dbname eq "Pfam"
              or
              $dbname eq "PROSITE"
              or
              $dbname eq "PDB"
#              or   $dbname eq "UniProt" # UniProt added in the UniProt section
              ){
              print DBLINK "$gene|$dbname|$acc_num|\n";
          }
      }
      next;
  }

  # old format:
  #KW   DNA-binding; Nuclear protein; Transcription regulation; Activator;
  #KW   Neurogenesis; Developmental protein; Differentiation.

  ###   format changed; see FB case 12120 for details
  ###   KW now are multiple lines
  ###   example:
  ###   KW   Kringle {ECO:0000256|SAAS:SAAS00144621};
  ###   KW   Protease {ECO:0000256|RuleBase:RU004260,
  ###   KW   ECO:0000256|SAAS:SAAS00144640};
  ###   KW   Reference proteome {ECO:0000313|Proteomes:UP000000437};
  ###   KW   Serine protease {ECO:0000256|RuleBase:RU004260,
  ###   KW   ECO:0000256|SAAS:SAAS00144624};
  ###   KW   Transferase {ECO:0000313|EMBL:ADW08347.1}.

  ###   The parsing code concatenates all the KW lines and then split with ';' and then strip anything from '{'

  if (/^KW\s+(.*\.)$/)  {
        $kwMultLinesConcatenated = $kwMultLinesConcatenated . $1;

        @kws = split(/;/, $kwMultLinesConcatenated);
        while ($kw = shift @kws) {
	       if ($kw =~ m/(.*)\{/) {
           	 $kwToPrint = $1;
           	 $kwToPrint =~ s/^\s+//;     ## strip leading space
           	 $kwToPrint =~ s/\s+$//;     ## strip trailing space
           	 $kwToPrint =~ s/\.$//;     ## strip trailing period (.)
	         foreach $gene (@gene_array) {
	            print KEYWD "$gene|$kwToPrint\n";
	         }
	       } elsif ($kw =~ m/(.*)$/) {     ## some records still with old format that does not contain braces
           	 $kwToPrint = $1;
           	 $kwToPrint =~ s/^\s+//;     ## strip leading space
           	 $kwToPrint =~ s/\s+$//;     ## strip trailing space
           	 $kwToPrint =~ s/\.$//;     ## strip trailing period (.)
	         foreach $gene (@gene_array) {
	            print KEYWD "$gene|$kwToPrint\n";
	         }
	       }
        }
        $kwMultLinesConcatenated = " ";
        undef @kws;
        undef $kwToPrint;
  } elsif (/^KW\s+(.*)/) {
        $kwMultLinesConcatenated = $kwMultLinesConcatenated . $1;
  }

  if(/\/\//) {

    if ($cc) { push (@cc, $cc);}        # add in the last cc line
    @sp_ac = split(' ',$sp_ac_list);
    $prm_ac = shift @sp_ac;
    chop $prm_ac;
    while ($sp_ac = shift @sp_ac){
      chop $sp_ac;
      print ACC "$prm_ac|$sp_ac\n" if ($sp_ac ne '' );
    }

    if (@cc) {

      foreach $gene (@gene_array) {
	  # '|' is in use in the comments field, thus use '$' to be delimiter
	  print COMMT "$gene\$$prm_ac\$".join("<br>",@cc)."\n";
      }
    }

   foreach $gene (@gene_array) {

	print DBLINK "$gene|UniProtKB|$prm_ac|$len\n";
    }

    if (length($ecnumber)>0){
	  foreach $gene (@gene_array) {
	    print DBLINK "$gene|EC|$ecnumber|\n";
	  }
    }

    # reinitiate the variables for loop
    $cc=''; $kw='';  $prm_ac = '';
    $gene=''; $single_gene = ''; @gene_array = (); $embl = ''; $ecnumberLine = '';
    @cc = ();  @dr = ();  @ecLineSplit = ();
    @sp_ac=(); $sp_ac=''; $sp_ac_list=''; $ecnumber = ''; $acc_num = '';
    $first = 1;$one = 1;
  }
}

exit;
