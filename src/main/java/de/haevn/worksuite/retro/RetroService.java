package de.haevn.worksuite.retro;

import de.haevn.worksuite.common.PdfService;
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
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Log4j2
public class RetroService {
    private final ObjectMapper objectMapper;
    private final WebsocketPushService websocketPushService;
    private final RetroRepository retroRepository;
    private final PdfService pdfService;

    @Transactional
    public RetroDTO createRetro(final String name) {
        RetroModel retro = new RetroModel();
        retro.setSprintName(name);
        final RetroModel retroModel = retroRepository.save(retro);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Retro created: " + name));
        return RetroDTO.fromModel(retroModel);
    }

    @Transactional
    public RetroDTO getRetroWeek(final UUID id) {
        final RetroModel model = retroRepository.findById(id).orElseThrow(NotFoundException::new);
        return RetroDTO.fromModel(model);
    }

    @Transactional
    public void addToPositiveList(final UUID retroId, final String item) {
        final RetroModel model = retroRepository.findById(retroId).orElseThrow(NotFoundException::new);
        if (model.getPositive().contains(item)) {
            return;
        }
        model.getPositive().add(item);
        retroRepository.save(model);
    }

    @Transactional
    public void addToNegativeList(final UUID retroId, final String item) {
        final RetroModel model = retroRepository.findById(retroId).orElseThrow(NotFoundException::new);
        if (model.getNegative().contains(item)) {
            return;
        }
        model.getNegative().add(item);
        retroRepository.save(model);
    }

    @Transactional
    public void addToActionItemList(final UUID retroId, final String item) {
        final RetroModel model = retroRepository.findById(retroId).orElseThrow(NotFoundException::new);
        if (model.getActionItems().contains(item)) {
            return;
        }
        model.getActionItems().add(item);
        retroRepository.save(model);
    }

    @Transactional
    public void removeFromPositiveList(final UUID retroId, final String item) {
        final RetroModel model = retroRepository.findById(retroId).orElseThrow(NotFoundException::new);
        model.getPositive().remove(item);
        retroRepository.save(model);
    }

    @Transactional
    public void removeFromNegativeList(final UUID retroId, final String item) {
        final RetroModel model = retroRepository.findById(retroId).orElseThrow(NotFoundException::new);
        model.getNegative().remove(item);
        retroRepository.save(model);
    }

    @Transactional
    public void removeFromActionItemList(final UUID retroId, final String item) {
        final RetroModel model = retroRepository.findById(retroId).orElseThrow(NotFoundException::new);
        model.getActionItems().remove(item);
        retroRepository.save(model);
    }

    @Transactional
    public List<RetroDTO> getAllRetros() {
        return retroRepository.findAll().stream().map(RetroDTO::fromModel).toList();
    }

    @Transactional
    public void deleteRetro(final UUID retroId) {
        retroRepository.deleteById(retroId);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Retro deleted."));
    }
}
