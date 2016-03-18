#!/bin/bash
//usr/bin/env groovy -cp "$SOURCEROOT/home/WEB-INF/lib*:$SOURCEROOT/lib/Java/*:$SOURCEROOT/home/WEB-INF/classes:$CATALINA_HOME/endorsed/*" "$0" $@; exit $?

import org.hibernate.Session
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.orthology.Species
import org.zfin.properties.ZfinProperties
import org.zfin.sequence.DisplayGroup
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.ReferenceDatabase

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
new HibernateSessionCreator()

Session session = HibernateUtil.currentSession()
tx = session.beginTransaction()

signafish = new ForeignDB()
signafish.with {
    dbName = ForeignDB.AvailableName.SIGNAFISH
    dbUrlPrefix = "https://http://signafish.org/protein/"
    displayName = "SignaFish"
    significance = 3
}
session.save(signafish)
println "Created ForeignDB $signafish.dbID"

pathwayGroup = new DisplayGroup()
pathwayGroup.with {
    groupName = DisplayGroup.GroupName.PATHWAYS
    definition = "displayed on the intercations and pathway section of the gene page"
}
session.save(pathwayGroup)
println "Created DisplayGroup $pathwayGroup.id"

signafishDb = new ReferenceDatabase()
signafishDb.with {
    foreignDB = signafish
    organism = Species.ZEBRAFISH
    foreignDBDataType = (ForeignDBDataType) session.get(ForeignDBDataType.class, 13L) // other - summary page
}
session.save(signafishDb)
println "Created ReferenceDatabase $signafishDb.zdbID"

pathwayGroup.setReferenceDatabases([signafishDb] as Set)
session.save(pathwayGroup)

if (args.contains('--commit')) {
    print "Committing changes ... "
    tx.commit()
} else {
    print "Rolling back changes ... "
    tx.rollback()
}
session.close()
println "done"

System.exit(0)
