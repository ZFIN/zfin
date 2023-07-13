package org.zfin.ui.repository;

import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Clone;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Map;

public interface PublicationPageRepository {

    PaginationResult<ExpressionTableRow> getPublicationExpression(Publication publication, Pagination pagination);

    PaginationResult<Clone> getProbes(Publication publication, Pagination pagination);

    List<String> getProbeTypes(Publication publication, Pagination pagination);

    Map<Publication, List<ExpressionTableRow>> getAllPublicationExpression(Pagination pagination);

    Map<Publication, List<Clone>> getAllProbes(Pagination pagination);
}
