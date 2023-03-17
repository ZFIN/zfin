CREATE OR REPLACE FUNCTION fluorescent_marker() RETURNS trigger AS
$$
BEGIN
    PERFORM create_color_info();
    RETURN NEW;
END
$$ LANGUAGE PLPGSQL;

DROP TRIGGER IF EXISTS fluorescent_marker_trigger on FLUORESCENT_MARKER;
CREATE TRIGGER fluorescent_marker_trigger
    AFTER INSERT
    ON FLUORESCENT_MARKER
    FOR EACH ROW
EXECUTE PROCEDURE fluorescent_marker();
