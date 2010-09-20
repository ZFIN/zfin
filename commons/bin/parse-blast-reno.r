#! /private/bin/rebol -sq
rebol [
        title: "parse BLAST output for ReNo"
        file: %parse-blast-reno.r
        date: [2004-Dec-7 2005-May-16 2006-Feb-15 2007-03-07]
        author: "Tom Conlin"
        arguments: { blastreport run-title}
]

;;;  need info about this blast run.
;;;  it's name, date, program, parameters, database(s)
;;;
;;; parse the first blast report in the run and use the data from there

;;; function to zero pad strings
zpad: func [str [string! number!] n [integer!]][
  head insert/dup str: form str #"0" n - length? str
]
; CCYY-MM-DD hh:mm:ss
;mysql-datetime: rejoin[now/year "-" zpad now/month 2 "-" zpad now/day 2 " " now/time]

run: context[] ;;; a global container for the results

;;; block of species we are interested in
species: ["Danio rerio" "Mus musculus" "Homo sapiens"]

;;; block of month abbreviations
mth: make block! 12
foreach m system/locale/months[insert tail mth copy/part m 3]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;;  some storage container prototypes
;;;

run-obj: context [
    name: copy ""
    host: copy ""
    datetime: copy ""
    program: copy ""
    database: copy ""
    expect: copy ""
    query-type:  copy ""
    target-type: copy ""
    details: make string! 4096
    reports: make block! 5000
]

report-obj: make object! [
        acc: copy ""
        acc_db: copy ""
        locus_acc: copy ""
        acc_len: 0
        alt_id: copy ""
        acc_type: copy ""
        defline: make string! 255
        detail: make string! 65500
        hit: make block! 20
]

hit-obj: make object! [
        order: 0
        acc: copy ""
        acc_db:  copy ""
        acc_type: copy ""
        defline: make string! 255
        species: copy ""
        length: 0
        score: 0
        bits: 0
        expect: "2.0"
        probtype: copy ""
        prob: "0.0"
        identites-num:   0
        identites-denom: 1
        positives-num:   0
        positives-denom: 1
        strand: copy ""
        alignment: make string! 65500
]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; parse sub rules (helper rules)
;;;
ws: charset [#" " #"^-" #"^/"] ;;; whitespace
bs: complement ws              ;;; not whitespace

;;; TODO:
;;; make rules for different patterns of standard deflines
;;; may split out
;deflines: []

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;;  blast report parse rules
;;;

run-rule: [ ;;; get meta data for the run from the first report
    any ws
    here:
    copy token to " "
    (
        run/program: trim token
        ;;; use program to establish default sequence type
        run/query-type: switch run/program [
            "BLASTN"  ["nucleotide"]
            "TBLASTX" ["nucleotide"]
            "BLASTX"  ["protein"]
            "TBLASTN" ["nucleotide"]
            "BLASTP"  ["protein"]
        ]
        run/target-type: switch run/program [
            "BLASTN"  ["nucleotide"]
            "TBLASTX" ["nucleotide"]
            "BLASTX"  ["nucleotide"]
            "TBLASTN" ["protein"]
            "BLASTP"  ["protein"]
        ]
    )
    thru  "^/^/Parameters:^/"
    param:
    copy token to end (run/details: token)
    :param
    copy token to newline (run/expect: trim token)
    thru "^/Statistics:"
    any ws
    thru "Database:" copy token to "Title:"
    (   token: trim  token
        probe token
        foreach pth parse token none[
         insert tail run/database last load trim pth
        ]
    )
    ;;; Thu Mar 17 13:16:47 2005
    thru "^/  Start: " any ws
    copy n 3 bs  any ws ;;; weekday
    copy m 3 bs  any ws (m: index? find mth m);;; month
    copy d integer! any ws ;;; day
    copy t 8 bs  any ws   ;;; time
    copy y integer!       ;;; year
    (run/datetime: rejoin[y "-" zpad m 2 "-" zpad d 2 " " t])

    :here ; return to beginning
]
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

report-rule: [ ;;;
        (   insert tail run/reports report: make report-obj[acc_type: run/query-type]
            ;print system/stats
        )
        thru "^/Query=  "
        copy token to "^/^/"
        (   report/defline: trim/all/with token newline
            while[find report/defline "  "][replace/all report/defline "  " " "]
            replace/all report/defline "\" "\\" ;;; escape backslash
            replace/all report/defline "'" "\'" ;;; escape single quotes
            row: parse/all report/defline "|"

            ;;; skip the gi type integer accessions
            if find/match ["gi" "gim" "bbs" "bbm"] first row [row: skip row 2]
            ;;; step over general
            if find/match ["gnl"] first row [row: next row ]

            either  1 == length? row
            [
                report/acc_db: none
                report/acc: first parse/all first row " "
                report/locus_acc: report/acc
            ][
                report/acc_db:  row/1
                report/acc:     row/2
                report/locus_acc: first parse/all row/3 " "
                if "tpe" = row/1 [ ;;; vega transcript
                    either find/match row/4 "ZDB-GENE"
                    [;;; known to zfin
                        report/alt_id: second parse row/4 none
                    ][;;; from havana
                        report/alt_id: lowercase copy/part row/5 find row/5 " "
                        if find  ["BUSM" "CH21" "CH73" "DKEY" "RP71" "ZFOS" "CH10"] copy/part report/alt_id 4[
                            insert report/alt_id "si:"
                        ]
                    ]
                    report/acc_type: "transcript"

                ]
                {can we tell the sequence type?
                DNA,
                RNA,
                tRNA (transfer RNA),
                rRNA (ribosomal RNA),
                mRNA (messenger RNA),
                uRNA (small nuclear RNA)
                cDNA
                EST
                transcript
                }

            ]
            parse find/last/tail last row "(" [
                copy token integer!(report/acc_len: to integer! trim token)
            ]
            if any[none? report/locus_acc tail? report/locus_acc] [
                report/locus_acc: report/acc
            ]
            ;;print [report/acc report/locus_acc]
        )

        [[;;; target results for this query
        copy token to "Searching...."
;(print "not fatal")
        (report/detail: copy trim token   trim/lines report/detail)
        thru "Score  P(N)      N"
        any ws
         [[  "*** NONE ***"
            (insert tail report/hit make hit-obj[acc: none acc_type: run/target-type])
            to "Parameters:"
         ]
         |
         [some [
            here:
            [   "sp|" | "gb|" | "tpe|" | "ref|" | "emb|" | "gnl|wz|"
                |
                [ bs
                  (print [" !!! NEW DEFLINE !!! ^/" copy/part :here 60 ]) ;;; to find new db codes
                ]
            ]
            thru newline
          ] ;;; ~ some hit teasers

          ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
          ;;; Here there can be
          ;;; "WARNING: ... ^/^/"
          ;;; and
          ;;; "NOTE:  ... ^/^/"
          ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
          to ">" some hit-rule
         ]]

        ]|[to "FATAL:" (print "FATAL!")]]
        copy token to end
        (insert tail report/detail copy trim token)
        any ws
] ;;; ~ report-rule
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
hit-rule: [;here: (print [tab "hit" copy/part :here 40])
        ">"
        (insert tail report/hit hit: make hit-obj[order: length? report/hit])
        [["sp|"  (hit/acc_db:  "sp")] |
         ["gb|"  (hit/acc_db:  "gb")] |
         ["ref|" (hit/acc_db: "ref")] |
         ["tpe|" (hit/acc_db: "tpe")] |
         ["gnl|wz|"  (hit/acc_db:  "wz"  hit/species: "Danio rerio")] |
         [here: "OTTDART" :here (hit/acc_db: "vega" hit/species: "Danio rerio")]
        ]
        copy token to "^/^/"
        some ws
        ;here:
        (;print copy/part :here 80
         replace/all token "^/" ""
         while[find token "  "][replace/all token "  " " "] ;;; collapse spaces
         replace/all token "\" "\\" ;;; escape backslash
         replace/all token "'" "\'" ;;; escape single quotes
         hit/defline:  token
         forall species [
            if find hit/defline first species[
                hit/species: first species
                break
            ]
         ]
         species: head species
         row: parse/all hit/defline "|"
         hit/acc: trim first row
         clear find hit/acc "."
         hit/length: either none? len: find/tail/last last row "Length = "
            [0][to integer! trim/all/with len ","]
        )

        ;thru ":" some ws  ;{Plus Strand HSPs:}  (not in pp records)
        thru "Score = "  copy token integer!     (hit/score: token)
        thru "("         copy token to " bits)"  (hit/bits: token)
        thru "Expect = " copy token to ", " ", " (hit/expect: token)
        ;copy token to " = "  " = "               (hit/probtype: token);P | Sum P(N)
        thru " = "
                         copy token to "^/"      (hit/prob: token)

        thru "Identities = " copy token integer! (hit/identites-num: token)
                        "/"  copy token integer! (hit/identites-denom: token)
        thru "Positives = "  copy token integer! (hit/positives-num: token)
                        "/"  copy token integer! (hit/positives-denom: token)
                        "(" integer! "%)"
        any ws
        opt[ ;(not in pp records)
            "," "Strand = "     copy token to "^/"  (hit/strand: trim token)
        ]
        any ws
        copy token to "^/^/^/"
        (hit/alignment: copy token)
        some ws
];;; ~ hit-rule
;;;
;;; end of parse rules
;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;
;;; use the parse rules on the file given on the command line
;;;

;;; print "reading file,  breaking into records ..."
;;;

fname: first system/options/args
blast-output: join system/script/parent/path fname

blk: parse/all read blast-output "^L"
recycle
;print ["got " length? blk " records ... getting run data from first record"]
run: make run-obj[
    name: second system/options/args
    reports: reports: make block! length? blk
]
parse first blk run-rule
;;;print first blk
;;;probe run

while[not tail? blk][
    if not parse first blk report-rule [
        print ["Failed to parse " fname ]
        print [
            "Got:-------------------"  newline
            run/name newline
            run/host newline
            run/datetime newline
            run/program newline
            run/database newline
            run/details newline
            count: length? run/reports " reports^/"
            run/reports/:count/acc " last report^/"
            hcount: length? run/reports/:count/hit " last report hits^/"
            "last report last hit^/"
        ]
        print "^/^/^/"
        probe last run/reports ;;;/:count/hit/:hcount
        save/all join system/script/parent/path [trim/all second system/options/args ".ctx"] run
        halt
    ]
    remove blk
    ;recycle
]
    ;report: first run/reports
    ;save/all %run.ctx run
    save/all join system/script/parent/path [trim/all second system/options/args ".ctx"] run

;
halt



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
comment {


BLASTN 2.0MP-WashU [23-Mar-2005] [sol8-ultrav9-I32LPF64 2005-03-22T13:57:20]

Copyright (C) 1996-2005 Washington University, Saint Louis, Missouri USA.
All Rights Reserved.

Reference:  Gish, W. (1996-2005) http://blast.wustl.edu

Notice:  this program and its default parameter settings are optimized to find
nearly identical sequences rapidly.  To identify weak protein similarities
encoded in nucleic acid, use BLASTX, TBLASTN or TBLASTX.

Query=  tpe|OTTDART00000021258|OTTDARG00000017634| DKEY-161L11.72|BX681417
    TCR-delta D segment 2  DKEY-161L11.72-001 ig_gene 10 bp
        (10 letters; record 3724)

FATAL:  The query sequence is shorter than the word length, W=11.

EXIT CODE 17

}

