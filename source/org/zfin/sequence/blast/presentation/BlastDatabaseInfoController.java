package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BlastDatabaseInfoController {

    private static Logger logger = LogManager.getLogger(BlastDatabaseInfoController.class);

    @RequestMapping("/blast/blast-definitions")
    protected String showBlastDefinitions(@RequestParam(required = false) String accession,
                                          @ModelAttribute("formBean") BlastInfoBean blastInfoBean) throws Exception {

        logger.info("abbrev: " + accession);
        // we don't want the proteinDB string
        boolean isRoot = Person.isCurrentSecurityUserRoot();
        if (accession != null && accession.trim().length() > 0) {
            blastInfoBean.setShowTitle(false);
            Database.AvailableAbbrev abbrev = Database.AvailableAbbrev.getType(accession);
            Database database = RepositoryFactory.getBlastRepository().getDatabase(abbrev);
            if (database != null) {
                logger.info("database is available: " + (isRoot || database.isPublicDatabase()));
                HibernateUtil.currentSession().flush();
                if (isRoot || database.isPublicDatabase()) {
                    List<DatabasePresentationBean> databasePresentationBeanList = new ArrayList<>();
                    DatabasePresentationBean databasePresentationBean = BlastPresentationService.createPresentationBean(database);
                    databasePresentationBean.setDirectChildren(BlastPresentationService.getDirectChildren(database, isRoot));
                    databasePresentationBean.setLeaves(BlastPresentationService.getLeaves(database));
                    databasePresentationBeanList.add(databasePresentationBean);
                    // should not show a database you don't have
                    if (database.getType().isNucleotide()) {
                        blastInfoBean.setNucleotideDatabases(databasePresentationBeanList);
                    } else {
//                    blastInfoBean.setProteinDatabases(BlastPresentationService.processFromChild(database,isRoot));
                        blastInfoBean.setProteinDatabases(databasePresentationBeanList);
                    }
                }
            } else {
                logger.info("database is NOT available: " + isRoot);
                blastInfoBean.setNucleotideDatabases(null);
                blastInfoBean.setProteinDatabases(null);
            }
        } else if (accession == null) {
            blastInfoBean.setShowTitle(true);
            blastInfoBean.setNucleotideDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.NUCLEOTIDE, !isRoot, true));
            blastInfoBean.setProteinDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.PROTEIN, !isRoot, true));

            cacheStatistics(blastInfoBean);
            if (Person.isCurrentSecurityUserRoot()) {
//                String remoteString = httpServletRequest.getParameter("remote") ;
                return "blast/blast-database-table";
            } else {
                return "blast/blast_database_info";
            }
        } else {
//        if(abbreviation!=null && abbreviation.trim().length()==0){
            return "blast/no_database_selected";
        }
        cacheStatistics(blastInfoBean);
        return "blast/blast_database_info";
    }

    protected BlastInfoBean cacheStatistics(BlastInfoBean blastInfoBean) {
        DatabaseStatisticsCache databaseStatisticsCache = WebHostDatabaseStatisticsCache.getInstance();

        logger.debug("do handleCurationEvent: " + blastInfoBean.isDoRefresh());

        // handle handleCurationEvent
        if (blastInfoBean.isDoRefresh() || !databaseStatisticsCache.isCached()) {
            databaseStatisticsCache.clearCache();
            databaseStatisticsCache.cacheAll();
            blastInfoBean.setDoRefresh(false);
        }


        if (CollectionUtils.isNotEmpty(blastInfoBean.getNucleotideDatabases())) {
            for (DatabasePresentationBean databasePresentationBean : blastInfoBean.getNucleotideDatabases()) {
                try {
                    DatabaseStatistics databaseStatistics = databaseStatisticsCache.getDatabaseStatistics(databasePresentationBean.getDatabase());
                    databasePresentationBean.setDatabaseStatistics(databaseStatistics);
                } catch (BlastDatabaseException e) {
                    logger.error("Failed to get stats for[" + databasePresentationBean.getDatabase().getAbbrev() + "]:\n" + e);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(blastInfoBean.getProteinDatabases())) {
            for (DatabasePresentationBean databasePresentationBean : blastInfoBean.getProteinDatabases()) {
                try {
                    DatabaseStatistics databaseStatistics = databaseStatisticsCache.getDatabaseStatistics(databasePresentationBean.getDatabase());
                    databasePresentationBean.setDatabaseStatistics(databaseStatistics);
                } catch (BlastDatabaseException e) {
                    logger.error("Failed to get stats for[" + databasePresentationBean.getDatabase().getAbbrev() + "]:\n" + e);
                }
            }
        }
        return blastInfoBean;
    }
}