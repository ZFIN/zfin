SELECT DISTINCT zdb_id, fig_label, fig_zdb_id
FROM image
INNER JOIN figure ON img_fig_zdb_id = fig_zdb_id
INNER JOIN publication ON fig_source_zdb_id = zdb_id
WHERE pub_can_show_images = 't'
AND (fig_caption = '' OR fig_caption IS NULL)
AND jtype IN ('Journal', 'Other', 'Review')
ORDER BY zdb_id, fig_label;
