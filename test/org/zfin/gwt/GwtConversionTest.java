package org.zfin.gwt;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for non-UTF characters (case 6544)
 */
public class GwtConversionTest extends AbstractDatabaseTest {

    private static Logger logger = Logger.getLogger(GwtConversionTest.class);
    private static FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    private final String unescapedName = "(\u2014Tg:123)";
    private final String escapedName = "(&#8212;Tg:123)";
    private final String unescapedNote = "(\u0392Tg:123)";
    private final String escapedNote = "(&Beta;Tg:123)";

    @Test
    public void testNewName() {

        HibernateUtil.createTransaction();
        try {
            Feature feature = (Feature) HibernateUtil.currentSession()
                    .createCriteria(Feature.class)
                    .setMaxResults(1)
                    .uniqueResult();
            assertNotNull(feature);

            FeatureAlias featureAlias = new FeatureAlias();
            featureAlias.setFeature(feature);
            featureAlias.setAlias(DTOConversionService.escapeString(unescapedName));
            featureAlias.setAliasLowerCase(DTOConversionService.escapeString(unescapedName.toLowerCase()));
            String groupName = DataAliasGroup.Group.ALIAS.toString();
            DataAliasGroup group = RepositoryFactory.getInfrastructureRepository().getDataAliasGroupByName(groupName);
            featureAlias.setAliasGroup(group);  //default for database, hibernate tries to insert null
            HibernateUtil.currentSession().save(featureAlias);

            List<DataAlias> dataAliasList = RepositoryFactory.getInfrastructureRepository().getDataAliases(DTOConversionService.escapeString(unescapedName.toLowerCase()));
            assertNotNull(dataAliasList);
            assertEquals(1, dataAliasList.size());
            assertEquals(DTOConversionService.escapeString(unescapedName), dataAliasList.get(0).getAlias());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void testMarkerPublicNote() {

        HibernateUtil.createTransaction();
        try {
            Marker marker = markerRepository.getMarkerByID("ZDB-ATB-090831-1");

            String newNote = DTOConversionService.escapeString(unescapedNote);
            marker.setPublicComments(newNote);
            HibernateUtil.currentSession().save(marker);
            assertEquals(DTOConversionService.escapeString(unescapedNote), marker.getPublicComments());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void testFeaturePublicNote() {

        HibernateUtil.createTransaction();
        try {
            Feature ftr = featureRepository.getFeatureByID("ZDB-ALT-070628-4");
            assertEquals(DTOConversionService.escapeString(unescapedNote), escapedNote);

            String newNote = DTOConversionService.escapeString(unescapedNote);
            ftr.setPublicComments(newNote);
            HibernateUtil.currentSession().save(ftr);
            assertEquals(DTOConversionService.escapeString(unescapedNote), ftr.getPublicComments());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }
}
