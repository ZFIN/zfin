#! /private/bin/rebol -sqw
rebol[
    Title:   "Entrez Fetch"
    Author:  "Tom Conlin"
    Date:    [11-Mar-2005 7-Mar-2007]
    version: 0.0.2
    website: https://eutils.ncbi.nlm.nih.gov/entrez/query/static/efetchseq_help.html
    Purpose: "retrive NCBI data from the commend line"
    comment {version 0.0.2 better handles unix pipelines}
]
comment {
 they may have added a new parameter ...
report=[docsum, brief, abstract, citation, medline, asn.1, mlasn1, uilist, sgml, gen]
}

hlp: {EFETCH sends a list of accessions to Entrez an outputs their records.
 Use flags to change
 		the Database queried
 		the query Type
 		return format Mode.

 Defaults are  Nucleotide GenBank records as text.
 Usage:
 	efetch.r  [FLAGS] -i  <input-filename> -o <output-filename>
 	efetch.r  [FLAGS] <STDIN>
 	efetch.r  [FLAGS] "accession[,accession[...]]"

 FLAGS: -d  [nucleotide | protein | genome | gene | popset | snp | sequences | uni* | omim]
        -t  [gb | gp | fasta | est | native | ft  gbc gpc gss acc seqid ...]
        -m  [text | html | xml | asn.1]
        -i  <input-filename>
        -o  <output-filename>
each flag must have its own hyphen and argument but order does not matter.

Eamples:
   % cat acc.lst | efetch.r > genbank_record.txt
   % efetch.r -i acc.lst -o acc.gb
   % efetch.r AL732544

where acc.lst can be space,tab,comma,newline separated accessions (with or without quotes)
for more info on allowed/rational parameter combinations see:
https://eutils.ncbi.nlm.nih.gov/entrez/query/static/efetchseq_help.html
}

;;;
;;; an object to contain methods incase I ever want to to use in another application
;;; (context would sit in own file and be included here)
;;;

efetch-ctx: make object![

;;; longer lists are timing out before they return to give a little more headroom
;;; I am increasing the network timeout for web transactions
system/schemes/http/timeout: 0:09:00

;;; where we are going
url:       https://eutils.ncbi.nlm.nih.gov/entrez/eutils/
;;; tags on our hats and scarves
constants: {efetch.fcgi?tool=zfin&email=zfinadmn%40zfin.org}

;;; these two would only come into play if I did an esearch/epost first
WebEnv:    copy ""
query_key: copy ""
;;; these may be used in conjunction with the WebEnv and query_key to retrieve data in controled batches
retstart:  0     ;sequential number of the first id retrieved - default=0 which will retrieve the first
retmax:    5000  ;number of items retrieved

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

dbs:       [gene genome nucleotide protein popset snp sequences unigene unists]
retmode:   [xml html text asn.1]
rettype: [;rettype 	scope 	Descriptions
    native
    ;(full record) 	all 	Default format for viewing sequences.
    fasta
    ;sequence only 	FASTA view of a sequence.
    gb
    ;nucleotide sequence only 	GenBank view for sequences, constructed sequences will be shown as contigs (by pointing to its parts).
    gbc
    ;nucleotide sequence only 	INSDSeq structured flat file.
    gbwithparts
    ;nucleotide sequence only 	GenBank view for sequences, the sequence will always be shown.
    est
    ;dbEST sequence only 	EST Report.
    gss
    ;dbGSS sequence only 	GSS Report
    gp
    ;protein sequence only 	GenPept view
    gpc
    ;protein sequence only 	INSDSeq structured flat file.
    seqid
    ;sequence only 	To convert list of gis into list of seqids.
    acc
    ;sequence only 	To convert list of gis into list of accessions
    chr 	;dbSNP only 	SNP Chromosome Report.
    flt 	;dbSNP only 	SNP Flat File report.
    rsr 	;dbSNP only 	SNP RS Cluster report.
    brief 	;dbSNP only 	SNP ID list.
    docset 	;dbSNP only 	SNP RS summary.

    ft  ;sequence only  Feature Table report

]
;;; ---------------------------------------------------------------------------


strand:     1    ;what strand of DNA to show (1=plus or 2=minus)
seq_start:  0    ;show sequence starting from this base number
seq_stop:   0    ;show sequence ending on this base number
complexity: 1    ;gi is often a part of a biological blob, containing other gi
comment{        * 0 - get the whole blob
                * 1 - get the bioseq for gi of interest (default in Entrez)
                * 2 - get the minimal bioseq-set containing the gi of interest
                * 3 - get the minimal nuc-prot containing the gi of interest
                * 4 - get the minimal pub-set containing the gi of interest
    ID is  comma seperated values
    NCBI sequence number (GI), accession, accession.version, fasta, GeneID, genome ID, seqid
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;  function that does the fetching
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

set 'efetch func[
     id   "GI, accession[.version], fasta, GeneID, genome_ID, seqid {<= 500 csv}" [string!]
    /db   "dbs"  db-arg    [word!] "default nucleotide protein genome gene  popset snp sequences"
    /mode "mode"  mode-arg [word!] "default text html xml asn.1"
    /type "type"  type-arg [word!] "default gb gp fasta est acc seqid native ft"
    /local result query-string
][
    query-string: to url! rejoin[
        url constants
        "&retmode=" either all[mode find retmode get 'mode-arg][:mode-arg]["text"]
        "&rettype=" either all[type find rettype get 'type-arg][:type-arg]["gb"]
        "&db="      either all[db   find dbs get 'db-arg][:db-arg]["nucleotide"]
        ;"&retstart=0"
        "&retmax=" retmax
        "&id=" id
    ]
    ;;;print query-string
    either error? err: try [result: read query-string]
	[ ;;; extend timeout by a couple of minutes and retry once
    	 system/schemes/http/timeout: 00:02:00 + system/schemes/http/timeout
   	 either error? err: try [result: read query-string]
		[probe disarm err
		 print [newline query-string newline]
		]
		[result]
	]
	[result]
  ];~ efetch


]; ~efetch-ctx
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; the rest is just dealing with the enviroment,
;;; and getting the arguments into the  fetch function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



either system/options/args
    [args: copy system/options/args
     if "efetch.r" = first args[remove args];;; allow to be called  manualy
     if empty? args [print hlp quit]
    ][
     if all[system/script/args args: copy system/script/args][
        if "efetch.r" = first args[remove args];;;
     ]
     if empty? args [print hlp quit]
    ]

refinements: ["-d"  "/db"   "-m"  "/mode"   "-t"  "/type" ] ;; "-w" "/webenv"
refinement: copy ""
refargs: copy ""
id:      copy ""
ids:     copy []
infile:  none
outfile: none

limit: 300 ; max number of IDs to send at once (limit is on the server side only accepting GETs)

;;; get output filename if exists
if all[args arg: find args "-o"][
    change-dir system/options/path
    outfile: to file! trim select args "-o"
    loop 2 [arg: remove arg]
]

;;; build up any function refinements found in the argument string
forskip refinements 2[
    if all[args arg: find args first refinements] [
        insert tail refinement select refinements first arg
        insert tail refargs join " '"  select args first arg
        loop 2 [arg: remove arg]
    ]
]

;;; get data from input file, if it exists otherwise try stdin
either all[args arg: find args "-i"]
    [change-dir system/options/path
     either exists? (infile: to file! trim select args "-i")
        [id: parse read infile none]
        [print ["Cannot open " infile ]quit]
        loop 2 [arg: remove arg]
    ][;;; id list is either what is left in args, or on stdin or absent
        if any[none? args empty? id: copy args][
            set-modes system/ports/input [binary: false]
            if error? err: try[id: copy system/ports/input ][
                probe disarm err
                halt
            ]
        ]
    ]

;;;
;;; turn the input data into csv list with not more than <limit> ids per list
;;; as ncbi requests

while[not tail? id][
    i: copy/part id limit
    if 1 < length? first id[
        i: next i
        forskip i 2[insert i ","]
    ]
    insert/only tail ids replace/all to string! head i " " ""
    id: skip id limit
]

;;; print usage statement
if empty? ids [print hlp quit]

;;;if verbose [print length? ids probe first ids
;;]

if outfile [write outfile ""]
while [not tail? ids][
    either outfile
        [  write/append outfile  do rejoin compose['efetch(refinement) "(first ids)" (refargs)]]
        [  print                 do rejoin compose['efetch(refinement) "(first ids)" (refargs)]
    ]
    ids: next ids
]



comment {
more options than you can shake a stick at ...

full list of DBs  http://www.ncbi.nlm.nih.gov/entrez/eutils/einfo.fcgi?
curl "http://www.ncbi.nlm.nih.gov/entrez/eutils/einfo.fcgi?" | cut -f 2- -d \> | cut -f1 -d \<

pubmed
protein
nucleotide
nuccore
nucgss
nucest
structure
genome
books
cancerchromosomes
cdd
domains
gene
genomeprj
gensat
geo
gds
homologene
journals
mesh
ncbisearch
nlmcatalog
omia
omim
pmc
popset
probe
pcassay
pccompound
pcsubstance
snp
taxonomy
toolkit
unigene
unists
}
