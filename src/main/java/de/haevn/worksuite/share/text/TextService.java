package de.haevn.worksuite.share.text;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import de.haevn.worksuite.share.PasswordValidation;
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
public class TextService {

    private final WebsocketPushService websocketPushService;
    private final TextShareRepository textShareRepository;
    private final PasswordValidation passwordValidation;

    @Transactional
    public UUID shareText(final TextShareDTO textShareDTO) {
        final TextModel textModel = textShareRepository.save(textShareDTO.toModel());
        websocketPushService.dispatch(
            new WsEvent(this.getClass(), Priority.INFO, "New Note shared: " + textModel.getTitle()));
        return textModel.getId();
    }

    @Transactional
    public String getText(final UUID id, final Optional<String> password) {
        final TextModel model = textShareRepository.findById(id).orElseThrow(NotFoundException::new);
        passwordValidation.validatePassword(model, password);
        return model.getContent();
    }

    @Transactional
    public List<TextShareDTO> getAllTextShare() {
        return textShareRepository.findAll().stream().map(TextShareDTO::fromModel).toList();
    }

    @Transactional
    public TextShareDTO updateText(final UUID id, final Optional<String> password, final TextShareDTO textShareDTO) {
        final TextModel textModel = textShareRepository.findById(id).orElseThrow(NotFoundException::new);
        passwordValidation.validatePassword(textModel, password);
        textModel.setContent(textShareDTO.content());
        textModel.setTitle(textShareDTO.title());
        textModel.setTags(textShareDTO.tags());
        textModel.setPassword(textShareDTO.password());
        final TextModel updatedModel = textShareRepository.save(textModel);
        return TextShareDTO.fromModel(updatedModel);
    }


    @Transactional
    public void deleteText(final UUID id, final Optional<String> password) {
        final TextModel model = textShareRepository.findById(id).orElseThrow(NotFoundException::new);
        passwordValidation.validatePassword(model, password);
        textShareRepository.deleteById(id);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Note deleted."));
    }
}
