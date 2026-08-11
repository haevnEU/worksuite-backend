package de.haevn.worksuite.common;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Service for securely streaming remote files while mitigating SSRF vulnerabilities.
 *
 * <p>Example usage:
 * <pre>{@code
 * URI targetUri = URI.create("https://pm.hausheld.info/exports/data.csv");
 * ResponseEntity<Resource> response = fileDownloadService.downloadWithRestClient(
 *     targetUri, "Authorization", "Bearer secret-token"
 * );
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private static final Set<String> ALLOWED_HOSTS = Set.of("pm.hausheld.info");
    private final RestClient restClient = RestClient.create();

    /**
     * Downloads a file stream from a validated remote endpoint using {@link RestClient}.
     *
     * @param uri target remote address
     * @param securityHeaderName optional authorization or custom header name
     * @param securityHeaderValue optional header value
     * @return a {@link ResponseEntity} holding the streaming {@link Resource}
     */
    public ResponseEntity<Resource> downloadWithRestClient(final URI uri, final String securityHeaderName,
        final String securityHeaderValue) {

        Objects.requireNonNull(uri, "URI must not be null");
        validateUri(uri);

        return restClient.get().uri(uri).headers(headers -> {
            if (StringUtils.hasText(securityHeaderName) && StringUtils.hasText(securityHeaderValue)) {
                headers.set(securityHeaderName, securityHeaderValue);
            }
        }).exchange((request, response) -> {
            if (response.getStatusCode().isError()) {
                throw new IllegalStateException("Remote server returned error: " + response.getStatusCode());
            }
            final Resource resource = new InputStreamResource(response.getBody());
            return ResponseEntity.ok().headers(response.getHeaders()).body(resource);
        });
    }

    /**
     * Validates that the URI scheme, host, and resolved IP addresses are safe against SSRF attacks.
     *
     * @param uri the URI to inspect
     */
    private void validateUri(final URI uri) {
        final String host = uri.getHost();
        if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase())) {
            throw new SecurityException("Access to untrusted host is denied: " + host);
        }

        try {
            final InetAddress[] addresses = InetAddress.getAllByName(host);
            for (final InetAddress addr : addresses) {
                if (addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()
                    || addr.isLinkLocalAddress()) {
                    throw new SecurityException(
                        "Access to internal or private network addresses is forbidden: " + addr);
                }
            }
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Host could not be resolved: " + host, ex);
        }
    }
}