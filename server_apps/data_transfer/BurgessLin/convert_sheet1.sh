#! /bin/tcsh
# convert_sheet1.sh
# mind the tab (after \1	)
#   o filters lines begining with la0
#   o delete streaches of spacees
#   o terminate with a tab 
#   o sort by la0#
#   o write output file which can be loaded by informix

grep "^la0" sheet1 | sed 's/  *//g;s/\(.*\)/\1	/' | sort -u >! la_gb_chr_loc.tab

