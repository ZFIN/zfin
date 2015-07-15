package org.zfin.profile.service;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;



public class BeanFieldUpdateTest {
    BeanFieldUpdate beanFieldUpdate = new BeanFieldUpdate();

    @Test
    public void testNull() {
        beanFieldUpdate.setTo("null");
        beanFieldUpdate.setNullToTrueNull();
        assertNull(beanFieldUpdate.getTo());

        beanFieldUpdate.setTo("Null");
        beanFieldUpdate.setNullToTrueNull();
        assertNull(beanFieldUpdate.getTo());

        beanFieldUpdate.setTo("NULL");
        beanFieldUpdate.setNullToTrueNull();
        assertNull(beanFieldUpdate.getTo());

        beanFieldUpdate.setTo("nuLl");
        beanFieldUpdate.setNullToTrueNull();
        assertNull(beanFieldUpdate.getTo());
    }

    @Test
    public void testNotNull() {
        beanFieldUpdate.setTo("");
        beanFieldUpdate.setNullToTrueNull();
        assertNotNull(beanFieldUpdate.getTo());

        beanFieldUpdate.setTo("test avenue");
        beanFieldUpdate.setNullToTrueNull();
        assertNotNull(beanFieldUpdate.getTo());

        beanFieldUpdate.setTo("123 testingtontown");
        beanFieldUpdate.setNullToTrueNull();
        assertNotNull(beanFieldUpdate.getTo());

        beanFieldUpdate.setTo(" ");
        beanFieldUpdate.setNullToTrueNull();
        assertNotNull(beanFieldUpdate.getTo());
    }

}
