#!/bin/sh

case "$1" in
'cp')
sed -e "s/^/command=\"\/bin\/$1 \$\{SSH_ORIGINAL_COMMAND\/\\\\\\\\\\\\\\\\\/\}\" &/" $1.pub > $1.pub_wcommand ;
;;
'xdget')
sed -e "s/^/command=\"\/common\/zfin\/wublast\/$1 \$\{SSH_ORIGINAL_COMMAND\/\\\\\\\\\\\\\\\\\/\}\" &/" $1.pub > $1.pub_wcommand ;
;;
'xdformat')
sed -e "s/^/command=\"\/common\/zfin\/wublast\/$1 \$\{SSH_ORIGINAL_COMMAND\/\\\\\\\\\\\\\\\\\/\}\" &/" $1.pub > $1.pub_wcommand ;
;;
*)
sed -e "s/^/command=\"qrsh -now n \/common\/zfin\/wublast\/$1 \$\{SSH_ORIGINAL_COMMAND\/\\\\\\\\\\\\\\\\\/\}\" &/" $1.pub > $1.pub_wcommand ;
;;
esac

mv $1.pub_wcommand $1.pub
