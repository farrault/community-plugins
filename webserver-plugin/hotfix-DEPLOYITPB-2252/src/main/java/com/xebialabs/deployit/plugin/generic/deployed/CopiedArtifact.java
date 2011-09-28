package com.xebialabs.deployit.plugin.generic.deployed;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.xebialabs.deployit.plugin.api.deployment.planning.Create;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.planning.Destroy;
import com.xebialabs.deployit.plugin.api.deployment.planning.Modify;
import com.xebialabs.deployit.plugin.api.udm.DeployableArtifact;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.artifact.Artifacts;
import com.xebialabs.deployit.plugin.api.udm.artifact.DerivedArtifact;
import com.xebialabs.deployit.plugin.api.udm.artifact.PlaceholderReplacer;
import com.xebialabs.deployit.plugin.generic.step.ArtifactCopyStep;
import com.xebialabs.deployit.plugin.generic.step.ArtifactDeleteStep;
import com.xebialabs.overthere.OverthereFile;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "An artifact deployed on a generic container")
public class CopiedArtifact<D extends DeployableArtifact> extends AbstractDeployedArtifact<D> implements DerivedArtifact<D> {

    private boolean useDescriptionGeneratedByStep = false;

    private OverthereFile placeholderProcessedFile;

    @Property(required = false, category= "Placeholders", description = "A key/value pair mapping of placeholders in the deployed artifact to their values. Special values are <ignore> and <empty>")
    private Map<String, String> placeholders = newHashMap();

    @Override
    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    @Override
    public OverthereFile getFile() {
        return placeholderProcessedFile;
    }

    @Override
    public void setFile(OverthereFile file) {
        this.placeholderProcessedFile = file;
    }

	@Override
    public D getSourceArtifact() {
	    return getDeployable();
    }

    @Override
    public void initFile(PlaceholderReplacer replacer) {
        Artifacts.replacePlaceholders(this,replacer);
    }

    @Create
    public void executeCreate(DeploymentPlanningContext ctx) {
        ArtifactCopyStep step = new ArtifactCopyStep(getCreateOrder(), getFile(), getContainer(), getTargetDirectory());
        step.setCreateTargetPath(isCreateTargetDirectory());
        step.setTargetFileName(resolveTargetFileName());
        step.setSourceFileDescription(getDeployable().getName());
        if (!useDescriptionGeneratedByStep)
            step.setDescription(getDescription(getCreateVerb()));
        ctx.addStep(step);
    }

    @Modify
    public void executeModify(DeploymentPlanningContext ctx) {
        executeDestroy(ctx);
        executeCreate(ctx);
    }

    @Destroy
    public void executeDestroy(DeploymentPlanningContext ctx) {
        ArtifactDeleteStep step = new ArtifactDeleteStep(getDestroyOrder(), getContainer(), getDeployable(), getTargetDirectory());
        step.setTargetDirectoryShared(isTargetDirectoryShared());
        step.setTargetFile(resolveTargetFileName());
        if (!useDescriptionGeneratedByStep)
            step.setDescription(getDescription(getDestroyVerb()));
        ctx.addStep(step);
    }

    public void setUseDescriptionGeneratedByStep(boolean useDescriptionGeneratedByStep) {
        this.useDescriptionGeneratedByStep = useDescriptionGeneratedByStep;
    }

}
