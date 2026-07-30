package services;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import model.Business;
import model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resendClient;
    private final String fromAddress;

    public EmailService(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.address:Veenu <notifications@send.veenu.app>}") String fromAddress
    ) {
        this.resendClient = new Resend(apiKey);
        this.fromAddress = fromAddress;
    }

    // ---------- Business emails ----------

    public void sendBusinessSuspensionEmail(Business business, String reason) {
        String subject = "Your business listing has been suspended";
        String html = """
                <p>Hi,</p>
                <p>Your business listing <strong>%s</strong> has been suspended.</p>
                <p><strong>Reason:</strong> %s</p>
                <p>If you believe this was a mistake, please contact support.</p>
                """.formatted(business.getListing().getName(), reason);

        send(business.getEmail(), subject, html);
    }

    public void sendBusinessChangesRequestedEmail(Business business, String reason) {
        String subject = "Changes requested for your business listing";
        String html = """
                <p>Hi,</p>
                <p>Your business listing <strong>%s</strong> requires changes before it can remain active.</p>
                <p><strong>Requested changes:</strong> %s</p>
                <p>Please log in and update your listing so it can be reviewed again.</p>
                """.formatted(business.getListing().getName(), reason);

        send(business.getEmail(), subject, html);
    }

    public void sendBusinessApprovedEmail(Business business) {
        String subject = "Your business listing is active";
        String html = """
                <p>Hi,</p>
                <p>Your business listing <strong>%s</strong> has been reviewed and is now active.</p>
                """.formatted(business.getListing().getName());

        send(business.getEmail(), subject, html);
    }

    // ---------- User emails ----------

    public void sendUserSuspensionEmail(User user, String reason) {
        String subject = "Your account has been suspended";
        String html = """
                <p>Hi %s,</p>
                <p>Your account has been suspended.</p>
                <p><strong>Reason:</strong> %s</p>
                """.formatted(user.getDisplayName(), reason);

        send(user.getEmail(), subject, html);
    }

    public void sendUserChangesRequestedEmail(User user, String reason) {
        String subject = "Changes requested for your account";
        String html = """
                <p>Hi %s,</p>
                <p>Your account requires changes before it can remain active.</p>
                <p><strong>Requested changes:</strong> %s</p>
                <p>Please log in and update your profile so it can be reviewed again.</p>
                """.formatted(user.getDisplayName(), reason);

        send(user.getEmail(), subject, html);
    }

    public void sendUserApprovedEmail(User user) {
        String subject = "Your account is active";
        String html = """
                <p>Hi %s,</p>
                <p>Your account has been reviewed and is now active.</p>
                """.formatted(user.getDisplayName());

        send(user.getEmail(), subject, html);
    }

    public void sendUserBannedEmail(User user, String reason) {
        String subject = "Your account has been permanently banned";
        String html = """
                <p>Hi %s,</p>
                <p>Your account has been permanently banned and cannot be reinstated.</p>
                <p><strong>Reason:</strong> %s</p>
                """.formatted(user.getDisplayName(), reason != null ? reason : "Not specified");

        send(user.getEmail(), subject, html);
    }

    // ---------- Core send ----------

    private void send(String to, String subject, String html) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(to)
                .subject(subject)
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resendClient.emails().send(params);
            // TODO: replace with a real logger (SLF4J) once confirmed working
            System.out.println("Email sent, id=" + response.getId());
        } catch (Exception e) {
            // TODO: replace with real logger. Email failures should NOT
            // block the suspend/approve/ban action itself — the DB state
            // change already succeeded by the time this is called, so a
            // failed email is logged, not thrown.
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
