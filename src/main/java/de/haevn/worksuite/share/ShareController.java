package de.haevn.worksuite.share;

import de.haevn.worksuite.common.RestApiController;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/share")
public class ShareController {

    private final ShareService shareService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void share(@RequestParam("files") final List<MultipartFile> files) {
        shareService.share(files);
    }

    @GetMapping
    public List<FileMeta> getAllSharedFiles() {
        return shareService.getAllSharedFiles();
    }

    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable final UUID id) throws IOException {
        shareService.deleteFile(id);
    }

}
