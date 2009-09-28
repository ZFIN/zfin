package org.zfin.sequence.blast.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.Database;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BlastDatabaseSelectController extends AbstractController {



    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {

        boolean isRoot = Person.isCurrentSecurityUserRoot() ;

        ModelAndView modelAndView = new ModelAndView("blast-database-select.page") ;
        BlastInfoBean blastInfoBean = new BlastInfoBean() ;
        String abbreviation = httpServletRequest.getParameter(LookupStrings.BLAST_DB) ;

        blastInfoBean.setNucleotideDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.NUCLEOTIDE,!isRoot,true));
        blastInfoBean.setProteinDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.PROTEIN,!isRoot,true));
        modelAndView.addObject(LookupStrings.FORM_BEAN,blastInfoBean) ;

        return modelAndView ;

    }
}