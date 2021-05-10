--liquibase formatted sql
--changeset xshao:ZFIN-5912

update annual_stats
   set as_type = "Gene expression experiments"
 where as_type = "Gene expression patterns"
   and as_date not in ("2015-01-01 00:00:00", "2016-01-01 00:00:00", "2017-01-01 00:00:00", "2018-01-01 00:00:00");

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(90358, "Expression & Phenotype", "Gene expression experiments", TO_DATE("2015-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(100381, "Expression & Phenotype", "Gene expression experiments", TO_DATE("2016-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(109435, "Expression & Phenotype", "Gene expression experiments", TO_DATE("2017-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(121336, "Expression & Phenotype", "Gene expression experiments", TO_DATE("2018-01-01", "%Y-%m-%d"));

select count(*) from annual_stats where as_type =  "Gene expression experiments";

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("1998-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("1999-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2000-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2001-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2002-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2003-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2004-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2005-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2006-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2007-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2008-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2009-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2010-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2011-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2012-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(0, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2013-01-01", "%Y-%m-%d"));

insert into annual_stats (as_count, as_section, as_type, as_date)
  values(266746, "Expression & Phenotype", "Gene expression patterns", TO_DATE("2014-01-01", "%Y-%m-%d"));

