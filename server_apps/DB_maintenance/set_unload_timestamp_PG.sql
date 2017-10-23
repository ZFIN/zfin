
delete from database_info where di_database_unloaded = '<!--|DB_NAME|-->';
insert into database_info (di_date_unloaded, di_database_unloaded)
 select CURRENT_TIMESTAMP, '<!--|DB_NAME|-->'
   from single;


--DELETE FROM database_info 
--WHERE  di_database_unloaded = '<!--|DB_NAME|-->'; 

--INSERT INTO database_info 
--            (di_date_unloaded, 
--             di_database_unloaded) 
--SELECT Now(), 
--       '<!--|DB_NAME|-->' 

