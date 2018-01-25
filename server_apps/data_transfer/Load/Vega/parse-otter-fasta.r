#!/private/bin/rebol -sq
rebol [

example: {rebol -sq parse-otter-fasta.r transcripts_for_tom.fa genes_for_tom.txt}
file: %parse-otter-fasta.r

purpose: {combine several files into one blastable fasta file
          with a meaningful defline.
INPUT:
o   Kerstin sends a file with vega genes, their clones & ottdargs
o   ZFIN has a httpable file for zdbids with known ottdargs & symbols
o   I generate a file with the sequence for all transcripts in Otter.

INPUT FORMAT(S):
    current Kerstins file: si_name tab OTTDARG tab clone_name newline
    current ZFIN file:     ZFIN_ID tab Symbol tab OTTDAR[G|T] newline
    otter transcript:      > ottdart | ottdarg # sequence # newline

OUTPUT:
    a blastable nt fasta file containing the sequences associated with
    the ottdargs included in Kerstin's file where the defline is
    augmented with associated zfin data where possible.
    2005-Feb, this process has been modified to only pass transcripts
    not currently in zfin (vega.txt) to reduce the load on the
    redundancy pipeline

OUTPUT FORMAT:
    the defline has the pipe terminated format
    http://blast.wustl.edu/doc/FAQ-Indexing.html
    o   tpe|   third party embl
    o   ottdart|  sangers unique transcript identifier
    o   ottdarg|  sangers gene_id may be multiple
    o   [zdb_id zfin_symbol|| or " |"  (zfin data optional space mandatory)
    o   vega gene name clone_LG
    o   clone name(s) ottdart found in

>tpe|ottdart|ottdarg|[zdbid symbol]|siname clone ... length bp
sequence

elements of the defline are to be used as linkouts in the blast-report

the resulting fasta file is blastn  like so:
/opt/ab-blast/blastn /research/zblastdb/db/Current/zfin_cdna \
/research/zusers/tomc/VEGA/2004-Dec/transcripts_for_tom.nt \
E=1.0e-50 E2=0.1 W=11 V=5 B=5 -prune -noseqs -kap -cpus=2 m=1 n=-3 x=6 gapx=25 q=7 r=2 gapL=1.37 gapK=.711 gapH=1.31 \
>! /research/zusers/tomc/VEGA/2004-Dec/vega_vs_cdna.out

nice +10 /opt/ab-blast/blastn /research/zblastdb/db/Current/zfin_cdna transcripts_for_tom.nt E=1.0e-50 >! vega_vs_cdna.out
}
]

;;;call/wait "make_clnacc_lg.sh"

if any [none?  system/script/args
    empty? system/script/args
][
    print system/script/header/example
    quit
]
args: parse system/script/args none
trnscrpt: to file! trim first args                ; fasta file
vega:  parse read to file! trim second args none  ; gene file from Kerstin


zfin: parse read http://zfin.org/data_transfer/Downloads/vega_transcript.txt "^-"
;;; transcrips now all exist and vega.txt, now without warrning also has ottdarP & 'no_translation'.
c_lg: parse read %clnacc_lg.txt none

;;; zero out existing files if they exist
write %vega_transcript.nt copy ""
write %vega_reno.nt copy ""
write %vega_zfin.nt copy ""
write %ottdarT_ottdarG_ottdarP.unl copy ""

fasta: parse/all read trnscrpt ">" ;;; may want to make this '>' a more robust delimiter
while [not tail? fasta][
    while[empty? fa: first fasta][fasta: next fasta]
    seq: copy ""
; 2009 Nov
;>OTTDART00000003965 name BUSM1-173A8.1-001 version 1 transcript_type protein_coding
; protein_id OTTDARP00000003616 gene_id OTTDARG00000003778 name BUSM1-173A8.1 version 1
; gene_type protein_coding gene_status NOVEL clone AL606751.5
    parse/all  fa [        copy ottdart to " " " "
        "name "            copy t-name  to " " " " ;(lowercase t-name) wait for now
        "version "         copy t-vers integer! " "
        "transcript_type " copy ttyp    to " " " "
        "protein_id "      copy ottdarp to " gene_id" " "
        (all[here: find ottdarp " " change :here "_"]) ;;;no translation -> no_translation
        "gene_id "         copy ottdarg to " " " "
        "name "            copy g-name  to " " " "
        "version "         copy g-vers integer! " "
        "gene_type "       copy gtyp    to " " " "
        "gene_status "     copy gstat   to " " " "; value not always present
        "clone "           copy cln-acc to newline newline
        copy seq to end
    ]
    ;print[ottdart ttyp ottdatp ottdarg gtyp gstat cln-acc]
    defline: find fa newline
    seq-len: length? trim/all/with copy seq newline

    either not none? known: find/case zfin ottdart
    [zdb: copy first back back back known g-name: copy first back known][zdb: copy ""]

    either find cln-acc ","
    [   lg: copy []
        foreach c parse cln-acc none[
            insert/only tail lg select c_lg  c
        ]
        lg: unique lg
        if 1 < length? lg [ print ["WARNING! " ottdart " on " lg ]]
        lg: form lg
    ][  lg: either lg: select c_lg cln-acc [lg][""]]


    write/append %vega_transcript.nt record: rejoin[
        ">tpe|" ottdart '| ottdarg '| zdb " " g-name '| ottdarp " " t-name " "
         join "LG_" lg  " " cln-acc " Danio rerio " gtyp " " gstat ", " seq-len " bp" newline
        seq
    ]
    ;;print copy/part record find record newline

    ;;; snag a copy of the ones novel to zfin for reno
    either empty? zdb
        [write/append %vega_reno.nt record]
        [write/append %vega_zfin.nt record]
    write/append %ottdarT_ottdarG_ottdarP.unl rejoin[ ottdart "|" ottdarg "|" either "no translation" = ottdarp[""][ottdarp] "|^/"]
    fasta: next fasta
    zdb: copy ""
]

;write/append %vega_transcript.nt  f

;print "hit 'q' to quit"
;halt
