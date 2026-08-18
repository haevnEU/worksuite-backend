package de.haevn.worksuite.vcs;

import de.haevn.worksuite.vcs.provider.VcsProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VcsProviderRegistry {
    private final Map<de.haevn.worksuite.vcs.VcsProvider, VcsProvider> vcsProviders =
        new EnumMap<>(de.haevn.worksuite.vcs.VcsProvider.class);

    public VcsProviderRegistry(final List<VcsProvider> vcsProviderList) {
        for (final VcsProvider vcsProvider : vcsProviderList) {
            final de.haevn.worksuite.vcs.VcsProvider provider = vcsProvider.getProvider();
            if (this.vcsProviders.containsKey(provider)) {
                throw new IllegalStateException("Duplicate VCS provider for provider: " + provider);
            }
            this.vcsProviders.put(provider, vcsProvider);
            log.info("Registered VCS provider for provider: {}", provider);
        }
    }

    public VcsProvider getVcsService(final de.haevn.worksuite.vcs.VcsProvider providerType) {
        final VcsProvider provider = vcsProviders.get(providerType);
        if (provider == null) {
            throw new IllegalArgumentException("No VCS provider found for type: " + providerType);
        }
        return provider;
    }

    public Set<de.haevn.worksuite.vcs.VcsProvider> getRegisteredProvider() {
        return vcsProviders.keySet();
    }
}
