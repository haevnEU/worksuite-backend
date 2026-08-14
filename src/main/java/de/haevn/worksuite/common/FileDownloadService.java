package de.haevn.worksuite.common;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Log4j2
@RequiredArgsConstructor
@Service
public class FileDownloadService {

    private static final String ALLOWED_HOST = "pm.hausheld.info";

    public ResponseEntity<Resource> downloadWithRestClient(final URI uri, final String securityHeaderName,
        final String securityHeaderValue) {

        Objects.requireNonNull(uri, "URI darf nicht null sein.");
        validateUri(uri);
        final RestClient restClient = RestClient.create();
        return restClient.get().uri(uri).headers(headers -> {
            if (StringUtils.hasText(securityHeaderName) && StringUtils.hasText(securityHeaderValue)) {
                headers.set(securityHeaderName, securityHeaderValue);
            }
        }).exchange((request, response) -> {
            if (response.getStatusCode().isError()) {
                throw new IllegalStateException("Remote Error: " + response.getStatusCode());
            }
            final Resource resource = new InputStreamResource(response.getBody());
            return ResponseEntity.ok().headers(response.getHeaders()).body(resource);
        });
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
}