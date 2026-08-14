package de.haevn.worksuite.download;


import de.haevn.worksuite.common.RestApiController;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/download")
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping("/{type}")
    public ResponseEntity<Resource> downloadSynchronous(@PathVariable final DownloadModule type,
        @RequestBody final RequestDTO dto) {
        log.info("Downloading " + type.name());
        return downloadService.downloadSynchronous(type, dto);
    }
}
