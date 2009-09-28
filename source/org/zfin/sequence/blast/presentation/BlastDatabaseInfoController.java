package org.zfin.sequence.blast.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.*;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class BlastDatabaseInfoController extends AbstractCommandController {

    @Override
    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {

        ModelAndView modelAndView ;
        BlastInfoBean blastInfoBean = (BlastInfoBean) o ;

        boolean isRoot = Person.isCurrentSecurityUserRoot() ;

        String abbreviation = httpServletRequest.getParameter(LookupStrings.ACCESSION) ;
        logger.info("abbrev: "+ abbreviation);


        // we don't want the proteinDB string

        if(abbreviation!=null){
            blastInfoBean.setShowTitle(false);
            modelAndView = new ModelAndView("single-blast-database-info.page") ;
            Database.AvailableAbbrev abbrev = Database.AvailableAbbrev.getType(abbreviation) ;
            Database database = RepositoryFactory.getBlastRepository().getDatabase(abbrev) ;
            if(database!=null){
                logger.info("database is available: "+ (isRoot==true || database.isPublicDatabase()));
                HibernateUtil.currentSession().flush();
                if(isRoot==true || database.isPublicDatabase()){
                    List<DatabasePresentationBean> databasePresentationBeanList = new ArrayList<DatabasePresentationBean>() ;
                    DatabasePresentationBean databasePresentationBean = BlastPresentationService.createPresentationBean(database) ;
                    databasePresentationBean.setDirectChildren(BlastPresentationService.getDirectChildren(database,isRoot));
                    databasePresentationBean.setLeaves(BlastPresentationService.getLeaves(database));
                    databasePresentationBeanList.add(databasePresentationBean) ;
                    // should not show a database you don't have
                    if(true==database.getType().isNucleotide()){
                        blastInfoBean.setNucleotideDatabases(databasePresentationBeanList);
                    }
                    else{
//                    blastInfoBean.setProteinDatabases(BlastPresentationService.processFromChild(database,isRoot));
                        blastInfoBean.setProteinDatabases(databasePresentationBeanList);
                    }
                }
            }
            else{
                logger.info("database is NOT available: "+ (isRoot==true || database.isPublicDatabase()));
                blastInfoBean.setNucleotideDatabases(null);
                blastInfoBean.setProteinDatabases(null);
            }
        }
        else{
            blastInfoBean.setShowTitle(true);
            blastInfoBean.setNucleotideDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.NUCLEOTIDE,!isRoot,true));
            blastInfoBean.setProteinDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.PROTEIN,!isRoot,true));

            if(true==Person.isCurrentSecurityUserRoot()){
//                String remoteString = httpServletRequest.getParameter("remote") ;
                modelAndView = new ModelAndView("blast-database-table.page") ;
            }
            else{
                modelAndView = new ModelAndView("blast-database-info.page") ;
            }
        }

        cacheStatistics(blastInfoBean) ;

        modelAndView.addObject(LookupStrings.FORM_BEAN,blastInfoBean) ;

        return modelAndView ;

    }

    protected BlastInfoBean cacheStatistics(BlastInfoBean blastInfoBean){
        DatabaseStatisticsCache databaseStatisticsCache = WebHostDatabaseStatisticsCache.getInstance() ;

        logger.debug("do refresh: "+ blastInfoBean.isDoRefresh());

        // handle refresh
        if(blastInfoBean.isDoRefresh()){
            databaseStatisticsCache.clearCache() ;
            blastInfoBean.setDoRefresh(false);
        }


        if(CollectionUtils.isNotEmpty(blastInfoBean.getNucleotideDatabases())){
            for(DatabasePresentationBean databasePresentationBean:blastInfoBean.getNucleotideDatabases()){
                try {
                    DatabaseStatistics databaseStatistics = databaseStatisticsCache.getDatabaseStatistics(databasePresentationBean.getDatabase()) ;
                    databasePresentationBean.setDatabaseStatistics(databaseStatistics);
                } catch (BlastDatabaseException e) {
                    logger.error("Failed to get stats for["+databasePresentationBean.getDatabase().getAbbrev()+"]:\n"+e);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(blastInfoBean.getProteinDatabases())){
            for(DatabasePresentationBean databasePresentationBean:blastInfoBean.getProteinDatabases()){
                try {
                    DatabaseStatistics databaseStatistics = databaseStatisticsCache.getDatabaseStatistics(databasePresentationBean.getDatabase()) ;
                    databasePresentationBean.setDatabaseStatistics(databaseStatistics);
                } catch (BlastDatabaseException e) {
                    logger.error("Failed to get stats for["+databasePresentationBean.getDatabase().getAbbrev()+"]:\n"+e);
                }
            }
        }
        return blastInfoBean ;
    }
}