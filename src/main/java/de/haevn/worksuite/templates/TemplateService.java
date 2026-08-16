package de.haevn.worksuite.templates;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TemplateService {

    private final WebsocketPushService websocketPushService;
    private final TemplateShareRepository templateShareRepository;

    @Transactional
    public TemplateShareDTO shareTemplate(final TemplateShareDTO templateShareDTO) {
        final Template template = templateShareRepository.save(templateShareDTO.toModel());
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Template created."));
        return TemplateShareDTO.fromModel(template);
    }

    @Transactional
    public TemplateShareDTO getTemplate(final UUID id) {
        final Template model = templateShareRepository.findById(id).orElseThrow(NotFoundException::new);
        return TemplateShareDTO.fromModel(model);
    }

    @Transactional
    public List<TemplateShareDTO> getAllTemplates(final Optional<String> tag) {
        return tag.map(
                s -> templateShareRepository.findByTagsContaining(s).stream().map(TemplateShareDTO::fromModel).toList())
            .orElseGet(() -> templateShareRepository.findAll().stream().map(TemplateShareDTO::fromModel).toList());
    }

    @Transactional
    public TemplateShareDTO updateTemplate(final UUID id, final TemplateShareDTO templateShareDTO) {
        final Template model = templateShareRepository.findById(id).orElseThrow(NotFoundException::new);

        model.setContent(templateShareDTO.content());
        model.setPlatform(templateShareDTO.platform());
        model.setTags(templateShareDTO.tags());
        model.setTitle(templateShareDTO.title());
        templateShareRepository.save(model);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Template updated."));
        return TemplateShareDTO.fromModel(model);
    }

    @Transactional
    public void deleteTemplate(final UUID id) {
        final Template model = templateShareRepository.findById(id).orElseThrow(NotFoundException::new);
        templateShareRepository.delete(model);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Template deleted."));
    }
}
