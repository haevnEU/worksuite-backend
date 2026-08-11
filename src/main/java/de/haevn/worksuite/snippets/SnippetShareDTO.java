package de.haevn.worksuite.snippets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SnippetShareDTO(UUID id, String title, String content, List<String> tags, String language,
                              LocalDateTime createdAt) {

    public static SnippetShareDTO fromModel(final SnippetModel snippetModel) {
        return new SnippetShareDTO(snippetModel.getId(), snippetModel.getTitle(), snippetModel.getContent(),
            snippetModel.getTags(), snippetModel.getLanguage(), snippetModel.getCreatedAt());
    }

    public SnippetModel toModel() {
        final SnippetModel snippetModel = new SnippetModel();
        snippetModel.setTags(tags);
        snippetModel.setContent(content);
        snippetModel.setTitle(title);
        snippetModel.setLanguage(language);
        snippetModel.setCreatedAt(createdAt);
        snippetModel.setId(id);
        return snippetModel;
    }
}
