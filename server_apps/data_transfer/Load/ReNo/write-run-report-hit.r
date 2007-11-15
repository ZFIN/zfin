#! /private/bin/rebol -sqw
rebol[]

terminate: func[
    "puts a terminator after each element in a series"
    blk [block!] /with t [string!]  /local result
][
    result: make block! 2 * length? blk
    parse blk [
        any [copy element skip
            (insert tail result element
             insert tail result either with[t]["|"]
            )
         ]
    ]
    rejoin result
]


clip-normal-float: func [
    "clips the 'close to zero' values to not closer than min normalized value per IEEE 754"
    denorm [string!] /local norm
][
    ;;;lo-norm: 2.225073858507202e-307  ;;;hi-norm: 1.7976931348623157e308
    ;;; use  0  >=  norm - epislon  ?
    either 0 >= norm: to decimal! denorm[0.0][maximum 2.2250e-307 norm]
]


run: load to file! system/script/args

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; run data

replace/all run/details newline "\^/"
replace/all run/details "|" "\|"

write %run.unl join terminate reduce[
    run/name
    "embryonix"    ;;; needs to be parameterized
    run/datetime
    run/program
    run/database
    run/query-type
    run/target-type
    trim run/details
] newline

write %report.unl ""
write %hit.unl ""

count: 0

foreach rpt run/reports [
    if here: find/last rpt/acc "." [clear here]
    if all[run/query-type = "protein" here: find/last rpt/locus_acc "."] [
        clear here
    ]
    xit-code: copy find/last rpt/detail " "
    replace/all rpt/detail newline "\^/"
    replace/all rpt/defline "|" "\|"
    write/append %report.unl join terminate reduce [
        rpt/acc         ;;; BC146618
        rpt/acc_db       ;;; "gb"
        either any["" = rpt/acc_type none? rpt/acc_type]
            [either rpt/acc_db = "tpe" ["transcript"][run/query-type]]
            [rpt/acc_type]
        rpt/locus_acc    ;;; "zgc:158136"
        rpt/acc_len      ;;; 3498
        rpt/alt_id       ;;; ""
        trim/lines xit-code
        rpt/defline      ;;; {gi|148921504|gb|BC146618.1|zgc:158136 Danio rerio cDNA clone MGC:158136 IMAGE:6970042, complete cds (3498 letters; record 1)}
        rpt/detail

    ] newline
    foreach hit rpt/hit[
        ;;; some hits are none!
        either hit/acc
            [clear find/last hit/acc "."]
            [hit/acc: "NONE"]
        replace/all hit/alignment newline "\^/"
        replace/all hit/alignment "|" "\|"
        replace/all hit/defline   "|" "\|"
        if rpt/acc <> hit/acc[ ;; skip self hits
        write/append %hit.unl join terminate reduce[
            rpt/acc       ;;; : BC146618
            hit/order     ;;; : 0
            hit/acc       ;;; : "BC116508"
            hit/acc_db    ;;; : "gb"
            either any[ "" =  hit/acc_type none? hit/acc_type]
                [either hit/acc_db = "tpe" ["transcript"][run/target-type]]
                [hit/acc_type]
            hit/defline   ;;; : {BC116508.1|BC116508 Danio rerio zgc     ;;;; :136342, mRNA (cDNA clone MGC     ;;;; :136342 IMAGE     ;;;; :8128252), complete cds Length = 2014}
            hit/species   ;;; : "Danio rerio"
            hit/length    ;;; : 0
            hit/score     ;;; : "5874"
            hit/bits      ;;; : "887.4"
            (clip-normal-float hit/expect)    ;;; : "0."
            hit/probtype  ;;; : ""
            (clip-normal-float hit/prob)      ;;; : "0."
            hit/identites-num     ;;;; : "1188"
            hit/identites-denom   ;;;; : "1197"
            hit/positives-num     ;;;; : "1188"
            hit/positives-denom   ;;;; : "1197"
            hit/strand            ;;;; : ""
            hit/alignment         ;;;; : {(99%), Strand = Plus / Plus ...
        ] newline
        ]
        ;;;print [count: 1 + count  tab  (clip-normal-float hit/expect)  tab  hit/prob]
   ]
]





comment {
    Epsilon: The ANSI C standard defines the value of epsilon as the difference between 1.0 and
    the least representable value greater than 1.0, that is, b**(1-p),
    where b is the radix (2) and p is the number of base b digits in the number.

    REAL        single          double          quad
    epsilon =   0.11920929E-06  0.22204460E-15  0.19259299E-33
    }
