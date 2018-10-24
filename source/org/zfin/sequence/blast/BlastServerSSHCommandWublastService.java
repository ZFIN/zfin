package org.zfin.sequence.blast;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * deprecated No longer used.  Use MountedWublastBlastService instead.
 */
public final class BlastServerSSHCommandWublastService extends BlastServerSGEWublastService {

    private static final Logger logger = Logger.getLogger(BlastServerSSHCommandWublastService.class);


    private static BlastServerSSHCommandWublastService instance;

    protected BlastServerSSHCommandWublastService() {
    }

    public static BlastServerSSHCommandWublastService getInstance() {
        if (instance == null) {
            instance = new BlastServerSSHCommandWublastService();
        }
        return instance;
    }

    @Override
    public String blastOneDBToString(XMLBlastBean xmlBlastBean) throws BlastDatabaseException, BusException {
        throw new BlastDatabaseException("blast not implemented ") ;
    }

    @Override
    public String blastOneDBToString(XMLBlastBean xmlBlastBean, Database database) throws BlastDatabaseException, BusException {
        throw new BlastDatabaseException("blast not implemented ") ;
    }

    /**
     * In this sense, the key is the same
     *
     * @return String of commands.
     */
    protected List<String> getPrefixCommands() {
        if (prefixCommands.size() == 0) {
            prefixCommands.add(ZfinPropertiesEnum.SSH.value());
            prefixCommands.add("localhost");
            prefixCommands.add("-i");
        }
        return prefixCommands;
    }

    @Override
    public String getKeyPath() {
        return ZfinPropertiesEnum.WEBHOST_KEY_PATH + "/";    //To change body of overridden methods use File | Settings | File Templates.
    }

}
