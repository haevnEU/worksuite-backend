package de.haevn.worksuite.push;

import de.haevn.worksuite.push.events.WsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Asynchronous Spring application event listener that routes {@link WsEvent} events to {@link WebsocketPushService}.
 *
 * <p>Prevents blocking HTTP request execution by running the dispatching process on a background task executor.
 *
 * <p>Example application event publishing:
 * <pre>{@code
 * @Autowired
 * private ApplicationEventPublisher eventPublisher;
 *
 * eventPublisher.publishEvent(new WsEvent(NoteService.class, Priority.INFO, "Note updated"));
 * }</pre>
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class WsEventListener {

    private final WebsocketPushService pushService;

    /**
     * Intercepts published {@link WsEvent} instances and delegates them asynchronously to the push service.
     *
     * @param event the dispatched WebSocket event
     */
    @Async
    @EventListener
    public void handleWsEvent(@NonNull final WsEvent event) {
        log.info("Handling event from source: '{}' with priority: '{}'", event.source(), event.priority());
        pushService.dispatch(event);
    }
}