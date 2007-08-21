select first 20 count(*) dacount,es_incoming_ip,es_http_user_agent from elsevier_statistics  group by es_incoming_ip,es_http_user_agent order by dacount desc ;
