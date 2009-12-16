package org.zfin.framework.presentation;

import org.apache.struts.tiles.DefinitionsFactoryConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Jul 17, 2006
 * Time: 2:10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class TilesConfigurationFormBean {
    private DefinitionsFactoryConfig config;

    public void setConfiguration(DefinitionsFactoryConfig config) {
        this.config = config;
    }

    public DefinitionsFactoryConfig getDefinitionsFactoryConfig() {
        return config;
    }

    public Set<String> getConfigurationFileNames() {
        String definitions = config.getDefinitionConfigFiles();

        if (definitions == null)
            return null;

        StringTokenizer st = new StringTokenizer(definitions, ",");
        Set<String> set = new HashSet<String>();
        while (st.hasMoreElements())
            set.add((String) st.nextElement());
        return set;

    }
}
