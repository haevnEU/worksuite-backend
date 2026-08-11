package de.haevn.worksuite.share;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.share.file.FileModel;
import de.haevn.worksuite.share.file.FileService;
import de.haevn.worksuite.share.file.FileShareDTO;
import de.haevn.worksuite.share.text.TextService;
import de.haevn.worksuite.share.text.TextShareDTO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/share")
public class ShareController {

    private final FileService fileService;
    private final TextService textService;

    // -----------------------------------------------------------------------------------------------------------------
    // Text Share Endpoints
    // -----------------------------------------------------------------------------------------------------------------

    @PostMapping("/text")
    public String shareText(@RequestBody final TextShareDTO textShareDTO) {
        log.info("Sharing text");
        return textService.shareText(textShareDTO).toString();
    }

    @PutMapping("/text/{id}")
    public TextShareDTO updateText(@PathVariable final UUID id, @RequestHeader final Optional<String> password,
        @RequestBody final TextShareDTO textShareDTO) {
        log.info("Updating text");
        return textService.updateText(id, password, textShareDTO);
    }

    @GetMapping("/text")
    public List<TextShareDTO> getAllTextShares() {
        log.info("Getting all text");
        return textService.getAllTextShare();
    }

    @GetMapping("/text/{id}")
    public String getText(@PathVariable final UUID id, @RequestHeader final Optional<String> password) {
        log.info("Getting text with id: {}", id);
        return textService.getText(id, password);
    }

    @DeleteMapping("/text/{id}")
    public ResponseEntity<Void> deleteText(@PathVariable final UUID id,
        @RequestHeader final Optional<String> password) {
        log.info("Deleting text with id: {}", id);
        textService.deleteText(id, password);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // File Share Endpoints
    // -----------------------------------------------------------------------------------------------------------------

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UUID> uploadFile(@RequestPart("file") MultipartFile file,
        @RequestParam(value = "metadata", required = false) String metadataJson) throws IOException {
        final ObjectMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
        FileShareDTO metadata = null;
        if (metadataJson != null && !metadataJson.isBlank()) {
            metadata = mapper.readValue(metadataJson, FileShareDTO.class);
        }

        final UUID id = fileService.createFileShare(file, metadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/file")
    public List<FileShareDTO> getAllFileShares() {
        log.info("Getting all file shares");
        return fileService.getAllFileShares();
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable final UUID id, @RequestHeader final Optional<String> password)
        throws IOException {
        log.info("Getting text with id: {}", id);
        final FileModel metadata = fileService.getMetadata(id).orElseThrow();
        final Resource resource = fileService.getFile(id, password);

        final ContentDisposition contentDisposition =
            ContentDisposition.builder("attachment").filename(metadata.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.parseMediaType(metadata.getContentType())).body(resource);
    }

    @DeleteMapping("/file/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable final UUID id, @RequestHeader final Optional<String> password)
        throws IOException {
        log.info("Deleting file with id: {}", id);
        fileService.deleteFile(id, password);
        return ResponseEntity.noContent().build();
    }
}
