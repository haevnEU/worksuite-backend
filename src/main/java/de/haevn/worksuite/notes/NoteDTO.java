package de.haevn.worksuite.notes;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteDTO(UUID id, String title, String content, String ticketId, LocalDateTime createdAt) {

    public static NoteDTO fromModel(final NoteModel noteModel) {
        return new NoteDTO(noteModel.getId(), noteModel.getTitle(), noteModel.getContent(), noteModel.getTicketId(),
            noteModel.getCreatedAt());
    }

    public NoteModel toModel() {
        final NoteModel noteModel = new NoteModel();
        noteModel.setContent(content);
        noteModel.setTitle(title);
        noteModel.setTicketId(ticketId);
        noteModel.setCreatedAt(createdAt);
        noteModel.setId(id);
        return noteModel;
    }
}
