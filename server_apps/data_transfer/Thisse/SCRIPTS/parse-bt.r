rebol[
   title: "parse bt" 
   date: [2001-Aug-2 2001-Nov-27]
   Author: "Tom Conlin"
]
;Stage Modifications
;For the new template the parse script will handle: Thisse stage ranges and ZFIN stages.


do %../Scripts/csv2pipe.r
do %../Scripts/fix-date.r



  ;Parameters: File, Integer
  ;File format required to be | delimited, Index starts at 1.
  ;Function replicates the field at index Integer.
  ;Returns a string;  
  copy-stage: func[ f [file!] int [integer!] /local row line field buffer count record][
     ;prevent null reference of variable, set value to empty string
     record: copy "" 
     buffer: read/lines f
     foreach line buffer[
        row: copy []
        row: parse/all line "|"
        remove field
        count: 1
        foreach field row[
           either (int <> count) 
              [record: rejoin[record field "|"] ]
              [record: rejoin[record field "|" field "|"] ] 
           count: count + 1
        ]
        record: rejoin[record newline]
     ] 
     return record
  ]

   ; change bernards stages to zfin stages
   map-stages: func[ buffer [string!] /local cut paste changes][
      changes: [                          
         "|B|B|"      "|Blastula:Sphere|Blastula:30%-epiboly|"         
         "|G|G|"      "|Gastrula:50%-epiboly|Gastrula:Bud|"            
         "|ES|ES|"     "|Segmentation:1-somite|Segmentation:5-somite|"  
         "|MS|MS|"     "|Segmentation:14-somite|Segmentation:14-somite|"
         "|24h|24h|"   "|Segmentation:20-somite|Pharyngula:Prim-5|"     
         "|36h|36h|"   "|Pharyngula:Prim-15|Pharyngula:Prim-25|"        
         "|48h|48h|"   "|Pharyngula:High-pec|Hatching:Long-pec|" 
      ]
      foreach [cut paste] changes [replace/all buffer cut paste]
      return buffer
   ]


  ;Parameters: File, Integer, String
  ;File format required to be | delimited, Index starts at 1.
  ;Function replaces null values at index Integer with String.
  ;Returns a string;  
  not-null: func[ nn-f [file!] nn-int [integer!] def_val [String!] /local row line field buffer count record ][
     ;prevent null reference of variable, set value to empty string
     record: copy "" 
     buffer: read/lines nn-f
     foreach line buffer[
        row: copy []
        row: parse/all line "|"
        remove field
        count: 1
        foreach field row[
           either (nn-int <> count) 
              [record: rejoin[record field "|"] ]
              [either field <> ""
                  [record: rejoin[record field "|"] ]
                  [record: rejoin[record def_val "|"] ]
              ]
           count: count + 1
        ]
        record: rejoin[record newline]
     ] 
     return record
  ]   
   
   
   ;;;;;;;;;;;;;; 
   ;;; PROBES ;;;
   ;;;;;;;;;;;;;; 
   comment: {
   create temp table probes_tmp (
      1  _KeyValue (10) not null,
      2  cdna_name varchar (80) not null,
      3  gene_zdb_id varchar(15),
      4  gb5p varchar (50),
      5  gb3p varchar (50),
      6  library varchar(80),
      7  digest varchar(20),
      8  vector varchar(80),
      9  pcr amplification varchar(5),
      0  insert_kb float,
      1  cloning_site varchar(20),
      2  polymerase varchar(80),
      3  comments lvarchar,
      4  modified DATETIME YEAR TO DAY)
   }

   buffer: fix-date %probes.csv  if buffer [write %probes.csv buffer] 
  
   row:  copy []
   line: copy ""
   buf:  copy ""
   field:    copy ""
   buffer: read/lines %probes.csv
   write %probes.unl ""
   foreach line buffer [
      remove line
      clear back tail line
      replace/all line "|"  "<PIPE73>"
      replace/all line "hpf"  "h"    
      replace/all line "^k"  "<br>" 
      replace/all line "primed<br>"  "primed "
      replace/all line "<br><br>Query: " "\^/\^/Query: "
      replace/all line {","}  "|"
      replace/all line "| "   "|" 
      replace/all line " |"   "|"
 
      row: parse/all line {|}
      replace row/10 "," "."     ; change decimal point in lg_location to dot

      line: rejoin [row/1 "|" row/2 "|" row/3 "|" row/4 "|" row/5 "|" row/6 "|" row/7 "|" row/8 "|" row/9 "|" row/10 "|" row/11 "|" row/12 "|" row/13 "|" row/14 "|" ]
                replace/all line "<PIPE73>" "\|"

      write/append %probes.unl rejoin [line newline]      
   ]
   



   ;;;;;;;;;;;;;;;;;;
   ;;; EXPRESSION ;;;
   ;;;;;;;;;;;;;;;;;;
   comment{
      expression_txt
      --------------
      _keyExp
      Stage
      Description
      state
      keywords
      modified
   }

   buffer: fix-date %expression.csv
   if buffer [write %expression.csv buffer]
   buffer: csv2pipe %expression.csv
   ;;; trim extranous spaces
   replace/all buffer "| "   "|" 
   replace/all buffer " |"   "|" 
   replace/all buffer "^k"  "<br>"
   replace/all buffer "|1|"  "|t|"
   replace/all buffer "|0|"  "|f|"
   write %expression.unl buffer
   buffer: copy-stage %expression.unl 2
   buffer: map-stages buffer 
   write %expression.unl buffer
   
   def_val: "t"
   buffer: not-null %expression.unl 5 def_val
   write %expression.unl buffer

        
   


   ;;;;;;;;;;;;;;;;
   ;;; KEYWORDS ;;;
   ;;;;;;;;;;;;;;;;
   comment {
      keywords_txt
      ------------
      cDNA name
      Stage
      Keyword
      modified
   }
   
   row:  copy []
   line: copy ""
   buf:  copy ""
   field:    copy []
   buffer: read/lines %expression.unl
   write %keywords.unl ""
   foreach line buffer [
          row: parse/all line {|}
          replace/all row/6 "<br>" "|"

          field: parse/all row/6 {|}
          foreach keyword field [
            line: rejoin [row/1 "|" row/2 "|" row/3 "|" keyword "|" row/7 "|" newline]
            write/append %keywords.unl line
          ]
        ]  
        
        
   ;;;;;;;;;;;;;;;
   ;;; AUTHORS ;;;
   ;;;;;;;;;;;;;;;
   comment {
      authors_csv
      ------------
      EST
      Author
      Modified      
   }
   auth-buffer: fix-date %authors.csv
   if auth-buffer [write %authors.csv auth-buffer]
   auth-buffer: csv2pipe %authors.csv
   ;;; trim extranous spaces
   changes: [
      "| "   "|" 
      " |"   "|" 
      "Fuerthauer"      "Fürthauer"
      "Furthauer"      "Fürthauer"
   ]

   foreach [cut paste] changes [replace/all auth-buffer cut paste]
   
   write %authors.unl auth-buffer

   
   ;;;;;;;;;;;;;
   ;;; IMAGE ;;;
   ;;;;;;;;;;;;;
   comment{
      images_txt
      ----------
      _keyExp
      Image filename
      Stage
      Orientation (view)
      Orientation (direction)
      Specimen
      comments
      modified
   }
   
   buffer: fix-date %images.csv
   if buffer [write %images.csv buffer]
   buffer: csv2pipe %images.csv
   ;;; trim extranous spaces
   replace/all buffer "| "   "|" 
   replace/all buffer " |"   "|" 
   
   write %images.unl buffer
   buffer: copy-stage %images.unl 3
   buffer: map-stages buffer 
   
   

   comment{
   in zfin the acceptable dir/views are ...
      fimgdir_name         fm_direction     
      -------------        ------------     
      anterior to right      anterior to right          
      dorsal to right      dorsal to right 
      anterior to left      anterior to left 
      anterior to top      anterior to top
      dorsal to left             
      not specified         <no direction selected>   
      dorsal to top         dorsal to top          
      oblique               oblique
                            

      fimgview_name        fm_view            
      -------------         ------             
      animal pole            animal pole        
      dorsal               dorsal             
      frontal               frontal            
      parasagittal         parasagittal       
      side view            side view (lateral)            
      ventral               ventral             
      not specified         <none> ??????????          
      sagittal               sagittal
      transverse            transverse         
      vegetal pole         vegetal pole  
     

      fimgprep_name        speciman
      -------------
      live
      LM-section            LM-section
      not specified         <no specimen selected>
      EM-section
      whole-mount            whole-mount


   }
   ;replace/all buffer "|<no view selected>|" "|not specified|"

   
   replace/all buffer "|<no direction selected>|" "|not specified|"      
   replace/all buffer "|<no specimen selected>|"  "|not specified|"
   replace/all buffer "|side view (lateral)|"     "|side view|"
   replace/all buffer "|section|"                 "|LM-section|"
   replace/all buffer ".jpg|"                     "|"
   ;;; trim extranous spaces (again)
   replace/all buffer "| "   "|" 
   replace/all buffer " |"   "|" 
   
   write %images.unl buffer











