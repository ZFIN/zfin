--liquibase formatted sql
--changeset sierra:addDefinition

alter table pub_tracking_status
 add (pts_definition varchar(255) not null constraint pts_definition)
;

update pub_tracking_Status
  set pts_definition = 'This means the pub was closed with literally no data, no records in record_attribution, no indexing.'
 where pts_status = 'CLOSED'
and pts_qualifier = 'no data';

update pub_tracking_Status
  set pts_definition = 'This means the pub was closed without a PDF.  There should be no data associated with this pub.'
 where pts_status = 'CLOSED'
and pts_qualifier = 'no PDF';



update pub_tracking_Status
  set pts_definition = 'This means a curated has reviewed it and there is just not enough useful data to bother with.'
 where pts_status = 'CLOSED'
and pts_qualifier = 'archived';

update pub_tracking_Status
  set pts_definition = 'This means data has been added to the paper.'
 where pts_status = 'CLOSED'
and pts_qualifier = 'curated';

update pub_tracking_Status
  set pts_definition = 'This means that there should bo data and that this paper is slotted to be deleted from ZFIN.'
 where pts_status = 'CLOSED'
and pts_qualifier = 'not a zebrafish paper';

update pub_tracking_Status
  set pts_definition = 'This paper has been processed by student worker and is ready for indexing.  Records with this status also define the PET date for this paper.'
 where pts_status = 'NEW';

update pub_tracking_Status
  set pts_definition = 'This paper is currently being indexed for genes, markers, clones, fish, genotypes and features'
 where pts_status = 'INDEXING';

update pub_tracking_Status
  set pts_definition = 'This paper is currently waiting for nomenclature review and/or support.'
 where pts_status = 'WAIT'
and pts_qualifier = 'Waiting for Nomenclature';

update pub_tracking_Status
  set pts_definition = 'This paper is currently waiting for ontology terms, review and/or support.'
 where pts_status = 'WAIT'
and pts_qualifier = 'Waiting for Ontology';


update pub_tracking_Status
  set pts_definition = 'This status is used to get the paper off your desk and make it someone elses problem.  Most curators have one thing they do when a paper is set like this.'
 where pts_status = 'WAIT'
and pts_qualifier = 'Waiting for Curator Review';

update pub_tracking_Status
  set pts_definition = 'This paper is waiting for a bug fix or feature enhancement.'
 where pts_status = 'WAIT'
and pts_qualifier = 'Waiting for Software Fix';

update pub_tracking_Status
  set pts_definition = ''
 where pts_status = ''
and pts_qualifier = '';

update pub_tracking_Status
  set pts_definition = 'Paper has been processed by student, ready for indexer to take.'
 where pts_status = 'READY_FOR_INDEXING';

update pub_tracking_Status
  set pts_definition = 'Pub has been claimed by Indexer.'
 where pts_status = 'INDEXING';

update pub_tracking_Status
  set pts_definition = 'Pub is ready for a curator to process, can come from indexer or student.'
 where pts_status = 'READY_FOR_CURATION';

update pub_tracking_Status
  set pts_definition = 'Pub has been claimed by curator and curator is actively reading and entering data.  Waiting statuses should be used to show blocks.'
 where pts_status = 'CURATING';
