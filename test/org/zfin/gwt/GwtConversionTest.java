package org.zfin.gwt;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.repository.RepositoryFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test for non-UTF characters (case 6544)
 */
public class GwtConversionTest extends AbstractDatabaseTest{

    private static Logger logger = Logger.getLogger(GwtConversionTest.class);

    private final String unescapedName = "(ÑTg:123)" ;
    private final String escapedName = "(&mdash;Tg:123)";

    @Test
    public void testConverter(){
        String converted = DTOConversionService.escapeString(unescapedName);
        logger.debug(converted);
        assertNotNull(converted);
        assertEquals(escapedName, converted);
    }

    @Test
    public void testUnescape(){
        String converted = DTOConversionService.unescapeString(escapedName);
        logger.debug(converted);
        assertEquals(unescapedName,converted);
        assertEquals(escapedName,DTOConversionService.escapeString(converted));
    }

    @Test
    public void testConverterCollection(){
        Set<String> strings = new HashSet<String>();
        strings.add(unescapedName);
        Collection<String> converted = DTOConversionService.escapeStrings(strings);
        assertNotNull(converted);
        String convertedString = converted.iterator().next() ;
        assertEquals(DTOConversionService.escapeString(unescapedName), convertedString);
        assertEquals(escapedName, convertedString);

        Collection<String> unescapedCollection = DTOConversionService.unescapeStrings(converted);
        convertedString = unescapedCollection.iterator().next() ;
        assertEquals(unescapedName, convertedString);
    }

    @Test
    public void testNewName(){

        HibernateUtil.createTransaction();
        try {
            Feature feature = (Feature) HibernateUtil.currentSession()
                    .createCriteria(Feature.class)
                    .setMaxResults(1)
                    .uniqueResult()
                    ;
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
            assertEquals(1,dataAliasList.size());
            assertEquals(DTOConversionService.escapeString(unescapedName), dataAliasList.get(0).getAlias());
        } catch (HibernateException e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }
}
