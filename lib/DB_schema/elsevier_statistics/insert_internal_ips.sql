
begin work ; 

-- Start - new static IPs
-- Nathan
insert into excluded_ip values( '128.223.56.139' ,'internal' ) ; 
-- End - new static IPs

-- Start - from awstats.zfin.org.conf
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





rollback work ; 
--commit work ; 

