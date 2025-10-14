--liquibase formatted sql
--changeset rtaylor:ZFIN-9962

--
--   Add metadata to marker_annotation_status
--
--
ALTER TABLE marker_annotation_status
    ADD COLUMN mas_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create a trigger function to update the timestamp on row updates:
CREATE OR REPLACE FUNCTION update_mas_updated_at()
    RETURNS TRIGGER AS '
BEGIN
    NEW.mas_updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create the trigger that calls this function before updates:
CREATE TRIGGER trigger_update_mas_updated_at
    BEFORE UPDATE ON marker_annotation_status
        FOR EACH ROW
        EXECUTE FUNCTION update_mas_updated_at();




--
--   Add metadata to marker_assembly
--
--
ALTER TABLE marker_assembly
    ADD COLUMN ma_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create a trigger function to update the timestamp on row updates:
CREATE OR REPLACE FUNCTION update_ma_updated_at()
    RETURNS TRIGGER AS '
BEGIN
    NEW.ma_updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create the trigger that calls this function before updates:
CREATE TRIGGER trigger_update_ma_updated_at
    BEFORE UPDATE ON marker_assembly
        FOR EACH ROW
        EXECUTE FUNCTION update_ma_updated_at();
