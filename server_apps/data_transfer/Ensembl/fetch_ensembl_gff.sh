#! /usr/bin/bash

# fetch_ensembl_gff.sh

# the Makefile has 'run_gff' and 'run_gff_force' targets
# the force target removes downloaded files
# forcing them to be refreshed.

### For testing, or incase of problems: delete downloaded data files
### use commandline option '-f' to force fetching new files

# where we put these files these days
gffdir="/research/zprodmore/gff3"

# the Ensembl GTF file (with embedded version number)
GTF="Danio_rerio.Zv*.gtf.gz"
FASTA="Danio_rerio.Zv*.dna.toplevel.fa.gz"

echo "`pwd`/$0"

while getopts f o
  do case "$o" in
    f)  	echo "force rm ${GTF}"; rm -f ${GTF} ;;
    [?])	print >&2 "Usage: $0 [-f {to force reloading}]" ;
  esac
done

######################################################################
echo ""
echo " fetch the 'TopLevel' Ensembl FASTA file into ${gffdir}"
echo " if & only if it is newer than the one we have (takes a Long time)"
echo ""
cd ${gffdir}
prior="`ls -lt ${FASTA} | head -1`"
wget -q --timestamping  "ftp://ftp.ensembl.org/pub/current_fasta/danio_rerio/dna/${FASTA}"
geterr=$?
post="`ls -lt ${FASTA} | head -1`"

if [[ -f ${FASTA} &&  ${geterr} -eq 0 ]]; then
	#-a "$prior" != "$post" ] ; then
	FASTA="`ls -t ${FASTA} | head -1`"
	# preserve the "timestamped" download to avoid (slooow) re-fetching
	gunzip -c  ${FASTA} > ${FASTA%.gz}
	chmod g+w ${FASTA%.gz}
	echo "produce 'ensembl_contig.gff3'"
	nawk 'BEGIN{print "##gff-version   3"}\
		/^>/{split($3,a,":");print "##sequence-region\t" substr($1,2) "\t" a[4] "\t" a[5]}' \
		${FASTA%.gz} > ensembl_contig.gff3
fi
cd -
#####################################################################

echo "fetch the Ensembl GTF file iff it is newer than the one we have"
echo ""
# get a fix on the oldest file that matches the pattern
prior="`ls -ltr ${GTF} | head -1`"
wget -q --timestamping  "ftp://ftp.ensembl.org/pub/current_gtf/danio_rerio/${GTF}"
geterr=$?
# get a fix on the newest file that matches the pattern
post="`ls -lt ${GTF} | head -1`"

#if the oldest matches the newest, then  they are the same

#echo "Is ${post} newer than ${prior}"

if [[ ( "${post}" != "${prior}" ) && ${geterr} -eq 0  ]] ; then
	echo "delete all but the most recent downloaded ${GTF}"
	find . -name "${GTF}" ! -name `ls -1t ${GTF}|head -1` -exec rm -f {} \;

	# get the current version number
	ver=`ls ${GTF}`
	ver=${ver%.gtf.gz}
	ver=${ver#Danio_rerio\.}
	echo "Most recent is: $ver"
	echo ""

	# Ensembl has looser names than we want i.e. "sym  (x of y)"
	# I have no illusion this will be sufficent to handle all future versions.
	#	unzip
	#	squeeze whitespace out of names
	#	don't chop trailing semicolon here, gtf2gff3 re-adds them
	#	convert to gff3
	#	avoid comment lines
	#	add "Ensembl_" to source
	#	skip "ID=" & ommit the "<name>=" from the name=value atributes
	#	chop trailing semicolon
	#	create empty items for columns are not incluced
	#	convert to pipe terminated ifx load file
	#	put in long term storage

	# note: can use ~/bin/unlcollen.awk to get max length of columns

	zcat ${GTF}|nawk 'BEGIN{FS="\"";OFS="\""}/\(/{gsub(" ","",$8);print}{print}'|\
	./gtf2gff3.pl|\
	nawk -v OFS='|' '/^[^#]/{$2="Ensembl_"$2;att=substr(substr($9,1,length($9)-1),4);\
		for(i=0;i<4;i++){if(match(att,/;[^=]*=/)){sub(/;[^=]*=/,"|",att)}\
			else{att=att"|"} }$9=att;print}' > $gffdir/drerio_ensembl.${ver}.unl

	# ensure accessible
	chmod g+w $gffdir/drerio_ensembl.${ver}.unl

	# symlink as current version
	unlink  $gffdir/drerio_ensembl.unl
	ln -s  $gffdir/drerio_ensembl.${ver}.unl $gffdir/drerio_ensembl.unl

	echo "$gffdir/drerio_ensembl.${ver}.unl -->  $gffdir/drerio_ensembl.unl"
fi



