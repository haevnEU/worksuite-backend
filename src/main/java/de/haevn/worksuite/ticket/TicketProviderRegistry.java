package de.haevn.worksuite.ticket;

import de.haevn.worksuite.ticket.provider.TicketProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TicketProviderRegistry {
    private final Map<TicketProviderType, TicketProvider> ticketProviders = new EnumMap<>(TicketProviderType.class);

    public TicketProviderRegistry(final List<TicketProvider> ticketProviderList) {
        for (final TicketProvider ticketProvider : ticketProviderList) {
            final TicketProviderType provider = ticketProvider.getProviderType();
            if (this.ticketProviders.containsKey(provider)) {
                throw new IllegalStateException("Duplicate ticket provider for provider: " + provider);
            }
            this.ticketProviders.put(provider, ticketProvider);
            log.info("Registered ticket provider for provider: {}", provider);
        }
    }

    public TicketProvider getTicketProvider(final TicketProviderType providerType) {
        final TicketProvider provider = ticketProviders.get(providerType);
        if (provider == null) {
            throw new IllegalArgumentException("No ticket provider found for type: " + providerType);
        }
        return provider;
    }

    public Set<TicketProviderType> getRegisteredProviders() {
        return ticketProviders.keySet();
    }
}
