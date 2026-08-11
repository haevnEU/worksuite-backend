package de.haevn.worksuite.push;
import de.haevn.worksuite.push.events.WsEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class WsEventListener {

    private final WebsocketPushService pushService;

    public WsEventListener(final WebsocketPushService pushService) {
        this.pushService = pushService;
    }

    @Async // Verhindert, dass der Aufrufer auf den WebSocket-Versand warten muss
    @EventListener
    public void handleWsEvent(final WsEvent event) {
        log.info("Handling Event from [{}] with Priority [{}]", event.source(), event.priority());
        pushService.dispatch(event);
    }
}