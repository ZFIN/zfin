package org.zfin

//these two lines are required by the ant mailer
@GrabConfig(systemClassLoader=true)
@Grab(group='javax.mail', module='mail', version='1.4.4')

@Grab('com.xlson.groovycsv:groovycsv:1.0')


import static com.xlson.groovycsv.CsvParser.parseCsv

import groovy.sql.Sql

//this method is down at the bottom, it sets up the database connection and
//shows how easy it is to load & access java properties
db = getDB()

//this means execute this block within a transaction
db.withTransaction {

    //set the common traits:
    def attribution= "ZDB-PUB-130724-1"
    def feature_type = "TRANSGENIC_INSERTION"
    def lab_zdb_id = "ZDB-LAB-981125-13"
    def lab_prefix_id = 191
    def unknown_zygocity = "ZDB-ZYG-070117-7"

    if ("--explain" in args) {
        db.execute 'set explain on;'
        db.execute 'set explain file to "/tmp/dorsky-explain.out";'
    }
    
    db.execute """
        create temp table dorsky_ids (
            di_feature_abbrev varchar(50),
            di_feature_zdb_id varchar(50),
            di_geno_zdb_id varchar(50),
            di_genofeat_zdb_id varchar(50),
            di_additional_genofeat_zdb_id varchar(50),
            di_fmrel_zdb_id varchar(50)
        ) with no log;
    """

    dorsky_ids = db.dataSet("dorsky_ids")

    dorsky_lines = parseCsv(new File("/research/zunloads/projects/Dorsky/dorsky-lines.csv").text)

    //read the csv entries into a map using the feature_abbrev as the key, so that I can iterate over
    //the query and access the matching line from the csv file
    def map = [:]
    for(csv in dorsky_lines) {
        dorsky_ids.add(
                di_feature_abbrev: csv.feature_abbrev
        )
        map[csv.feature_abbrev] = csv
    }

    //using a temp table to generate ids into.
    db.execute """
        update dorsky_ids
        set
            di_feature_zdb_id = get_id('ALT'),
            di_geno_zdb_id = get_id('GENO'),
            di_genofeat_zdb_id = get_id('GENOFEAT'),
            di_additional_genofeat_zdb_id = get_id('GENOFEAT'),
            di_fmrel_zdb_id = get_id('FMREL');
    """


    //insert all but the additional genofeat into record attribution, insert that later only if it's not blank
    ["di_feature_zdb_id",
     "di_geno_zdb_id",
     "di_genofeat_zdb_id",
     "di_fmrel_zdb_id"].each { column ->
        db.execute("insert into zdb_active_data select ${db.expand column} from dorsky_ids;")
    }

    //this will get used below to only insert additional genofeats when necessary
    def insertSpecificIdIntoZdbActiveData = { column, abbrev ->
        """
            insert into zdb_active_data
            select ${db.expand column} from dorsky_ids
                where di_feature_abbrev = ${abbrev};
        """
    }

    //add record attribution for a specific column of the temp table with protection
    //for records that are already there (zc1066a already exists)
    def upsertRecordAttribution = { column ->
        """
            insert into record_attribution (Recattrib_Data_zdb_id, recattrib_source_zdb_id)
             select ${db.expand column}, "${db.expand attribution}"
              from dorsky_ids
            where not exists (select 'x' from record_attribution
                                  where recattrib_datA_zdb_id = ${db.expand column}
                         and recattrib_source_zdb_id = "${db.expand attribution}");
        """
    }

    //add single values (used to handle the genofeat ids that might get generated but not used)
    def upsertSpecificRecordAttribution = { data ->
        """
            insert into record_attribution (Recattrib_Data_zdb_id, recattrib_source_zdb_id)
             select "${db.expand data}", "${db.expand attribution}"
              from single
            where not exists (select 'x' from record_attribution
                                  where recattrib_datA_zdb_id = "${db.expand data}"
                         and recattrib_source_zdb_id = "${db.expand attribution}");
        """
    }

    def addFeatureTypeSourceRecordAttribution = { feature_zdb_id ->
        """
            insert into record_attribution (Recattrib_Data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
             select "${db.expand feature_zdb_id}", "${db.expand attribution}", "feature type"
              from single
            where not exists (select 'x' from record_attribution
                                  where recattrib_datA_zdb_id = "${db.expand feature_zdb_id}"
                         and recattrib_source_zdb_id = "${db.expand attribution}");
        """
    }

    //Groovy's dataSet feature has a friendly insert syntax, that's about all I'm using it for
    def feature = db.dataSet("feature")
    def genotype = db.dataSet("genotype")
    def int_data_source = db.dataSet("int_data_source")
    def genotype_feature = db.dataSet("genotype_feature")
    def feature_marker_relationship = db.dataSet("feature_marker_relationship")
    def feature_assay = db.dataSet("feature_assay")


    //For each row in the temp table, get the matching line from the csv, do inserts etc.
    //  row.* refers to rows in the temp table
    //  line.* refers to lines within the csv
    db.eachRow("select * from dorsky_ids;") { row ->
        line = map[row.di_feature_abbrev]
        println "handling $line.feature_abbrev"

        def featureZdbId;
        def featureExists = false;
        def existingFeature = db.firstRow(""" select feature_zdb_id from feature where feature_abbrev == ${line.feature_abbrev};""")
        if (existingFeature?.feature_zdb_id) {
           featureZdbId = existingFeature.feature_zdb_id
           featureExists = true;
        } else
           featureZdbId = row.di_feature_zdb_id

        def constructName;
        db.eachRow("select mrkr_name from marker where mrkr_zdb_id = ${line.construct_zdb_id}") { constructName = it.mrkr_name}

        def featureName = constructName + line.feature_abbrev

        if (!featureExists)
            feature.add(
                feature_zdb_id:             featureZdbId,
                feature_abbrev:             line.feature_abbrev,
                feature_name:               featureName,
                feature_line_number:        line.feature_line_number,
                feature_type:               feature_type,
                feature_lab_prefix_id:      lab_prefix_id
            )

        if (!featureExists) {
            feature_marker_relationship.add(
                fmrel_zdb_id:               row.di_fmrel_zdb_id,
                fmrel_ftr_zdb_id:           featureZdbId,
                fmrel_mrkr_zdb_id:          line.construct_zdb_id,
                fmrel_type:                 "contains innocuous sequence feature"
            )

            int_data_source.add(
                    ids_data_zdb_id:            featureZdbId,
                    ids_source_zdb_id:          lab_zdb_id
            )
            feature_assay.add(
                    featassay_feature_zdb_id:   featureZdbId,
                    featassay_mutagen:          'DNA',
                    featassay_mutagee:          'embryos'
            )
        }
        genotype.add(
                geno_zdb_id:                row.di_geno_zdb_id,
                geno_display_name:          line.feature_abbrev,
                geno_handle:                line.feature_abbrev
        )

        genotype_feature.add(
                genofeat_zdb_id:            row.di_genofeat_zdb_id,
                genofeat_geno_zdb_id:       row.di_geno_zdb_id,
                genofeat_feature_zdb_id:    featureZdbId,
                genofeat_zygocity:          unknown_zygocity,
                genofeat_dad_zygocity:      unknown_zygocity,
                genofeat_mom_zygocity:      unknown_zygocity
        )

        //if there's something else to put in the genotype, add it!
        if(line.additional_allele_zdb_id) {
            db.execute(insertSpecificIdIntoZdbActiveData("di_additional_genofeat_zdb_id", line.feature_abbrev))
            db.execute(upsertSpecificRecordAttribution(row.di_additional_genofeat_zdb_id))
            genotype_feature.add(
                    genofeat_zdb_id:            row.di_additional_genofeat_zdb_id,
                    genofeat_geno_zdb_id:       row.di_geno_zdb_id,
                    genofeat_feature_zdb_id:    line.additional_allele_zdb_id,
                    genofeat_zygocity:          unknown_zygocity,
                    genofeat_dad_zygocity:      unknown_zygocity,
                    genofeat_mom_zygocity:      unknown_zygocity
            )


        }


        //Generate the genotype names (this is a little imperfect, but will do)
        def geno_display_name;
        def geno_handle;
        db.eachRow("""select get_genotype_display(${row.di_geno_zdb_id}) as geno_display_name,
                             get_genotype_handle(${row.di_geno_zdb_id}) as geno_handle from single;
                   """) {
            geno_display_name = it.geno_display_name
            geno_handle = it.geno_handle
        }

        db.executeUpdate """
            update genotype
              set (geno_display_name,geno_handle) = (${geno_display_name},${geno_handle})
              where geno_zdb_id = ${row.di_geno_zdb_id}
        """

        //add source='feature type' attribution (a thing we do, but we're not sure why)
        db.execute(addFeatureTypeSourceRecordAttribution(featureZdbId))

    }

    //For each of these columns, add all of it's rows to record_attribution
    ["di_feature_zdb_id",
            "di_geno_zdb_id",
            "di_genofeat_zdb_id",
            "di_fmrel_zdb_id"].each {column ->
        //add record attribution if it's not there already
        db.execute(upsertRecordAttribution(column))
    }



    //test section, only one test for now, but it gets the idea in there..

    db.eachRow("select * from dorsky_ids;") { row ->
        println "testing $row.di_feature_abbrev"
        def featureZdbId
        db.eachRow("select feature_zdb_id from feature where feature_abbrev = ${row.di_feature_abbrev};") {
            featureZdbId = it.feature_zdb_id
        }

        //make sure record attribution with source = feature_type is inserted
        def recattribSourceTypeFeatureTypeCount = 0
        db.eachRow("""
            select count(*) from record_attribution
            where recattrib_source_zdb_id = ${attribution}
              and recattrib_data_zdb_id = ${featureZdbId}
              and recattrib_source_type = "feature type";
        """) {
            recattribSourceTypeFeatureTypeCount = it[0]
        }

        assert(recattribSourceTypeFeatureTypeCount == 1)
    }





        if ("--mail" in args) {
        def env = System.getenv()
        StringBuilder sb = new StringBuilder();

        def count
        db.eachRow(""" select count(*) from dorsky_ids; """) { count = it[0] }

        sb.append """
            <table>
            <caption style="font-weight: bold"> $count lines </caption>
            <tr>
               <th style="text-align: left; font-weight: bold">Feature</th>
               <th style="text-align: left; font-weight: bold">Genotype</th>
               <th style="text-align: left; font-weight: bold">Construct</th>
            </tr>
        """

        //is there a value in defining the template above and then using it below?  I'm not sure,
        //I guess I'm doing it because I can...
        def rowTemplate = {
            """
            <tr>
                <td style="vertical-align:top"> <a href="http://${env['DOMAIN_NAME']}/$it.feature_zdb_id">$it.feature_abbrev</a> </td>
                <td style="vertical-align:top"> <a href="http://${env['DOMAIN_NAME']}/$it.geno_zdb_id">$it.geno_display_name</a> </td>
                <td style="vertical-align:top"> <a href="http://${env['DOMAIN_NAME']}/$it.mrkr_zdb_id">$it.mrkr_abbrev</a> </td>
            </tr>
            """
        }

        db.eachRow("""
            select feature_abbrev, feature_zdb_id, geno_display_name, geno_zdb_id, mrkr_abbrev, mrkr_zdb_id
            from dorsky_ids
                join feature on feature_abbrev = di_feature_abbrev
                join genotype on geno_zdb_id = di_geno_zdb_id
                join feature_marker_relationship on fmrel_ftr_zdb_id = feature_zdb_id
                join marker on fmrel_mrkr_zdb_id = mrkr_zdb_id

        """) {
            println rowTemplate(it)
            sb.append rowTemplate(it)
        }
        sb.append "</table>"

        def subject = "[${env['INSTANCE']}] Dorsky Load"
        println subject

        //apparently the ant mailer is the friendliest mail syntax within groovy, this isn't too bad.
        def ant = new AntBuilder()
        ant.mail(mailhost:'localhost', mailport:25, subject:subject, messagemimetype: 'text/html') {
            from(address:'kevin@zfin.org')
            to(address:'kevin@zfin.org')
            to(address:'ybradford@zfin.org')
            message("${sb.toString()}")
        }
    }



    if ("--commit" in args)
        db.commit();
    else
        db.rollback();

}






def getDB() {
    //get environment variables
    def env = System.getenv()

    //get java properties
    def props = new Properties()
    new File("${env['TARGETROOT']}/home/WEB-INF/zfin.properties").withInputStream {
        stream -> props.load(stream)
    }

    //load the informix driver (doing this here means it doesn't have to be on the command line!)
    this.getClass().classLoader.rootLoader.addURL(new File("${env['TARGETROOT']}/lib/Java/ifxjdbc.jar").toURL())

    //make the database connection
    db = Sql.newInstance("jdbc:informix-sqli://${props['SQLHOSTS_HOST']}:${props['INFORMIX_PORT']}/${props['DBNAME']}:INFORMIXSERVER=${props['INFORMIXSERVER']};DB_LOCALE=en_US.utf8",'com.informix.jdbc.IfxDriver'
    )

    db
}