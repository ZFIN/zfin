package org.zfin.framework.mail;

import org.junit.Test;
import org.zfin.properties.AbstractZfinPropertiesTest;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests the MailSender classes.
 */
public class MailTest extends AbstractZfinPropertiesTest{

    private String unfilteredEmail1 = "ndunn\\@uoregon.edu" ;
    private String filteredEmail1 = "ndunn@uoregon.edu" ;
    private String unfilteredEmail2 = "nathandunn\\@zfin.org" ;
    private String filteredEmail2 = "nathandunn@zfin.org" ;

    private String[] unfilteredEmailList = new String[]{
            unfilteredEmail1, unfilteredEmail2
    } ;


    private final IntegratedJavaMailSender mailSender = new IntegratedJavaMailSender();

    @Test
    public void individualFilter(){
        assertEquals(filteredEmail1, mailSender.filterEmail(filteredEmail1));
        assertFalse(filteredEmail1.equals(unfilteredEmail1));
        assertEquals(filteredEmail1, mailSender.filterEmail(unfilteredEmail1));
    }

    @Test
    public void multipleFilter(){
        String[] emails = mailSender.filterEmail(unfilteredEmailList) ;
        assertEquals(filteredEmail1,emails[0]);
        assertEquals(filteredEmail2,emails[1]);
    }

    @Test
    public void emailProps(){
        assertEquals("test@zfin.org",  ZfinPropertiesEnum.DEFAULT_EMAIL.value());
        assertEquals("test@zfin.org",  ZfinProperties.splitValues(ZfinPropertiesEnum.DEFAULT_EMAIL)[0]);
        assertEquals("test@zfin.org", mailSender.filterEmail(ZfinPropertiesEnum.DEFAULT_EMAIL.value()));
        assertEquals("test@zfin.org",  mailSender.filterEmail(ZfinProperties.splitValues(ZfinPropertiesEnum.DEFAULT_EMAIL))[0]);
    }

    @Test
    public void multipleEmailProps(){
        String[] emails = mailSender.filterEmail(ZfinProperties.splitValues(ZfinPropertiesEnum.SWISSPROT_EMAIL_CURATOR)) ;
        assertEquals(filteredEmail1,emails[0]);
        assertEquals(filteredEmail2,emails[1]);
    }
}
