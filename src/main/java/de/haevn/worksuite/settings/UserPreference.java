package de.haevn.worksuite.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.haevn.worksuite.ticket.TicketProviderType;
import de.haevn.worksuite.vcs.VcsProvider;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreference {
    private final GeneralPreference generalPreference;
    private final GitPreference gitPreference;
    private final TicketPreference ticketPreference;

    @Data
    public static class GeneralPreference {
        private String theme = "dark";
        private String language = "en";
    }



    @Data
    public static class GitPreference {
        private String vcsToken = "";
        private String provider = VcsProvider.GITHUB.name();
        private List<Long> watchedRepos = List.of();
    }

    @Data
    public static class TicketPreference {
        private String ticketToken = "";
        private String provider = TicketProviderType.REDMINE.name();
    }

    public static UserPreference defaultPreferences() {
        return new UserPreference(new GeneralPreference(), new GitPreference(), new TicketPreference());
    }
}
