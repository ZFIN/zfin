//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.apache.log4j.Logger
import groovy.sql.Sql
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.properties.ZfinProperties

Logger log = Logger.getLogger(getClass());
AntBuilder ab = new AntBuilder()
ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")
def db = Sql.newInstance(ZfinPropertiesEnum.JDBC_URL.value(), ZfinPropertiesEnum.JDBC_DRIVER.value())

//Start by copying files into targetroot from the data cache

String destination = ZfinPropertiesEnum.TARGETROOT.value() + "/server_apps/data_transfer/Downloads/GFF3"
String downloadsDir = ZfinPropertiesEnum.TARGETROOT.value() + "/server_apps/data_transfer/Downloads"
String dataCachePath = "/research/zprodmore/gff3"

ab.copy(file: "$dataCachePath/drerio_ensembl_pg.unl", todir: destination)
ab.copy(file: "$dataCachePath/ensembl_contig.gff3", todir: destination)


//Init the output file
BufferedWriter out = new File("$dataCachePath/E_drerio_constant.gff3").newWriter()
//"$destination/drerio_ensembl.unl"



db.getConnection().autoCommit = false



println "loading gff3 from file"
db.execute("delete from gff3 where substring(gff_source from 1 for 8) = 'Ensembl_';")
String copySql = """copy gff3 from '$destination/drerio_ensembl_pg.unl' with delimiter '|';"""
db.execute(copySql)

out.println("##gff-version 3")

println "unload gff3 from database"

db.eachRow("""
select
  gff_seqname,
  gff_source ,
  gff_feature,gff_start,gff_end,gff_score,gff_strand,gff_frame,
  'ID='       || gff_ID      ||
  ';Name='    || case gff_Name when NULL then '' else gff_Name end ||
  ';Parent='  || case
                 when gff_Parent IS NULL AND gff_feature != 'gene' AND gff_Name IS NOT NULL then gff_Name
                 when gff_Parent IS NULL AND gff_feature = 'gene' then ''
                 else gff_Parent end || ';Alias='   || gff_ID
from  gff3
where substring(gff_source from 1 for 8) = 'Ensembl_'
and gff_feature not in ('mRNA','transcript')
order by 1,4,3;
""") { row ->
    def record = []
    row.getMetaData().columnCount.times { record << row[it] }
    out.println(record.join("\t"))
}

out.flush()
out.close()

db.commit()

"chmod g+w $dataCachePath/E_drerio_constant.gff3".execute()
ab.copy(file:"$dataCachePath/E_drerio_constant.gff3", todir: downloadsDir)

println "Finished running ensembl load SQL"



System.exit(0)

