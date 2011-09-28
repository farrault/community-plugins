package com.xebialabs.deployit.plugins.notifications.email.step;

import static com.xebialabs.deployit.plugin.api.execution.Step.Result.Success;
import static com.xebialabs.deployit.plugins.notifications.email.util.Addresses.toRecipients;
import static com.xebialabs.deployit.plugins.notifications.email.util.Addresses.NameAndAddress.toNameAndAddress;
import static java.lang.String.format;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;

import java.util.List;

import org.codemonkey.simplejavamail.Email;

import com.xebialabs.deployit.plugin.generic.step.GenericBaseStep;
import com.xebialabs.deployit.plugins.notifications.email.ci.MailServer;
import com.xebialabs.deployit.plugins.notifications.email.util.Addresses.NameAndAddress;
import com.xebialabs.deployit.plugins.notifications.email.util.Addresses.Recipient;

@SuppressWarnings("serial")
public abstract class EmailSendStep extends GenericBaseStep {
    protected final String fromAddress;
    protected final List<String> toAddresses;
    protected final List<String> ccAddresses;
    protected final List<String> bccAddresses;
    protected final String subject;
    
    protected EmailSendStep(int order, String description, MailServer mailServer, String fromAddress, List<String> toAddresses,
            List<String> ccAddresses, List<String> bccAddresses, String subject) {
        super(order, description, mailServer);
        this.fromAddress = fromAddress;
        this.toAddresses = toAddresses;
        this.ccAddresses = ccAddresses;
        this.bccAddresses = bccAddresses;
        this.subject = subject;
    }

    protected abstract String getBody();
    
    @Override
    public Result doExecute() throws Exception {
        String body = getBody();
        getCtx().logOutput(format("Sending email...%nFrom: %s%nTo: %s%nCc: %s%nBcc: %s%nSubject: %s%n%n%s%n",
                fromAddress, toAddresses, ccAddresses, bccAddresses, subject, body));
        ((MailServer) getContainer()).getMailer().sendMail(getEmail(body));
        getCtx().logOutput("Email sent successfully");
        return Success;
    }
    
    protected Email getEmail(String body) {
        Email email = new Email();
        NameAndAddress fromNameAndAddress = toNameAndAddress(fromAddress);
        email.setFromAddress(fromNameAndAddress.getName(), fromNameAndAddress.getAddress());
        for (Recipient to : toRecipients(toAddresses, TO)) {
            to.addToEmail(email);
        }
        for (Recipient cc : toRecipients(ccAddresses, CC)) {
            cc.addToEmail(email);
        }
        for (Recipient bcc : toRecipients(bccAddresses, BCC)) {
            bcc.addToEmail(email);
        }
        email.setSubject(subject);
        email.setText(body);
        return email;
    }
}
