package org.zfin.framework.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Convenience class that lists certain unix processes.
 */
public class UnixProcessServiceTest {

    @Test
    public void listAllProcesses(){
        List<UnixProcess> list = UnixProcessService.getProcesses();
        assertNotNull(list);
    }

}
