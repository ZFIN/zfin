
begin work ; 

delete from elsevier_statistics where es_incoming_ip in (select ei_ip from excluded_ip ) ; 

rollback work ; 
--commit work ; 
