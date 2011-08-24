-- just in this form to keep things consistant
-- and to avoid changing mechanisms if we decide
-- to do addtional processing/integration in the future

! echo "E_unload_zfin_morpholino.sql -> E_zfin_morpholino.gff3"

! cp E_zfin_morpholino.gff3 <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_morpholino.gff3

-- to be valid the gff3 requires a header
-- ! awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_zfin_morpholino.gff3
