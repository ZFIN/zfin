#! /private/bin/rebol -sqw
rebol [
	Author: "Tom Conlin" 
	Date: [2003-Sept-20 2003-Dec-02]
]

comment {
dbsource typically has  "DBSOURCE    accession <acc_num>"
but there can be cruft before that on the line 
ex:
DBSOURCE    embl locus BRENG2, accession X68446.1

There can be several valid lines with different valid <acc_num> on each line
ex:
DBSOURCE    embl locus BRWNT1G1, accession X58880.1
            embl locus BRWNT1G2, accession X58881.1
            embl locus BRWNT1G3, accession X58882.1
            embl locus BRWNT1G4, accession X58883.1

There can be valid lines followed by invalid lines.
ex:
DBSOURCE    swissprot: locus HS7C_BRARE, accession Q90473;
            class: standard.
            created: Nov 1, 1997.
            sequence updated: Nov 1, 1997.
            annotation updated: Jul 15, 1999.
            xrefs: gi: 1408566, gi: 1235933
            xrefs (non-sequence databases): HSSPP19120, ZFINZDB-GENE-990415-92,
            PFAMPF00012, PROSITEPS00297, PROSITEPS00329, PROSITEPS01036
            
There can be single lines with no valid accession 
exs:
DBSOURCE    pir: locus WJZFX2;
DBSOURCE    pdb: molecule 1KQX, chain 65, release Jan 8, 2002;
DBSOURCE    pdb: molecule 1KQW, chain 65, release Jan 8, 2002;
DBSOURCE    prf: locus 2124419A;

}

genpept: read %sequences.gp

; so first record is same as the rest (to facilatate parsing)
insert genpept newline 

; the only chars found in accession numbers
acc-char: charset "0123456789_ABCDEFGHIJKLMNOPQRSTUVWXYZ"

token: copy ""   ; tmp var
protein: copy "" ; tmp var
gene: copy "" ; tmp var
buffer: make string! 800,000 ;room for 8,000 rows with 100 chars per row

parse genpept [
some [  (protein: copy "")
        thru "^/LOCUS       "  
        copy token to " " ; some acc-char 
            (insert tail protein join token  ["|"])          
        copy token integer! "aa" ; the length
            (insert tail protein join token ["|"])      
        thru "^/DBSOURCE    "
        copy accessions to "^/KEYWORDS    "
        to "^/FEATURES " thru newline
        copy features to "^/ORIGIN "
        (   parse/all features [
                (mt: false gene: copy [] )
                opt [to {                     /organelle="mitochondrion"} (mt: true)]  
                any [ (token: copy "")
                    thru {                     /}[
                        [{gene="}          copy token to {"}]|
                        [{standard_name="} copy token to {"}]|
                        [{name="}          copy token to {"}]|
                        [{note="synonym: } copy token to {"}]|
                        [to newline]
                    ]                          
                    (   replace/all token "^/" ""   ; delete newlines
                        replace/all token "^-" " "  ; tab to spave
                        replace/all token "  " " "  ; compress spaces
                        replace/all token "-" ""    ; delete hyphens
                        if all[not equal? "" token][
                            token: lowercase token
                            if all[mt not equal? "mt" copy/part token 2 not equal? "cytb" token][
                                insert token "mt"
                                if equal? #"x" pick token 5[remove skip token 4]
                             ]
                            if equal? "or " copy/part token 3 [replace token " " ""]
                            if not find token " " [
                                insert/only tail gene head token
                            ]
                        ]
                    )
                ]
                (   gene: unique gene
                    if 1 < g: length? gene[
                        for i g 2 -1 [insert gene/:i "','"]
                        insert head gene "('"
                        insert tail gene "')"
                    ]     
                    insert tail protein rejoin[ gene "|"]
                )
            ]
            
        )
        (   parse accessions [
                any [thru "accession " copy token some acc-char 
                    (insert tail buffer rejoin[protein token "|^/"])
                ]
            ]
        )
     ]
]

write %prot_len_acc.unl buffer 
write %GenPept.records genpept ;;; to save reloading while debugging

