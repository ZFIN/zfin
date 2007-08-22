
begin work ; 

delete from excluded_ip ; 


-- Start - dynamic Ips from our stats and awstats.zfin.org.conf
insert into excluded_ip values( '128.223.56' ,'internal' ) ; 
insert into excluded_ip values( '128.223.57' ,'internal' ) ; 
-- End - dynamic Ips from our stats and awstats.zfin.org.conf


-- Start - external MS ips
insert into excluded_ip values( '65.55.213' ,'external' ) ; 
insert into excluded_ip values( '65.54.188' ,'external' ) ; 
insert into excluded_ip values( '65.55.209' ,'external' ) ; 
-- End - external MS ips

-- Start - external bot IP
insert into excluded_ip values( '38.99.44.102' ,'external' ) ; 
-- End - external bot IP


--rollback work ; 
commit work ; 

