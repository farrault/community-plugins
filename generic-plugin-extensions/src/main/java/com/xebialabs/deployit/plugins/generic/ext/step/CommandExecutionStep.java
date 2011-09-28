package com.xebialabs.deployit.plugins.generic.ext.step;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Iterables.transform;
import static com.xebialabs.deployit.plugin.api.execution.Step.Result.Fail;
import static com.xebialabs.deployit.plugin.api.execution.Step.Result.Success;
import static com.xebialabs.overthere.CmdLineArgument.arg;
import static java.lang.String.format;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.overthere.ExecutionContextOverthereProcessOutputHandler;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcessOutputHandler;

@SuppressWarnings("serial")
public class CommandExecutionStep implements DeploymentStep {
    private int order;
    private Host target;
    private CmdLine cmdLine;

    private transient OverthereConnection remoteConn;

    public CommandExecutionStep(int order, Host target, String commandLine) {
        this(order, target, new CmdLine().add(toCmdLineArgs(commandLine)));
    }
    
    protected CommandExecutionStep(int order, Host target, CmdLine cmdLine) {
        this.order = order;
        this.target = target;
        this.cmdLine = cmdLine;
    }

    // FIXME: doesn't support arguments containing spaces
    protected static List<CmdLineArgument> toCmdLineArgs(String commandLine) {
        checkArgument(!nullToEmpty(commandLine).trim().isEmpty(), "commandLine");
        return ImmutableList.copyOf(transform(Splitter.on(' ').split(commandLine.replace('\n', ' ')), 
                new Function<String, CmdLineArgument>() {
                    @Override
                    public CmdLineArgument apply(String input) {
                        return arg(input);
                    }
                }));
    }

    public String getDescription() {
        return format("Execute '%s' on %s", cmdLine, target.getName());
    }

    @Override
    public Result execute(DeploymentExecutionContext ctx) throws Exception {
        try {
            OverthereProcessOutputHandler handle = new ExecutionContextOverthereProcessOutputHandler(ctx);
            ctx.logOutput("Executing command: " + cmdLine.toCommandLine(target.getOs(), true));
            int rc = getRemoteConnection().execute(handle, cmdLine);
            if (rc != 0) {
                ctx.logError("Command failed with return code " + rc);
                return Fail;
            }
        } finally {
            disconnect();
        }

        return Success;
    }

    protected OverthereConnection getRemoteConnection() {
        if (remoteConn == null) {
            remoteConn = target.getConnection();
        }
        return remoteConn;
    }

    private void disconnect() {
        if (remoteConn != null)
            remoteConn.close();

        remoteConn = null;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
