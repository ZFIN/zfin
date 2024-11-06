#!/bin/tcsh
# 
# Vega is weird because we do a bunch of pre-processing away from
# the BLAST db generation pipeline.  See RENO pipeline for more info.
# we just cp the file from a location on embryonix, if we're on genomix,
# and from the same location but without the scp of we're on embryonix/helix.

echo "==| start of the vega_withdrawn load |=="
@TARGET_PATH@/ZFIN/zfin_vegaWithdrawn/cpzfin_vegaWithdrawn.sh


echo "==| Finish processzfin_vegaWithdrawn.sh |=="

exit
