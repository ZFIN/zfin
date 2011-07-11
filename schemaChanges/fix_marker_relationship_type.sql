update marker_relationship_type
set mreltype_1_to_2_comments = 'Has Coding Sequence'
where
mreltype_1_to_2_comments = 'has coding sequence'
;

update marker_relationship_type
set mreltype_1_to_2_comments = 'Has Artifact'
where
mreltype_1_to_2_comments = 'has artifact'
;

update marker_relationship_type
set mreltype_1_to_2_comments = 'Is Hybridized by'
where
mreltype_1_to_2_comments = 'Is hybridized by'
;

update marker_relationship_type
set mreltype_1_to_2_comments = 'Is Recognized by'
where
mreltype_1_to_2_comments = 'Is recognized by'
;

update marker_relationship_type
set mreltype_1_to_2_comments = 'Targets'
where
mreltype_1_to_2_comments = 'targets'
;

update marker_relationship_type
set mreltype_1_to_2_comments = 'Has Promoter'
where
mreltype_1_to_2_comments = 'has promoter'
;


UPDATE marker_relationship_type
set mreltype_2_to_1_comments='Is Coding Sequence of'
where
mreltype_2_to_1_comments='is coding sequence of'
;


UPDATE marker_relationship_type
set mreltype_2_to_1_comments='Is Encoded by'
where
mreltype_2_to_1_comments='Is encoded by'
;

UPDATE marker_relationship_type
set mreltype_2_to_1_comments='Is Artifact of'
where
mreltype_2_to_1_comments='is artifact of'
;

UPDATE marker_relationship_type
set mreltype_2_to_1_comments='Is Targeted by'
where
mreltype_2_to_1_comments='is targeted by'
;


UPDATE marker_relationship_type
set mreltype_2_to_1_comments='Is Promoter of'
where
mreltype_2_to_1_comments='is promoter of'
;



