package org.zfin.ui.repository;

import org.hibernate.query.Query;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.repository.PaginationResultFactory;

public class HibernateDiseasePageRepository implements DiseasePageRepository {

	@Override
	public PaginationResult<OmimPhenotypeDisplay> getGenesInvolved(GenericTerm term, Pagination pagination, boolean includeChildren) {
		PaginationBean bean = PaginationBean.getPaginationBean(pagination);
		if (!includeChildren) {
			String hql = "from OmimPhenotypeDisplay where disease = :disease ";
			hql += "order by homoSapiensGene.symbol";
			Query<OmimPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, OmimPhenotypeDisplay.class);
			query.setParameter("disease", term);
			return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
		}
		String hql = "select omim from OmimPhenotypeDisplay as omim, TransitiveClosure as clo " +
			"where clo.child = omim.disease AND clo.root = :disease ";
		hql += "order by omim.homoSapiensGene.symbol";

		Query<OmimPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, OmimPhenotypeDisplay.class);
		query.setParameter("disease", term);
		return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
	}

}
