package com.xebialabs.deployit.plugin.lock;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.xebialabs.deployit.plugin.api.execution.ExecutionContext;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.udm.ControlTask;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.base.BaseContainer;

/**
 * Lock manager CI that provides control tasks to list and clear locks.
 */
@SuppressWarnings("serial")
@Metadata(root = Metadata.ConfigurationItemRoot.INFRASTRUCTURE, virtual = false, description = "Manager for container locks")
public class Manager extends BaseContainer {

	@SuppressWarnings("rawtypes")
    @ControlTask(description="Clears all locks")
	public List<Step> clearLocks() {
		Step clearLocksStep = new Step() {

			@Override
			public String getDescription() {
				return "Clearing all locks";
			}

			@Override
			public Result execute(ExecutionContext ctx) throws Exception {
				LockFileHelper.clearLocks();
				return Result.Success;
			}
		};
		
		return newArrayList(clearLocksStep);
	}
	
	@SuppressWarnings("rawtypes")
    @ControlTask(description="Lists all locks")
	public List<Step> listLocks() {
		Step listLocksStep = new Step() {

			@Override
			public String getDescription() {
				return "Listing all locks";
			}

			@Override
			public Result execute(ExecutionContext ctx) throws Exception {
				ctx.logOutput("The following containers are currently locked:");

				List<String> locksListing = LockFileHelper.listLocks();
				if (locksListing.isEmpty()) {
					ctx.logOutput("<none>");
				} else {
					for (String string : locksListing) {
						ctx.logOutput("- " + string);
					}
				}
				
				ctx.logOutput("\n");
				ctx.logOutput("Note: this control task is expected to fail.");
				
				// Needed so the logging is shown in the GUI
				return Result.Fail;
			}
		};
		
		return newArrayList(listLocksStep);
	}
}
