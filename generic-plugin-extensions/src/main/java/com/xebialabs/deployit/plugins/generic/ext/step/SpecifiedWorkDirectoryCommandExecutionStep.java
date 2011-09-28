package com.xebialabs.deployit.plugins.generic.ext.step;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;

import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.overthere.CmdLine;

@SuppressWarnings("serial")
public class SpecifiedWorkDirectoryCommandExecutionStep extends CommandExecutionStep {
    
    public SpecifiedWorkDirectoryCommandExecutionStep(int order, Host target, String commandLine,
            String workingDirectory) {
        super(order, target, inWorkingDirectory(workingDirectory)
                .add(toCmdLineArgs(commandLine)));
    }
    
    private static CmdLine inWorkingDirectory(String workingDirectory) {
        checkArgument(!nullToEmpty(workingDirectory).trim().isEmpty(), "commandLine");
        CmdLine inWorkingDirectory = new CmdLine();
        inWorkingDirectory.addArgument("cd");
        inWorkingDirectory.addArgument(workingDirectory);
        inWorkingDirectory.addRaw("&&");
        return inWorkingDirectory;
    }
}
