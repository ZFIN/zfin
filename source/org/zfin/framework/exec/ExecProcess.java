package org.zfin.framework.exec;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * Make this class wrap the apache exec stuff.
 */
@Setter
@Getter
public class ExecProcess {

    protected CommandLine commandLine;
    protected OutputStream outputStream = new ByteArrayOutputStream();
    protected OutputStream errorStream = new ByteArrayOutputStream();
    protected File workingDirectory;
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
        if (workingDirectory != null) {
            defaultExecutor.setWorkingDirectory(workingDirectory);
        }
//        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream,fileInputStream) ;
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
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
        if (errorStream == null) {
            return null;
        } else {
            return errorStream.toString();
        }
    }

    public String getStandardOutput() {
        if (outputStream == null) {
            return null;
        } else {
            return outputStream.toString();
        }
    }

    public String toString() {
        return commandLine.toString();
    }

    public static String exec(String command) throws IOException, InterruptedException {
        ExecProcess process = new ExecProcess(command);
        process.exec();
        return process.getStandardOutput();
    }

    public static String exec(File workingDirectory, String command) throws IOException, InterruptedException {
        ExecProcess process = new ExecProcess(command);
        process.setWorkingDirectory(workingDirectory);
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
