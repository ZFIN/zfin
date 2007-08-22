
--select avg(DAY(es_date)) avgdate , es_incoming_ip,es_http_user_agent 
----select first 3 es_date
--from elsevier_statistics  
--where es_date >=  TODAY-2
--and es_date <=  TODAY-1
--group by es_incoming_ip,es_http_user_agent, avgdate
--order by es_date desc
--order by thecount desc ;
;

select first 5 TODAY-2, count(*) thecount ,es_incoming_ip,es_http_user_agent 
from elsevier_statistics  
where es_date >=  TODAY-2
and es_date <=  TODAY-1
group by es_incoming_ip,es_http_user_agent  
order by thecount desc ;

