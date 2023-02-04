package org.zfin.ui.repository;

import org.hibernate.query.Query;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;

import java.util.List;

public class HibernateDiseasePageRepository implements DiseasePageRepository {

	@Override
	public List<OmimPhenotypeDisplay> getGenesInvolved(GenericTerm term, Pagination pagination, boolean includeChildren) {
		if(!includeChildren) {
			String hql = "from OmimPhenotypeDisplay where disease = :disease ";

			Query<OmimPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, OmimPhenotypeDisplay.class);
			query.setParameter("disease", term);
			return query.getResultList();
		}
		String hql = "select omim from OmimPhenotypeDisplay as omim, TransitiveClosure as clo " +
			"where clo.child = omim.disease AND clo.root = :disease ";

		Query<OmimPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, OmimPhenotypeDisplay.class);
		query.setParameter("disease", term);
		return query.getResultList();
	}
}
