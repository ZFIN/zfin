#! /bin/tcsh
# generate_nom_can_fasta.sh

# find and rank zfin objects in need of renaming.
# generated longest protenin fasta file to blast 
# 


set bin_pth="/private/apps/wublast";
set current="/research/zblastdb/db/Current";
set timestamp="`date +%Y%m%d`"
set here="`pwd`"
set quote='"'

dbaccess $DBNAME select_nomemclature_candidates.sql;
cat nomenclature_candidate_pp.unl | cut -f 4,5 -d \| | sort -u > ! keys.txt

# zero out the fasta file
cat /dev/null >! accession.pp

foreach key (`cat keys.txt`)
	echo $key
	switch ($key)
		#case "Polypeptide|GenPept":
		#	$xdget -p  $current/sptr_zf `grep $key nomenclature_candidate_pp.unl | cut -f3 -d \|` >> accession.pp
		#breaksw
		case "Polypeptide|RefSeq":
			$bin_pth/xdget -p  $current/refseq_zf_aa `grep $key nomenclature_candidate_pp.unl | cut -f3 -d \|` >> accession.pp
		breaksw
		case "Polypeptide|SWISS-PROT":
			$bin_pth/xdget -p  $current/sptr_zf `grep $key nomenclature_candidate_pp.unl | cut -f3 -d \|` >> accession.pp
		breaksw
		case "cDNA|Genbank":
			$bin_pth/xdget -n  $current/gbk_gb_zf `grep $key nomenclature_candidate_nt.unl | cut -f3 -d \|` >> accession.nt
		breaksw
		case "cDNA|RefSeq":
			$bin_pth/xdget -n  $current/refseq_zf_rna `grep $key nomenclature_candidate_nt.unl | cut -f3 -d \|` >> accession.nt
		breaksw
		case "other|VEGA":
			$bin_pth/xdget -nd  $current/vega_zfin `grep $key nomenclature_candidate_nt.unl | cut -f3 -d \|` >> accession.nt
		breaksw
		default:
			echo "$key not selected"
		breaksw
	endsw
end # foreach

echo "on EMBRYONIX blast the nomenclature set against Human & mouse & zebrafish proteins"

ssh embryonix "cd $here;nice +10 $bin_pth/blastp $quote$current/sptr_hs $current/sptr_ms $current/sptr_zf$quote accession.pp -E e-50 >! 
UniProt_$timestamp.out"

/research/zcentral/Commons/bin/parse-blast-reno.r UniProt_$timestamp.out "UniProt_$timestamp" 
