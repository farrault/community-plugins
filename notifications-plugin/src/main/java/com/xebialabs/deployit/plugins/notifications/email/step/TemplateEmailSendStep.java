package com.xebialabs.deployit.plugins.notifications.email.step;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.xebialabs.deployit.plugin.generic.freemarker.ConfigurationHolder;
import com.xebialabs.deployit.plugins.notifications.email.ci.MailServer;
import com.xebialabs.overthere.RuntimeIOException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SuppressWarnings("serial")
public class TemplateEmailSendStep extends EmailSendStep {
    private final Map<String, Object> vars;
    private final String templatePath;

    public TemplateEmailSendStep(int order, String description, MailServer mailServer, String fromAddress, 
            List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, 
            String subject, Map<String, Object> vars, String templatePath) {
        super(order, description, mailServer, fromAddress, toAddresses, ccAddresses, 
                bccAddresses, subject);
        this.vars = vars;
        this.templatePath = templatePath;
    }

    @Override
    protected String getBody() {
        return evaluateTemplate(templatePath, vars);
    }

    // adapted from TemplateArtifactCopyStep
    public String evaluateTemplate(String templatePath, Map<String, Object> vars) {
        Configuration cfg = ConfigurationHolder.getConfiguration();
        try {
            Template template = cfg.getTemplate(templatePath);
            StringWriter out = new StringWriter();
            template.process(vars, out);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        } 
    }
}
