package de.haevn.worksuite.common;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Log4j2
@RequiredArgsConstructor
@Service
public class FileDownloadService {

    private static final String ALLOWED_HOST = "pm.hausheld.info";

    private final WebClient unlimitedWebClient;

    public Flux<DataBuffer> download(final URI uri, final String filename) {
        return download(uri, null, null);
    }

    public Flux<DataBuffer> download(final URI uri, final String securityHeaderName, final String securityHeaderValue) {
        Objects.requireNonNull(uri, "URI darf nicht null sein.");
        //Objects.requireNonNull(filename, "Filename darf nicht null sein.");

        validateUri(uri);

        return unlimitedWebClient.get().uri(uri).headers(headers -> {
            if (StringUtils.hasText(securityHeaderValue) && StringUtils.hasText(securityHeaderName)) {
                headers.set(securityHeaderName, securityHeaderValue);
            }
        }).retrieve().bodyToFlux(DataBuffer.class).doOnNext(this::inspectContent);
    }

    private void validateUri(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI darf nicht null sein.");
        }

        //if (!"https".equalsIgnoreCase(uri.getScheme())) {
        //    throw new SecurityException("Nur HTTPS-Verbindungen sind erlaubt!");
        //}

        final String host = uri.getHost();
        if (host == null || !host.equalsIgnoreCase(ALLOWED_HOST)) {
            throw new SecurityException("Zugriff auf diese Adresse ist nicht gestattet: " + host);
        }

        try {
            final InetAddress[] addresses = InetAddress.getAllByName(host);
            for (final InetAddress addr : addresses) {
                if (addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                    throw new SecurityException("Zugriff auf interne IP-Adressen ist verboten!");
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Host konnte nicht aufgelöst werden: " + host, e);
        }
    }

    private void inspectContent(DataBuffer buffer) {
        // TODO: Spätere Prüfung (z. B. Magic Bytes, Hash, Scan)
    }
}