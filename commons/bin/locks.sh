
INFORMIXDIR="/private/apps/Informix/informix"
export INFORMIXDIR

INFORMIXSERVER="waldo"
export INFORMIXSERVER

/private/apps/Informix/informix/bin/dbaccess sysmaster 2> /dev/null <<+
unload to /tmp/locks.out
select a.dbsname, a.tabname, a.rowidlk, a.type, a.owner, b.username
from syslocks a, syssessions b
where a.owner = b.sid
order by a.owner;
+
cat /tmp/locks.out | awk -F"|" '{

t_llevel=$3
if (length($3)<7)
   { t_llevel="ROW" }

if ($3 == "0")
   { t_llevel="TABLE" }

if (substr($3,length($3)-1,2) == "00")
   { t_llevel="PAGE" }

if (length($3)>6)
   { t_llevel="IDX KEY" }

t_ltype=$4
if ($4 == "B")
   { t_ltype="BYTES" }

if ($4 == "S")
   { t_ltype="SHRED" }

if ($4 == "X")
   { t_ltype="EXCLV" }

if ($4 == "I")
   { t_ltype="INTNT" }

if ($4 == "U")
   { t_ltype="UPDAT" }

if ($4 == "IX")
   { t_ltype="INT-EX" }

if ($4 == "IS")
   { t_ltype="INT-SHR" }

if ($4 == "SIX")
   { t_ltype="SHR-INT-EX" }


print($5"|"$6"|"$1"|"$2"|"t_llevel"|"t_ltype"|")

}' > /tmp/locks1.out

/private/apps/Informix/informix/bin/dbaccess sysmaster 2> /dev/null <<+
create temp table t_lloocckk (session integer, usernm char(15), db_name 
char(15)
, tb_name char(20),level char(10), type char(15));
load from /tmp/locks1.out insert into t_lloocckk;
unload to /tmp/locks.out
select session, usernm, db_name, tb_name, level, type, count(*)
from t_lloocckk
group by 1,2,3,4,5,6
order by 7 desc;
drop table t_lloocckk;
+

echo "Lock usage report Total locks in use" `/private/apps/Informix/informix/bin/onstat -k | tail -2 | grep active | awk '{print $1, "out of", $3}'`
echo "----------------------------------------------------------------------------"
echo "SESSION  OWNER      DATABASE   TABLE            LEVEL     TYPE      #LOCKS"
echo "----------------------------------------------------------------------------"

cat /tmp/locks.out | awk -F"|" '{

printf("%8d %-10s %-10s %-18s %-7s %-10s %-6d\n",$1,$2,$3,$4,$5,$6,$7)

}'

rm /tmp/locks.out
rm /tmp/locks1.out
