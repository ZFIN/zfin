select p1.accession_no, p1.zdb_id from publication p1
where p1.accession_no in (
    select p2.accession_no from publication p2
    where p2.accession_no is not null
    group by p2.accession_no
    having count(*) > 1
)
order by p1.accession_no;