package de.haevn.worksuite.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.haevn.worksuite.ticket.TicketProviderType;
import de.haevn.worksuite.vcs.VcsProvider;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserPreference(
    GeneralPreference generalPreference,
    GitPreference gitPreference,
    TicketPreference ticketPreference
) {
    public static UserPreference defaultPreferences() {
        return new UserPreference(
            new GeneralPreference("dark", "en"),
            new GitPreference("", VcsProvider.GITHUB.name(), List.of()),
            new TicketPreference("", TicketProviderType.REDMINE.name())
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeneralPreference(String theme, String language) {
        public GeneralPreference() {
            this("dark", "en");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GitPreference(String vcsToken, String provider, List<Long> watchedRepos) {
        public GitPreference() {
            this("", VcsProvider.GITHUB.name(), List.of());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TicketPreference(String ticketToken, String provider) {
        public TicketPreference() {
            this("", TicketProviderType.REDMINE.name());
        }
    }
}