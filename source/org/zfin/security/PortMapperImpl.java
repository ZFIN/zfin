package org.zfin.security;

import org.acegisecurity.util.PortMapper;
import org.springframework.util.Assert;
import org.zfin.properties.ZfinProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is ZFIN implementation of the portMApper Interface. The class
 * provided by Acegi hard codes the ports for HTTP and HTTPS.
 * At ZFIN, Tomcat is proxied by Apache but Tomcat needs to run in secure mode to
 * work with Acegi. The secure and insecure ports are read from the ZFIN properties file.
 */
public class PortMapperImpl implements PortMapper {
    //~ Instance fields ================================================================================================

    private Map<Integer, Integer> httpsPortMappings;

    //~ Constructors ===================================================================================================

    public PortMapperImpl() {
        httpsPortMappings = new HashMap<Integer, Integer>();
        httpsPortMappings.put(80, 443);
        httpsPortMappings.put(8080, 8443);
    }

    //~ Methods ========================================================================================================

    /**
     * Returns the translated (Integer -> Integer) version of the original port mapping specified via
     * setHttpsPortMapping()
     *
     * @return DOCUMENT ME!
     */
    public Map getTranslatedPortMappings() {
        return httpsPortMappings;
    }

    public Integer lookupHttpPort(Integer httpsPort) {
        int insecurePort = ZfinProperties.getInsecureServerPort();
        if (insecurePort != 0)
            return insecurePort;

        for (Integer integer : httpsPortMappings.keySet()) {

            if (httpsPortMappings.get(integer).equals(httpsPort)) {
                return integer;
            }
        }

        return null;
    }

    public Integer lookupHttpsPort(Integer httpPort) {
        int securePort = ZfinProperties.getSecureServerPort();
        if (securePort != 0)
            return securePort;
        return httpsPortMappings.get(httpPort);
    }

    /**
     * <p>Set to override the default HTTP port to HTTPS port mappings of 80:443, and  8080:8443.</p>
     * In a Spring XML ApplicationContext, a definition would look something like this:<pre>
     *   &lt;property name="portMappings">    &lt;map>      &lt;entry key="80">&lt;value>443&lt;/value>&lt;/entry>
     *       &lt;entry key="8080">&lt;value>8443&lt;/value>&lt;/entry>    &lt;/map>  &lt;/property></pre>
     *
     * @param newMappings A Map consisting of String keys and String values, where for each entry the key is the string
     *                    representation of an integer HTTP port number, and the value is the string representation of the
     *                    corresponding integer HTTPS port number.
     * @throws IllegalArgumentException if input map does not consist of String keys and values, each representing an
     *                                  integer port number in the range 1-65535 for that mapping.
     */
    public void setPortMappings(Map newMappings) {
        Assert.notNull(newMappings, "A valid list of HTTPS port mappings must be provided");

        httpsPortMappings.clear();

        for (Object o : newMappings.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Integer httpPort = new Integer((String) entry.getKey());
            Integer httpsPort = new Integer((String) entry.getValue());

            if ((httpPort < 1) || (httpPort > 65535) || (httpsPort < 1)
                    || (httpsPort > 65535)) {
                throw new IllegalArgumentException("one or both ports out of legal range: " + httpPort + ", "
                        + httpsPort);
            }

            httpsPortMappings.put(httpPort, httpsPort);
        }

        if (httpsPortMappings.size() < 1) {
            throw new IllegalArgumentException("must map at least one port");
        }
    }
}
