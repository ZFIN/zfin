#! /bin/tcsh
# generate_nom_can_fasta.sh

# find and rank zfin objects in need of renaming.
# generated longest protien fasta file to blast
#


set bin_pth="/private/apps/wublast";
# path from $WEBHOST_BLAST_DATABASE_PATH/Current
set current="<!--|WEBHOST_BLAST_DATABASE_PATH|-->/Current/";
set timestamp="`date +%Y%m%d`"
set here="`pwd`"


#dbaccess -a $DBNAME select_nomemclature_candidates.sql


cat nomenclature_candidate_pp.unl | cut -f 4,5 -d \| | sort -u > ! keys.txt

# zero out the fasta file
cat /dev/null >! accession.pp

foreach key (`cat keys.txt`)
	echo $key
	switch ($key)
		case "Polypeptide|Ensembl":
			${bin_pth}/xdget -p  ${current}/ensemblProt_zf `grep $key nomenclature_candidate_pp.unl | \
			cut -f3 -d \|` >> accession.pp
		breaksw
		case "Polypeptide|GenPept":
			${bin_pth}/xdget -p  ${current}/refseq_zf_aa `grep $key nomenclature_candidate_pp.unl | \
			cut -f3 -d \|` >> accession.pp
		breaksw
		case "Polypeptide|RefSeq":
			${bin_pth}/xdget -p  ${current}/refseq_zf_aa `grep $key nomenclature_candidate_pp.unl | cut -f3 -d \|` >> accession.pp
		breaksw
		case "Polypeptide|UniProtKB":
			${bin_pth}/xdget -p  ${current}/sptr_zf `grep $key nomenclature_candidate_pp.unl | cut -f3 -d \|` >> accession.pp
		breaksw
		case "cDNA|Genbank":
			${bin_pth}/xdget -n  ${current}/gbk_zf_rna `grep $key nomenclature_candidate_nt.unl | cut -f3 -d \|` >> accession.nt
		breaksw
		case "cDNA|RefSeq":
			${bin_pth}/xdget -n  ${current}/refseq_zf_rna `grep $key nomenclature_candidate_nt.unl | cut -f3 -d \|` >> accession.nt
		breaksw
		case "other|VEGA":
			${bin_pth}/xdget -nd  ${current}/vega_zfin `grep $key nomenclature_candidate_nt.unl | cut -f3 -d \|` >> accession.nt
		breaksw
		default:
			echo "$key not selected"
		breaksw
	endsw
end # foreach

echo "On $HOST blast the nomenclature set against Human & mouse & zebrafish proteins"
echo ""
nice +10 ${bin_pth}/blastp "${current}/sptr_hs ${current}/sptr_ms ${current}/sptr_zf ${current}/refseq_zf_aa ${current}/publishedProtein ${current}/unreleasedProtein" accession.pp -E e-50 >! Protein_${timestamp}.out

sleep 3
echo ""
echo "Parse the Blast output for loading into ReNo"
echo ""

# $SOURCEROOT is the right thing to use here but we are too lame for that 

../../../../../commons/bin/parse-blast-reno.r Protein_${timestamp}.out "Protein_${timestamp}"
