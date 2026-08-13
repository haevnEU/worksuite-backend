package de.haevn.worksuite.ticket;

import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.Issue;
import de.haevn.worksuite.common.FileDownloadService;
import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.vcs.MrProtocolRequest;
import de.haevn.worksuite.vcs.VcsService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestApiController("/api/v1/ticket")
public class RedmineController {

    private final FileDownloadService fileDownloadService;
    private final RedmineService redmineService;
    private final VcsService vcsService;

    @GetMapping("")
    public List<Issue> listTickets() throws RedmineException {
        return redmineService.fetch();
    }

    @GetMapping("/{id}")
    public Issue getTicket(@PathVariable long id) throws RedmineException {
        return redmineService.getByIssuedId(id).orElseThrow();
    }

    @GetMapping("/{id}/checklist")
    public void getTicketChecklist(@PathVariable String id, @RequestParam("state") Optional<Boolean> state)
        throws RedmineException {
    }

    @PostMapping("/{id}/move-to-qs")
    public void moveToQs(@PathVariable long id, @Valid @RequestBody QaProtocolRequest data) throws RedmineException {
        redmineService.moveToQs(id, data);
    }

    @PostMapping("/{id}/merge-request")
    public void createMergeRequest(@PathVariable final long id, @RequestBody final MrProtocolRequest protocol)
        throws RedmineException {
        final String mrLink = vcsService.createMergeRequest(id, protocol);
        redmineService.addMergeRequestLink(id, mrLink);
    }

    @PostMapping("/{id}/time-entries")
    public void bookTicket(@PathVariable long id, @Valid @RequestBody LogTimeRequest request) throws RedmineException {
        redmineService.bookTicket(id, request);
    }

    @PostMapping("/{id}/comment")
    public void createComment(@PathVariable long id, @RequestBody String comment) throws RedmineException {
        redmineService.addComment(id, comment);
    }

    @PostMapping("/download/attachments")
    public Flux<DataBuffer> downloadAttachment(@RequestBody String url) throws RedmineException {
        return redmineService.downloadAttachment(url);
    }
}
