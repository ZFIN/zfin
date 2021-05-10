#! /bin/tcsh
# generate_nom_can_fasta.sh

# find and rank zfin objects in need of renaming.
# generated longest protien fasta file to blast
#


set bin_pth="/opt/ab-blast";
# path from $WEBHOST_BLAST_DATABASE_PATH/Current
set current="/research/zblastfiles/zmore/blastRegeneration/Current/";
set timestamp="`date +%Y%m%d`"
set here="`pwd`"


#dbaccess -a $DBNAME select_nomemclature_candidates.sql


cat sanger_mutant_nomenclature_candidate_pp.unl | cut -f 4,5 -d \| | sort -u > ! keys.txt

# zero out the fasta file
cat /dev/null >! accession.pp

foreach key (`cat keys.txt`)
	echo $key
	switch ($key)
		case "Polypeptide|Ensembl":
			${bin_pth}/xdget -p  ${current}/ensemblProt_zf `grep $key sanger_mutant_nomenclature_candidate_pp.unl | \
			cut -f3 -d \|` >> accession.pp
		breaksw
		default:
			echo "$key not selected"
		breaksw
	endsw
end # foreach

echo "On $HOST blast the nomenclature set against Human & mouse & zebrafish proteins"
echo ""
nice ${bin_pth}/blastp "${current}/sptr_hs ${current}/sptr_ms ${current}/sptr_zf ${current}/refseq_zf_aa ${current}/publishedProtein ${current}/unreleasedProtein" accession.pp -E -filter=xnu+seg >! Protein_${timestamp}.out

sleep 3
echo ""
echo "Parse the Blast output for loading into ReNo"
echo ""


$SOURCEROOT/commons/bin/parse-blast-reno.r SangerNomenclature_${timestamp}.out "SangerNomenclature_${timestamp}"

/bin/sed 's/tr|/sp|/g' SangerNomenclature_${timestamp} > SangerNomenclature_${timestamp}.tr

#reno parser won't accept tr| (trembl) as a prefix for blast output, change these to sp| (swiss prot)
/bin/rm SangerNomenclature_${timestamp}.out
/bin/mv SangerNomenclature_${timestamp}.tr SangerNomenclature_${timestamp}.out 
