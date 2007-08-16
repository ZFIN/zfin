
begin work ; 

delete from excluded_ip ; 


-- Start - from awstats.zfin.org.conf
insert into excluded_ip values( '128.223.56.139' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.81' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.99' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.147' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.154' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.157' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.160' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.161' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.163' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.175' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.186' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.189' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.191' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.199' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.196' ,'internal' ) ; 

insert into excluded_ip values( '128.223.57.215' ,'internal' ) ; 
insert into excluded_ip values( '128.223.57.221' ,'internal' ) ; 
insert into excluded_ip values( '128.223.57.230' ,'internal' ) ; 
insert into excluded_ip values( '128.223.57.231' ,'internal' ) ; 
insert into excluded_ip values( '128.223.57.253' ,'internal' ) ; 
-- End - static Ips from awstats.zfin.org.conf


-- Start - dynamic Ips from our stats and awstats.zfin.org.conf
insert into excluded_ip values( '128.223.56.198' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.179' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.195' ,'internal' ) ; 
insert into excluded_ip values( '128.223.56.155' ,'internal' ) ; 
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

