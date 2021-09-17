CREATE OR REPLACE FUNCTION add_users() RETURNS void AS $$
DECLARE
  userlist text[] := ARRAY['rtaylor', 'staylor','ryanm','pkalita','pm','kschaper','cmpich','solr','informix','zfishweb','zfinner'];
  username text;
BEGIN
  FOREACH username IN ARRAY userlist
  LOOP
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = username) THEN
      EXECUTE format('CREATE user %s WITH superuser', username);
    END IF;
  END LOOP;
END;
$$ LANGUAGE 'plpgsql';

