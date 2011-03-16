package org.zfin.ontology.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ActiveData;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generic entry point for viewing a term detail page.
 */
public class OntologyTermDetailController extends AbstractCommandController {

    public OntologyTermDetailController() {
        setCommandClass(OntologyBean.class);
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        OntologyBean form = (OntologyBean) command;
        String termID = form.getTermID();
        if (termID == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, "");

        GenericTerm term;
        if (termID.contains(ActiveData.Type.TERM.name())){
            term = RepositoryFactory.getOntologyRepository().getTermByZdbID(termID);
        }
        else
        if( termID.contains(ActiveData.Type.ANAT.name() )){
            term = RepositoryFactory.getOntologyRepository().getTermByZdbID(termID);
        } else {
            // try an obo id
            term = RepositoryFactory.getOntologyRepository().getTermByOboID(termID);
        }
        if (term == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, termID);
        if (term.getOntology().equals(Ontology.ANATOMY)) {
            if (termID.contains(ActiveData.Type.ANAT.name())){
                response.sendRedirect("/action/anatomy/term-detail?anatomyItem.zdbID=" + termID);
            }
            else{
                response.sendRedirect("/action/anatomy/term-detail?id=" + term.getOboID());
            }
        } else if (Ontology.isGoOntology(term.getOntology())) {
            response.sendRedirect("http://www.ebi.ac.uk/QuickGO/GTerm?id=" + term.getOboID());
        }
        form.setTerm(term);
        return new ModelAndView("ontology-term", LookupStrings.FORM_BEAN, form);
    }
}
