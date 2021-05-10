SELECT event_object_table,
  trigger_name,
  event_manipulation,
  action_statement,
  action_timing
FROM   information_schema.triggers
WHERE  event_object_table = '@TABLE@'
ORDER  BY event_object_table,
  event_manipulation