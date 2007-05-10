unload to golist.txt
select distinct mrkr_abbrev,
	mrkr_zdb_id, 
	recattrib_source_zdb_id,
	jtype,
	pub_completion_date, 
	case 
	 when pub_completion_date is not null
	 	then 'Closed'
	 when pub_completion_date is null
		and cur_closed_date is not null
		then 'GO Curated'
	 else 'Open'
	 end
from marker, 
	record_Attribution, 
	curation,
	publication
where mrkr_Zdb_id = recattrib_data_zdb_id
 and cur_pub_zdb_id = zdb_id
 and recattrib_source_zdb_id = cur_pub_zdb_id
 and recattrib_source_zdb_id = zdb_id
and jtype not in ('Curation', 'Unpublished')
and mrkr_type = 'GENE'
and cur_topic ='GO'
union
select distinct mrkr_abbrev,
	mrkr_zdb_id, 
	recattrib_source_zdb_id,
	jtype,
	pub_completion_date, 
	case 
	 when pub_completion_date is not null
	 	then 'Closed'
	 when pub_completion_date is null
		and cur_closed_date is not null
		then 'GO Curated'
	 else 'Open'
	 end
from marker, 
	record_Attribution, 
	curation,
	publication,
        genotype_feature,
        feature_marker_relationship
where mrkr_Zdb_id = fmrel_mrkr_zdb_id
and recattrib_data_zdb_id=genofeat_zdb_id
and genofeat_feature_zdb_id=fmrel_ftr_zdb_id
 and cur_pub_zdb_id = zdb_id
 and recattrib_source_zdb_id = cur_pub_zdb_id
 and recattrib_source_zdb_id = zdb_id
and jtype not in ('Curation', 'Unpublished')
and mrkr_type = 'GENE'
and cur_topic ='GO'
union
select distinct mrkr_abbrev,
	mrkr_zdb_id, 
	recattrib_source_zdb_id,
	jtype,
	pub_completion_date, 
	case 
	 when pub_completion_date is not null
	 	then 'Closed'
	 else 'Open'
	 end
from marker, 
	record_Attribution,
	publication
where mrkr_Zdb_id = recattrib_data_zdb_id
 and recattrib_source_zdb_id = zdb_id
and jtype not in ('Curation', 'Unpublished')
and mrkr_type = 'GENE'
and not exists (Select 'x'
		  from curation
		  where cur_topic = 'GO'
		  and cur_pub_zdb_id = zdb_id)
union
select distinct mrkr_abbrev,
	mrkr_zdb_id, 
	recattrib_source_zdb_id,
	jtype,
	pub_completion_date, 
	case 
	 when pub_completion_date is not null
	 	then 'Closed'
	 else 'Open'
	 end
from marker, 
	record_Attribution,
	publication,
        genotype_feature,
        feature_marker_relationship
where mrkr_Zdb_id = fmrel_mrkr_zdb_id
and recattrib_data_zdb_id=genofeat_zdb_id
and genofeat_feature_zdb_id=fmrel_ftr_zdb_id
 and recattrib_source_zdb_id = zdb_id
and jtype not in ('Curation', 'Unpublished')
and mrkr_type = 'GENE'
and not exists (Select 'x'
		  from curation
		  where cur_topic = 'GO'
		  and cur_pub_zdb_id = zdb_id)

union
select distinct mrkr_abbrev,
	mrkr_zdb_id, 
	recattrib_source_zdb_id,
	jtype,
	pub_completion_date, 
	case 
	 when pub_completion_date is not null
	 	then 'Closed'
	 when pub_completion_date is null
		and cur_closed_date is not null
		then 'GO Curated'
	 else 'Open'
	 end
from marker, 
	record_Attribution, 
	curation,
	publication,
        feature_marker_relationship
where mrkr_Zdb_id = fmrel_mrkr_zdb_id
and recattrib_data_zdb_id=fmrel_ftr_zdb_id
 and cur_pub_zdb_id = zdb_id
 and recattrib_source_zdb_id = cur_pub_zdb_id
 and recattrib_source_zdb_id = zdb_id
and (jtype='Journal' or jtype='Review')
and mrkr_type = 'GENE'
and cur_topic ='GO'
union
select distinct mrkr_abbrev,
	mrkr_zdb_id, 
	recattrib_source_zdb_id,
	jtype,
	pub_completion_date, 
	case 
	 when pub_completion_date is not null
	 	then 'Closed'
	 else 'Open'
	 end
from marker, 
	record_Attribution,
	publication,
        feature_marker_relationship
where mrkr_Zdb_id = fmrel_mrkr_zdb_id
and recattrib_data_zdb_id=fmrel_ftr_zdb_id
 and recattrib_source_zdb_id = zdb_id
and jtype not in ('Curation', 'Unpublished')
and mrkr_type = 'GENE'
and not exists (Select 'x'
		  from curation
		  where cur_topic = 'GO'
		  and cur_pub_zdb_id = zdb_id)

;



