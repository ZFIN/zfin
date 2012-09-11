package org.zfin.profile.service;

import org.junit.Test;
import org.zfin.profile.Person;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 */
public class BeanCompareServiceTest {


//    @Autowired
    private BeanCompareService beanCompareService = new BeanCompareService();

    @Test
    public void testGetter() throws Exception{
//        assertEquals("isName", beanCompareService.generateGetterNameFromFieldName("name",Boolean.class)) ;
//        assertEquals("isName", beanCompareService.generateGetterNameFromFieldName("name",boolean.class)) ;
        assertEquals("getName", beanCompareService.generateGetterNameFromFieldName("name",String.class)) ;
        assertEquals("setName", beanCompareService.generateSetterNameFromFieldName("name")) ;
    }

    @Test
    public void test1() throws Exception{
        Person p1 = new Person();
        String oldField =  "bob" ;
        p1.setFirstName(oldField);
        Person p2 = new Person();
        String newField =  "john" ;
        p2.setFirstName(newField);

        BeanFieldUpdate beanFieldUpdate = beanCompareService.compareBeanField("firstName", p1, p2) ;

        assertEquals("BeanFieldUpdate{field='firstName', from=bob, to=john, fieldType=class java.lang.String}",beanFieldUpdate.toString() ) ;
        assertNotNull(beanCompareService.compareBeanField("firstName", p1, p2));
        beanCompareService.applyUpdate(p1, beanFieldUpdate);
        assertNull(beanCompareService.compareBeanField("firstName", p1, p2));

    }

    @Test
    public void testNullField() throws Exception{
        Person p1 = new Person();
        Person p2 = new Person();

        String oldField =  "bob" ;
        p1.setFirstName(oldField);
        String newField =  null ;
        p2.setFirstName(newField);

        assertEquals("BeanFieldUpdate{field='firstName', from=bob, to=null, fieldType=class java.lang.String}"
                ,beanCompareService.compareBeanField("firstName", p1, p2,String.class,true).toString()) ;
        assertNull(beanCompareService.compareBeanField("firstName", p1, p2)) ;


        oldField = null  ;
        p1.setFirstName(oldField);
        newField = "john"  ;
        p2.setFirstName(newField);
        BeanFieldUpdate beanFieldUpdate = beanCompareService.compareBeanField("firstName", p1, p2) ;
        assertEquals("BeanFieldUpdate{field='firstName', from=null, to=john, fieldType=class java.lang.String}",beanFieldUpdate.toString()) ;
        assertNotNull(beanCompareService.compareBeanField("firstName", p1, p2));

        List<BeanFieldUpdate> updateList = new ArrayList<BeanFieldUpdate>();
        updateList.add(beanFieldUpdate);
        beanCompareService.applyUpdates(p1, updateList) ;
        assertNull(beanCompareService.compareBeanField("firstName", p1, p2)) ;

    }


    @Test
    public void testBooleanSetter() throws Exception{
        Person p1 = new Person();
        Person p2 = new Person();
        String field = "emailList" ;

        Boolean oldField =  true ;
        p1.setEmailList(oldField);
        Boolean newField =  null ;
//        p2.setEmailList(newField);

        assertEquals("BeanFieldUpdate{field='emailList', from=true, to=false, fieldType=class java.lang.Boolean}"
                ,beanCompareService.compareBeanField("emailList", p1, p2, Boolean.class, true).toString()) ;
        assertNull(beanCompareService.compareBeanField("emailList", p1, p2, Boolean.class, true));

    }
}
