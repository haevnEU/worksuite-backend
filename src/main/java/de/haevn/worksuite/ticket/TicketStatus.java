package de.haevn.worksuite.ticket;

public enum TicketStatus {
    BACKLOG(18), PROGRESS(2), REVIEW(19), QA(8);

    public final long id;

    TicketStatus(final long id) {
        this.id = id;
    }
}
