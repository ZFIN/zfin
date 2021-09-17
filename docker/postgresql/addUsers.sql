CREATE OR REPLACE FUNCTION add_users() RETURNS void AS $$
DECLARE
  userlist text[] := ARRAY['tomcat','gradle','rtaylor','staylor','ryanm','cmpich','xshao','informix','zfishweb','zfinner'];
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

SELECT add_users();
DROP FUNCTION IF EXISTS add_users();
