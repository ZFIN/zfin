package org.zfin.infrastructure.delete;

import org.apache.log4j.Logger;
import org.zfin.publication.Publication;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class DeletePublicationRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeletePublicationRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Publication publication = getPublicationRepository().getPublication(zdbID);
        if (publication == null)
            throw new NullPointerException("No publication found: " + zdbID);

        entity = publication;
        return validationReportList;

        /// Needs to be implemented, currently placed in edit_pub.apg
/*
        select recattrib_source_zdb_id
        from record_attribution, figure
        where recattrib_source_zdb_id = '$OID'
        and recattrib_data_zdb_id = fig_zdb_id
        and (exists (select 'x'
        from phenotype_experiment
        where phenox_fig_zdb_id = fig_zdb_id)
        or exists (select 'x'
        from construct_figure
        where consfig_fig_zdb_id = fig_zdb_id)
        or exists (select 'x'
        from expression_pattern_figure
        where xpatfig_fig_zdb_id = fig_zdb_id)
        or exists (select 'x'
        from genotype_figure_fast_search
        where gffs_fig_zdb_id = fig_zdb_id)
        )
                */
    }

    private Logger logger = Logger.getLogger(DeletePublicationRule.class);

    @Override
    public void prepareDelete() {
        entity = getPublicationRepository().getPublication(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
