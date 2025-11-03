--liquibase formatted sql
--changeset rtaylor:ZFIN-9986

-- Fix duplicated .jpg extensions in image filenames
update image set img_image = replace(img_image, '.jpg.jpg', '.jpg') where image.img_image like '%.jpg.jpg';

-- Fix specific images with that are actually video files
update image set img_image = 'ZDB-IMAGE-001220-18.mp4' where img_zdb_id = 'ZDB-IMAGE-001220-18';
update image set img_image = 'ZDB-IMAGE-001220-19.mp4' where img_zdb_id = 'ZDB-IMAGE-001220-19';


