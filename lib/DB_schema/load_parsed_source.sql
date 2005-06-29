begin work ;

execute procedure set_session_params();

create temp table tmp_parsed_source (pub_zdb_id varchar(50),
					old_source varchar(255),
					source_abbrev varchar(255),
					volume varchar(50),
					pages varchar(25),
					prepub_status varchar(30))
with no log ;

alter table journal
  modify (jrnl_abbrev varchar(255));


create unique index journal_alternate_key_index
  on journal (jrnl_name)
  using btree in idxdbs3 ;

alter table journal
  add constraint unique (jrnl_name)
  constraint journal_alternate_key ;


insert into zdb_active_source
  select jrnl_zdb_id 
    from journal
  where not exists (select 'x'
			from zdb_Active_source
			where zactvs_zdb_id = jrnl_zdb_id);


load from source_mdfd
  insert into tmp_parsed_source ;

create unique index tmp_pubzdbid_pk
  on tmp_parsed_source(pub_zdb_id)
  using btree in idxdbs1;


update statistics high for table tmp_parsed_source;

update publication
  set pub_volume = (select volume 
			from tmp_parsed_source
			where zdb_id = pub_zdb_id)
  where source is not null
  and source != '' ;

update publication
  set pub_pages = (select pages
			from tmp_parsed_source
			where zdb_id = pub_zdb_id)
  where source is not null
  and source != '' ;

update publication
  set pub_jrnl_abbrev = (select source_abbrev 
			from tmp_parsed_source
			where zdb_id = pub_zdb_id)
  where source is not null;


create temp table tmp_new_journals (t_jrnl_abbrev varchar(255));

insert into tmp_new_journals
  select distinct source_abbrev
	from tmp_parsed_source 	
	where not exists (select 'x' 
			    from journal
				where jrnl_abbrev = source_abbrev);

set constraints all deferred ;

insert into journal (jrnl_zdb_id, jrnl_is_nice, jrnl_name, jrnl_abbrev)
	select get_id('JRNL'), 'f', t_jrnl_abbrev, t_jrnl_abbrev
	  from tmp_new_journals;


!echo journal abbrev null
select * from tmp_new_journals
  where t_jrnl_abbrev is null ;


insert into zdb_active_source
  select jrnl_zdb_id from journal where jrnl_zdb_id not in 
	(select zactvs_zdb_id from zdb_active_source) ;

set constraints all immediate ;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id 
			   from journal
			   where jrnl_abbrev = pub_jrnl_abbrev)
  where pub_jrnl_zdb_id is null ;



!echo count of null journal ids

select source, zdb_id from publication
where pub_jrnl_zdb_id is null ;

select jrnl_name, jrnl_abbrev
  from journal 
  where jrnl_abbrev like '%In Fish Development and Genetics - the Zebrafish and Medaka Mo%' ;

update journal
  set jrnl_abbrev = 'AACE/WebNet97 conference on WWW, Internet, and Intranet, Toronto, Canada, Nov. 1,1997'
  where jrnl_name = '
 conference on WWW, Internet, and Intranet, Toronto, Canada, Nov.' ;

update journal 
  set jrnl_name = 'AACE/WebNet97 conference on WWW, Internet, and Intranet, Toronto, Canada, Nov. 1,1997'
  where jrnl_name = '
 conference on WWW, Internet, and Intranet, Toronto, Canada, Nov.' ;

update journal
  set jrnl_abbrev = 'Avail. Univ. Microfilms Int., Order No. DA8625444 From Diss. Abstr. Int. B 1987'
  where jrnl_name =  ' From Diss. Abstr. Int. B';

update journal 
  set jrnl_name = 'Avail. Univ. Microfilms Int., Order No. DA8625444 From Diss. Abstr. Int. B 1987'
  where jrnl_name = ' From Diss. Abstr. Int. B';

update journal
  set jrnl_abbrev = 'Avail. Univ. Microfilms Int., Order No. BA8622499 From: Diss. Abstr. Int. B 1987'
  where jrnl_name =  ' From: Diss. Abstr. Int. B';

update journal 
  set jrnl_name = 'Avail. Univ. Microfilms Int., Order No. BA8622499 From: Diss. Abstr. Int. B 1987'
  where jrnl_name = ' From: Diss. Abstr. Int. B';

delete from journal
  where jrnl_name = '"BMC Ear Nose and Throat Disorders"' ;

update journal
  set jrnl_abbrev = 'In Effects of oxygen-prebleached softwood sulfate pulp chlorine bleaching wastewaters on reproduction in zebra fish. Inst. Vatten- Luftvaardsforsk.'
  where jrnl_abbrev = ',';

update journal
  set jrnl_name = 'In Effects of oxygen-prebleached softwood sulfate pulp chlorine bleaching wastewaters on reproduction in zebra fish. Inst. Vatten- Luftvaardsforsk.'
  where jrnl_name = ',';

set constraints all deferred ;

insert into journal (jrnl_zdb_id,
			jrnl_name,
			jrnl_abbrev,
			jrnl_is_nice)
  values (get_id('JRNL'),
		'Inst. Vatten- Luftvaardsforsk.',
		'Inst. Vatten- Luftvaardsforsk.',
		'f');

insert into zdb_active_source
  select jrnl_zdb_id
   from journal
   where jrnl_zdb_id not in (Select zactvs_zdb_id
				from zdb_Active_source);
set constraints all immediate;

update publication
  set pub_jrnl_zdb_id = (select jrnl_zdb_id
			   from journal
			   where jrnl_abbrev = 
				'Inst. Vatten- Luftvaardsforsk.')
  where source = 'Inst. Vatten- Luftvaardsforsk., [Publ.] B, IVL B-659, 25 pp' ;

update journal
  set jrnl_name = "In Scanning Electron Microscopy/1977/II. O. Johari and R.P. Becker, eds., IIT Research Inst., Chicago, Il."
  where jrnl_abbrev = "/II. O. Johari and R.P. Becker, eds., IIT Research Inst., Chicago, Il. pp." ;

update journal
  set jrnl_abbrev = "In Scanning Electron Microscopy/1977/II. O. Johari and R.P. Becker, eds., IIT Research Inst., Chicago, Il."
  where jrnl_abbrev = "/II. O. Johari and R.P. Becker, eds., IIT Research Inst., Chicago, Il. pp." ;


update journal
  set jrnl_name = "In Scanning Electron Microscopy/1977/II. O. Johari and R.P. Becker, eds., IIT Research Inst., Chicago, Il."
  where jrnl_abbrev = "/II. O. Johari and R.P. Becker, eds., IIT Research Inst., Chicago, Il. pp." ;

update journal
  set jrnl_abbrev = "In Scanning Electron Microscopy/1978/II. R.P. Becker, and O. Johari, eds., Chicago, IL: II T Research Inst."
  where jrnl_abbrev = "/II. R.P. Becker, and O. Johari, eds., Chicago, IL: II T Research Inst. pp." ;

update journal
  set jrnl_name = "In Scanning Electron Microscopy/1978/II. R.P. Becker, and O. Johari, eds., Chicago, IL: II T Research Inst."
  where jrnl_abbrev = "/II. R.P. Becker, and O. Johari, eds., Chicago, IL: II T Research Inst. pp." ;

update journal
  set jrnl_name = "Cambridge, The Company of Biologists, Ltd."
  where jrnl_name ="Cambridge, The Company of Biologists, Ltd.  pp.";

update journal
  set jrnl_name = "6th Asian Fisheries Forum Book of Abstracts"
  where jrnl_name ="th Asian Fisheries Forum Book of Abstracts p";

update journal
  set jrnl_abbrev = "6th Asian Fisheries Forum Book of Abstracts"
  where jrnl_abbrev ="th Asian Fisheries Forum Book of Abstracts p";

update journal
  set jrnl_abbrev = "13th European J. Muscle Res. Cell Motil."
  where jrnl_abbrev ="th European J. Muscle Res. Cell Motil.";

update journal
  set jrnl_name = "13th European J. Muscle Res. Cell Motil."
  where jrnl_name ="th European J. Muscle Res. Cell Motil.";

update journal
  set jrnl_name ="3rd ed., Minneapolis, MN: Burgess Publishing Co. pp."
  where jrnl_name ="rd ed., Minneapolis, MN: Burgess Publishing Co. pp.";

update journal
  set jrnl_abbrev ="AACE/WebNet97 conference on WWW, Internet, and Intranet, Toronto, Canada, Nov. 1,1997"
  where jrnl_abbrev =" conference on WWW, Internet, and Intranet, Toronto, Canada, Nov.";

update journal
  set jrnl_abbrev ="Abstr. Pap. Am. Chem. Soc. 190"
  where jrnl_abbrev ="Abstr. Pap. Am. Chem. Soc.";

update journal
  set jrnl_abbrev ="Abstr. Pap. Am. Chem. Soc. 190"
  where jrnl_abbrev ="Abstr. Pap. Am. Chem. Soc.";

update publication
  set pub_volume = "Akvarieforeningen Karpens Tidskrift. 1"
  where source = 'Akvarieforeningen Karpens Tidskrift.' ;

update publication
  set pub_pages = "842, 250"
  where source = 'Ann. N.Y. Acad. Sci. 842, 250 p' ;

update publication
  set pub_pages = "742 P"
  where source = 'Amsterdam; New York: Elsevier' ;

update journal
  set jrnl_abbrev ="Am. Ms. Novitiates 150"
  where jrnl_abbrev ="Am. Ms. Novitiates";

update publication
  set (pub_pages,pub_volume) = ('p. 531','2001: Book of Abstracts, World Aquacul. Soc.')
  where source = 'Aquaculture 2001: Book of Abstracts, World Aquacul. Soc. p. 531';

update publication
  set pub_pages = '357-358'
  where source = '3rd ed., Minneapolis, MN: Burgess Publishing Co. 357-358' ;

update publication
  set (pub_pages,pub_volume) = (null,null)
  where source = 'Aquarium Fish. Deposited Doc., VIHITI 2149-82, 7 Avail. VIHITI' ;

update publication
  set (pub_pages,pub_volume) = (null,null)
  where source = 'Aquarium Fish. Deposited Doc., VIHITI 2149-82, 7 Avail. VIHITI' ;

update journal
  set (jrnl_name, jrnl_abbrev) = ('Aquarium Fish. Deposited Doc., VIHITI 2149-82, 7 Avail. VIHITI','Aquarium Fish. Deposited Doc., VIHITI 2149-82, 7 Avail. VIHITI')
  where jrnl_name = 'Aquarium Fish. Deposited Doc., VIHITI';

update publication
  set pub_pages = 'p. 193'
  where source = '6th Asian Fisheries Forum Book of Abstracts p 193' ;

update publication
  set pub_pages = 'p. 277'
  where source = '6th Asian Fisheries Forum Book of Abstracts p 277' ;

update publication
  set pub_pages = 'p. 85'
  where source = '6th Asian Fisheries Forum Book of Abstracts p 85' ;

update publication
  set pub_pages = '30 P'
  where source = 'A.M.Thesis, Washington University 30 p' ;

update journal
  set jrnl_is_nice = 'f'
  where jrnl_is_nice is null ;

update publication
  set pub_can_show_images = (select jrnl_is_nice
				from journal
				where jrnl_zdb_id = pub_jrnl_zdb_id)
  where pub_can_show_images = 'f' ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.biomedcentral.com/bmcbiol/",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "BMC Biology" ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://dev.biologists.org/",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Development" ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www3.interscience.wiley.com/cgi-bin/jhome/38417",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Dev. Dyn." ;


insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.ijdb.ehu.es/",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Int. J. Dev. Biol." ;

update journal 
  set jrnl_name = 'International Journal of Developmental Biology'
  where jrnl_abbrev = 'Int. J. Dev. Biol.' ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://biology.plosjournals.org/",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "PLoS Biology" ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.elsevier.com/wps/find/journaldescription.cws_home/622816/description#description",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Dev. Biol." ;

update journal 
  set jrnl_name = 'Developmental Biology'
  where jrnl_abbrev = 'Dev. Biol' ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.elsevier.com/wps/find/journaldescription.cws_home/628039/description#description",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Gene Expr. Patterns" ;

update journal 
  set jrnl_name = 'Gene Expression Patterns'
  where jrnl_abbrev = 'Gene Expr. Patterns' ;


insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.genesdev.org/",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Genes & Dev." ;


update journal 
  set jrnl_name = 'Genes & Development'
  where jrnl_abbrev = 'Genes & Dev.' ;



insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.jbc.org/",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "J. Biol. Chemistry" ;

update journal 
  set jrnl_name = 'Journal of Biological Chemistry'
  where jrnl_abbrev = 'J. Biol. Chemistry' ;


insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.nature.com/nature/index.html",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Nature" ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.sciencemag.org/",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Science" ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.springeronline.com/sgw/cda/frontpage/0,11855,4-0-70-1072932-detailsPage%253Djournal%257Cdescription%257Cdescription,00.html?referer=www.springeronline.com%2Fjournal%2F00427%2Fabout",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Dev. Genes. Evol." ;

update journal 
  set jrnl_name = 'Development Genes and Evolution'
  where jrnl_abbrev = 'Dev. Genes. Evol.' ;


insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www3.interscience.wiley.com/cgi-bin/jhome/68503812",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Genesis" ;

insert into source_url (srcurl_source_zdb_id, 
			srcurl_url, 
			srcurl_purpose,
			srcurl_display_text)
  select jrnl_zdb_id,
	"http://www.elsevier.com/wps/find/journaldescription.cws_home/506090/description#description",
	"information",
	"Journal website"
    from journal 
    where jrnl_abbrev = "Mech. Dev." ;


update journal 
  set jrnl_name = 'Mechanisms of Development'
  where jrnl_abbrev = 'Mech. Dev.' ;

update publication
  set status = 'e-pub ahead of print'
  where exists (select 'x'
		  from tmp_parsed_source
		  where prepub_status is not null
		  and pub_zdb_id = zdb_id);

--rollback work;
commit work ;