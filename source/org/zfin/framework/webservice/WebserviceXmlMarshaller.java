package org.zfin.framework.webservice;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is to be used for all webservice marshalling.
 * This allows us to change the method in exactly one place.
 */
public class WebserviceXmlMarshaller {

    private final static Logger logger = Logger.getLogger(WebserviceXmlMarshaller.class) ;

    private static Map<Class,Unmarshaller> unmarshallerMap = new HashMap<Class,Unmarshaller>();

    public static String marshal(Object marshallingTarget) {
        StringWriter stringWriter = new StringWriter() ;
        try {
            Marshaller.marshal(marshallingTarget,stringWriter);
            stringWriter.flush();
        } catch (Exception e) {
            logger.error("Failed to marshal as xml",e);
        }
        return stringWriter.getBuffer().toString();
    }

    public static Object unmarshal(String xml,Class clazz) {
        StringReader stringReader = new StringReader(xml) ;
        try {
            Unmarshaller unmarshaller ;
            if(unmarshallerMap.containsKey(clazz)){
                unmarshaller = unmarshallerMap.get(clazz) ;
            }
            else{
                unmarshaller = new Unmarshaller(clazz) ;
                unmarshallerMap.put(clazz,unmarshaller) ;
            }
            return unmarshaller.unmarshal(clazz,stringReader) ;
        } catch (Exception e) {
            logger.error("Failed to read xml:\n"+xml,e);
            return null ;
        }
    }
}
