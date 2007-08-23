

select first 5 TODAY-1, count(*) thecount ,es_incoming_ip,es_http_user_agent 
from elsevier_statistics  
where es_date >=  TODAY-1
and es_date <=  TODAY
group by es_incoming_ip,es_http_user_agent  
order by thecount desc ;

