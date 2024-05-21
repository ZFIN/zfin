package org.zfin.framework.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * Make this class wrap the apache exec stuff.
 */
public class ExecProcess {

    protected CommandLine commandLine;
    protected ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    protected ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream();
    protected int[] exitValues = {0};
    // this might get used once we have an input stream
//    protected FileInputStream fileInputStream  ;

    /**
     * This command assumes a tokenizer
     *
     * @param command line
     */
    public ExecProcess(String command) {
        setCommand(command);
    }

    public ExecProcess(List<String> commandList, boolean handleQuoting) {
        setCommand(commandList, handleQuoting);
    }

    public ExecProcess(List<String> commandList) {
        this(commandList, true);
    }

    public int exec() throws IOException, InterruptedException {
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        defaultExecutor.setExitValues(exitValues);
//        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream,fileInputStream) ;
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream, byteArrayErrorStream);
        defaultExecutor.setStreamHandler(pumpStreamHandler);
        return defaultExecutor.execute(commandLine);
    }

    public int exec(List<String> commandList) throws IOException, InterruptedException {
        setCommand(commandList);
        return exec();
    }

    public void setCommand(String command) {
        commandLine = CommandLine.parse(command);
    }

    public void setCommand(List<String> commandList, boolean handleQuoting) {
        commandLine = new CommandLine(commandList.get(0));
        for (int i = 1; i < commandList.size(); i++) {
            commandLine.addArgument(commandList.get(i), handleQuoting);
        }
    }

    public void setCommand(List<String> commandList) {
        setCommand(commandList, true);
    }

    public String getStandardError() {
        if (byteArrayErrorStream == null) {
            return null;
        } else {
            return byteArrayErrorStream.toString();
        }
    }

    public String getStandardOutput() {
        if (byteArrayOutputStream == null) {
            return null;
        } else {
            return byteArrayOutputStream.toString();
        }
    }

    public int[] getExitValues() {
        return exitValues;
    }

    public void setExitValues(int[] exitValues) {
        this.exitValues = exitValues;
    }

    public String toString() {
        return commandLine.toString();
    }

    public static String exec(String command) throws IOException, InterruptedException {
        ExecProcess process = new ExecProcess(command);
        process.exec();
        return process.getStandardOutput();
    }

    /**
     * Like the static method "exec", but it accepts the command and args as a list instead of a string
     * @param commands The command to execute and its arguments
     * @param handleQuoting Boolean for whether or not to handle quoting of args
     * @return Returns the standard output of the command as a string
     * @throws IOException
     * @throws InterruptedException
     */
    public static String execList(List<String> commands, boolean handleQuoting) throws IOException, InterruptedException {
        ExecProcess process = new ExecProcess(commands, handleQuoting);
        process.exec();
        return process.getStandardOutput();
    }

}
