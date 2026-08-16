package de.haevn.worksuite.notes;

import java.time.LocalDateTime;
import java.util.UUID;

record NoteDTO(UUID id, String title, String content, String ticketId, LocalDateTime createdAt) {

    public static NoteDTO fromModel(final Note note) {
        return new NoteDTO(note.getId(), note.getTitle(), note.getContent(), note.getTicketId(),
            note.getCreatedAt());
    }

    public Note toModel() {
        final Note note = new Note();
        note.setContent(content);
        note.setTitle(title);
        note.setTicketId(ticketId);
        note.setCreatedAt(createdAt);
        note.setId(id);
        return note;
    }
}
