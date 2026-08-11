package de.haevn.worksuite.weekly;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DaySummaryDTO(UUID id, Instant date, String summary, List<String> tasks, Instant createdAt) {
    public static DaySummaryDTO fromModel(final DaySummary model) {
        if (model == null) {
            return null;
        }
        return new DaySummaryDTO(model.getId(), model.getDate(), model.getSummary(),
            model.getTasks() != null ? List.copyOf(model.getTasks()) : List.of(), model.getCreatedAt());
    }

    public DaySummary toModel() {
        final DaySummary model = new DaySummary();
        model.setId(this.id());
        model.setDate(this.date());
        model.setSummary(this.summary());
        if (this.tasks() != null) {
            model.setTasks(this.tasks());
        }
        model.setCreatedAt(this.createdAt());
        return model;
    }
}