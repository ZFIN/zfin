#! /private/bin/rebol -sqw

rebol[]

go-obj: context [
     id: copy ""
     name: copy ""
     namespace: copy []
     def: copy []
     is_a: copy []
     exact_synonym: copy []
     alt_id: copy ""
     xref_analog: copy []
     relationship: copy []
     comment: copy []
     is_obsolete: copy ""
     xref_unknown: copy []
     subset: copy []
     synonym: copy []
     related_synonym: copy []
     narrow_synonym: copy []
     broad_synonym: copy []
     use_term: copy []
     is_transitive: copy ""
]

buffer: make string! 1000000

;;; names are all lowercase with underscore
;;; could have used "anything but colon" but this is more restrictive
;;; that-is   name-chars: complement charset ":"
name-chars: charset [#"a" - #"z" #"_"]

parse read ftp://ftp.geneontology.org/go/ontology/gene_ontology.obo [
    thru "^/^/" ;;; skips thru the first blank line
    some [(goob: make go-obj[]) ;;; term record
        "[Term]" newline
        some [ ;;; name: value pairs
            copy name some name-chars ":"
            copy value to "^/" "^/"
            ( either word? in goob to word! name
                [insert tail get in goob to word! name trim value]
                [print ["Warning: " name  value " not a valid field."]]
            )
       ];;; end some name: value
       (;;; fix up any loose pipes in the def field
       replace/all goob/def "|" "\|"
       ;;; move data from object to a line in the buffer
       foreach field next first goob[
          insert tail buffer join get in goob to word! field "|"
       ]
       insert tail buffer newline
       )
       newline
    ];;; end term rule
];;; end parse rule

write %godefs_parsed.unl buffer
