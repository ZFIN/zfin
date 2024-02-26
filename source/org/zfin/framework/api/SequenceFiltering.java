package org.zfin.framework.api;

import org.zfin.gwt.root.util.StringUtils;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.MarkerDBLink;

public class SequenceFiltering extends Filtering<MarkerDBLink> {


    public SequenceFiltering() {
        filterFieldMap.put(FieldFilter.SEQUENCE_ACCESSION, accessionFilter);
        filterFieldMap.put(FieldFilter.SEQUENCE_TYPE, typeFilter);
        filterFieldMap.put(FieldFilter.ENTITY_ID, entityIdFilter);
        filterFieldMap.put(FieldFilter.SUPER_TYPE, superTypeFilter);
        filterFieldMap.put(FieldFilter.FOREIGN_DB, foreignDBFilter);
        filterFieldMap.put(FieldFilter.DISPLAY_GROUP, displayGroupFilter);
        filterFieldMap.put(FieldFilter.DB_LINK_INFO, dbInfoFilter);
    }

    public static FilterFunction<MarkerDBLink, String> accessionFilter =
        (dbLink, value) -> FilterFunction.contains(dbLink.getAccessionNumberDisplay(), value);

    public static FilterFunction<MarkerDBLink, String> dbInfoFilter =
        (dbLink, value) -> {
            if (dbLink.getLinkInfo() == null && StringUtils.isNotEmpty(value) ||
                dbLink.getLinkInfo() != null && StringUtils.isEmpty(value))
                return false;
            if (dbLink.getLinkInfo() == null && StringUtils.isEmpty(value))
                return true;
            return FilterFunction.contains(dbLink.getLinkInfo(), value);
        };

    public static FilterFunction<MarkerDBLink, String> entityIdFilter =
        (dbLink, value) -> FilterFunction.contains(dbLink.getZdbID(), value);

    public static FilterFunction<MarkerDBLink, String> typeFilter =
        (dbLink, value) -> FilterFunction.fullMatchMultiValueOR(dbLink.getSequenceType().toLowerCase(), value);

    public static FilterFunction<MarkerDBLink, String> superTypeFilter =
        (dbLink, value) -> FilterFunction.fullMatchMultiValueOR(dbLink.getReferenceDatabase().getForeignDBDataType().getSuperType().getValue().toLowerCase(), value);

    public static FilterFunction<MarkerDBLink, String> foreignDBFilter =
        (dbLink, value) -> FilterFunction.fullMatchMultiValueOR(dbLink.getReferenceDatabase().getForeignDB().getDbName().getValue().toLowerCase(), value);

    public static FilterFunction<MarkerDBLink, String> displayGroupFilter =
        (dbLink, value) -> FilterFunction.fullMatchMultiValueMultiEntityOR(
            dbLink.getReferenceDatabase().getDisplayGroups().stream().map(DisplayGroup::getGroupName).map(DisplayGroup.GroupName::getValue).toList()
            , value);


}
