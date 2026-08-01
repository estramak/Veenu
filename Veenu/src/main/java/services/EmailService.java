package services;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import model.Business;
import model.Listing;
import model.Note;
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

    public void sendBusinessChangesRequestedEmail(Business business, String reason, String recipientEmail) {
        String subject = "Changes requested for your business listing";
        String html = """
                <p>Hi,</p>
                <p>Your business listing <strong>%s</strong> requires changes before it can remain active.</p>
                <p><strong>Requested changes:</strong> %s</p>
                <p>Please log in and update your listing so it can be reviewed again.</p>
                """.formatted(business.getListing().getName(), reason);

        send(recipientEmail, subject, html);
    }

    public void sendBusinessPendingEmail(Business business, String reason, String recipientEmail) {
        String subject = "Changes Currently Pending";
        String html = """
                <p>Hi,</p>
                <p>The changes submitted for your business listing 
                <strong>%s</strong> have been received, and your business 
                is pending reinstatement. Please watch for any additional 
                emails coming your way."</p>
                """.formatted(business.getListing().getName());

        send(recipientEmail, subject, html);
    }

    public void sendBusinessTakenDownEmail(Business business, String reason, String recipientEmail) {
        String subject = "Your business has been permanently taken down";
        String html = """
            <p>Hi,</p>
            <p>Your business listing <strong>%s</strong> has been permanently 
            taken down due to a violation and cannot be reinstated. It will 
            no longer be shown to other users on Veenu.</p>
            <p><strong>Reason:</strong> %s</p>
            """.formatted(business.getListing().getName(), reason != null ? reason : "Not specified");

        send(recipientEmail, subject, html);
    }

    public void sendBusinessOverrideTakeDownEmail(Business business, String recipientEmail) {
        String subject = "Your business listing has been reinstated";
        String html = """
               <p>Hi,</p>
               <p>Your business listing <strong>%s</strong> has been reinstated and is now visible to other users.</p>
               """.formatted(business.getListing().getName());

        send(recipientEmail, subject, html);
    }

    public void sendBusinessApprovedEmail(Business business, String recipientEmail) {
        String subject = "Your business listing is active";
        String html = """
                <p>Hi,</p>
                <p>Your business listing <strong>%s</strong> has been reviewed and is now active.</p>
                """.formatted(business.getListing().getName());

        send(recipientEmail, subject, html);
    }

    public void sendBusinessRemovedEmail(Business business, String reason, String emailRecipient) {
        String subject = "Your business has been removed";
        String html = """
            <p>Hi,</p>
            <p>Your business listing <strong>%s</strong> has been removed from Veenu.</p>
            <p><strong>Reason:</strong> %s</p>
            <p>If you believe this was a mistake, please contact support.</p>
            """.formatted(business.getListing().getName(), reason != null ? reason : "Not specified");

        send(emailRecipient, subject, html);
    }

    // ---------- Listing emails ----------
    public void sendListingChangesRequestedEmail(Listing listing, String reason, String recipientEmail) {
        String subject = "Help us update " + listing.getName();
        String html = """
            <p>Hi,</p>
            <p>You added <strong>%s</strong> to Veenu a while back, thank you for that!</p>
            <p>We had to take it down for now. Here's what we think might be outdated or incorrect:</p>
            <p><em> %s</em></p>
            <p>If you know this spot well, we'd love your help getting the details right again.
            Just reply to this email or update the listing once you're able, and we'll get it back up.</p>
            """.formatted(listing.getName(), reason != null ? reason : "some details may need a second look");

        send(recipientEmail, subject, html);
    }

    public void sendListingPendingReviewEmail(Listing listing, String recipientEmail) {
        String subject = "Thanks — we're reviewing your update to " + listing.getName();
        String html = """
            <p>Hi,</p>
            <p>Thanks for updating <strong>%s</strong>. We've received your changes and
            will take a look shortly.</p>
            <p>We'll let you know once it's back live.</p>
            """.formatted(listing.getName());

        send(recipientEmail, subject, html);
    }

    public void sendListingTakenDownEmail(Listing listing, String reason, String recipientEmail) {
        String subject = listing.getName() + " has been taken down";
        String html = """
            <p>Hi,</p>
            <p>You added <strong>%s</strong> to Veenu a while back, thank you for that.</p>
            <p>Unfortunately we've had to take it down permanently.</p>
            <p><strong>Reason:</strong> %s</p>
            """.formatted(listing.getName(), reason != null ? reason : "Not specified");

        send(recipientEmail, subject, html);
    }

    public void sendListingOverrideTakeDownEmail(Listing listing, String recipientEmail) {
        String subject = listing.getName() + " is back on the map";
        String html = """
            <p>Hi,</p>
            <p>Good news! After taking another look, we've reinstated <strong>%s</strong>.
            It's live on the map again.</p>
            <p>Thanks for your patience, and for sharing this spot with the community.</p>
            """.formatted(listing.getName());

        send(recipientEmail, subject, html);
    }

    public void sendListingApprovedEmail(Listing listing, String recipientEmail) {
        String subject = listing.getName() + " is live again";
        String html = """
            <p>Hi,</p>
            <p><strong>%s</strong> has been reviewed and is now active on the map.</p>
            <p>Thanks for helping keep it accurate for the community.</p>
            """.formatted(listing.getName());

        send(recipientEmail, subject, html);
    }

    public void sendListingRemovedEmail(Listing listing, String reason, String recipientEmail) {
        String subject = listing.getName() + " has been removed";
        String html = """
            <p>Hi,</p>
            <p>You added <strong>%s</strong> to Veenu a while back, thank you for that.</p>
            <p>It looks like this spot may no longer be around, so we've removed it from the map.</p>
            <p><strong>Note:</strong> %s</p>
            <p>If that's not right and it's still open, let us know and we'll get it back up.</p>
            """.formatted(listing.getName(), reason != null ? reason : "Not specified");

        send(recipientEmail, subject, html);
    }

    // ---------- User emails ----------

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

    public void sendUserPendingReviewEmail(User user) {
        String subject = "Thanks! we're reviewing your account update";
        String html = """
            <p>Hi %s,</p>
            <p>Thank you for updating your account. We've received your changes and
            will take a look shortly.</p>
            <p>We'll let you know once your account is active again.</p>
            """.formatted(user.getDisplayName());

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

    public void sendUserTakenDownEmail(User user, String reason) {
        String subject = "Your account has been permanently taken down";
        String html = """
            <p>Hi %s,</p>
            <p>Your account has been permanently taken down due to a violation and cannot be reinstated.</p>
            <p><strong>Reason:</strong> %s</p>
            """.formatted(user.getDisplayName(), reason != null ? reason : "Not specified");

        send(user.getEmail(), subject, html);
    }

    public void sendUserOverrideTakeDownEmail(User user) {
        String subject = "Your account has been reinstated";
        String html = """
            <p>Hi %s,</p>
            <p>After reviewing your account again, we've reinstated it. You're welcome
            back on Veenu.</p>
            """.formatted(user.getDisplayName());

        send(user.getEmail(), subject, html);
    }

    // ---------- Note emails ----------
    public void sendNoteTakenDownEmail(User author, Note note, String reason) {
        String subject = "Your note has been permanently taken down";
        String preview = note.getContent().length() > 100
                ? note.getContent().substring(0, 100) + "…"
                : note.getContent();

        String html = """
            <p>Hi %s,</p>
            <p>A note you posted has been permanently taken down due to a violation
            and cannot be reinstated.</p>
            <p><strong>Your note:</strong> <em>"%s"</em></p>
            <p><strong>Reason:</strong> %s</p>
            """.formatted(author.getDisplayName(), preview, reason != null ? reason : "Not specified");

        send(author.getEmail(), subject, html);
    }

    public void sendNoteOverrideTakeDownEmail(User author, Note note) {
        String subject = "Your note has been reinstated";
        String preview = note.getContent().length() > 100
                ? note.getContent().substring(0, 100) + "…"
                : note.getContent();

        String html = """
            <p>Hi %s,</p>
            <p>After reviewing again, we've reinstated your note:</p>
            <p><em>"%s"</em></p>
            """.formatted(author.getDisplayName(), preview);

        send(author.getEmail(), subject, html);
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
