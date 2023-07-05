package org.zfin.framework.api;

import org.zfin.publication.Publication;

public class PublicationFiltering extends Filtering<Publication> {


    public PublicationFiltering() {
        filterFieldMap.put(FieldFilter.PUBLICATION_TYPE, publicationTypeFilter);
        filterFieldMap.put(FieldFilter.PUBLICATION_ID, publicationIdFilter);
        filterFieldMap.put(FieldFilter.PUBLICATION_AUTHOR, publicationAuthorFilter);
    }

    public static FilterFunction<Publication, String> publicationTypeFilter =
            (publication, value) -> FilterFunction.contains(publication.getType().getDisplay(), value);

    public static FilterFunction<Publication, String> publicationIdFilter =
            (publication, value) -> FilterFunction.contains(publication.getZdbID(), value);


    public static FilterFunction<Publication, String> publicationAuthorFilter =
            (publication, value) -> FilterFunction.contains(publication.getShortAuthorList(), value);


}
