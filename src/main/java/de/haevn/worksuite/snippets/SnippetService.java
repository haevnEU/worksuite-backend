package de.haevn.worksuite.snippets;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class SnippetService {

    private final WebsocketPushService websocketPushService;
    private final SnippetShareRepository snippetShareRepository;

    @Transactional
    public SnippetShareDTO shareSnippet(final SnippetShareDTO snippetShareDTO) {
        final Snippet snippet = snippetShareRepository.save(snippetShareDTO.toModel());
        websocketPushService.dispatch(
            new WsEvent(this.getClass(), Priority.INFO, "Snippet shared: " + snippet.getId()));
        return SnippetShareDTO.fromModel(snippet);
    }

    @Transactional
    public SnippetShareDTO getSnippet(final UUID id) {
        final Snippet model = snippetShareRepository.findById(id).orElseThrow(NotFoundException::new);
        return SnippetShareDTO.fromModel(model);
    }

    @Transactional
    public List<SnippetShareDTO> getAllSnippets() {
        return snippetShareRepository.findAll().stream().map(SnippetShareDTO::fromModel).toList();
    }

    @Transactional
    public SnippetShareDTO updateSnippet(final UUID id, final SnippetShareDTO snippetShareDTO) {
        final Snippet model = snippetShareRepository.findById(id).orElseThrow(NotFoundException::new);
        model.setContent(snippetShareDTO.content());
        model.setLanguage(snippetShareDTO.language());
        model.setTags(snippetShareDTO.tags());
        model.setTitle(snippetShareDTO.title());
        snippetShareRepository.save(model);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Snippet updated."));
        return SnippetShareDTO.fromModel(model);
    }

    @Transactional
    public void deleteSnippet(final UUID id) {
        final Snippet model = snippetShareRepository.findById(id).orElseThrow(NotFoundException::new);
        snippetShareRepository.delete(model);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Snippet deleted."));
    }
}
