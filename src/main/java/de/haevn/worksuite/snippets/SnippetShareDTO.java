package de.haevn.worksuite.snippets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SnippetShareDTO(UUID id, String title, String content, List<String> tags, String language,
                              LocalDateTime createdAt) {

    public static SnippetShareDTO fromModel(final Snippet snippet) {
        return new SnippetShareDTO(snippet.getId(), snippet.getTitle(), snippet.getContent(),
            snippet.getTags(), snippet.getLanguage(), snippet.getCreatedAt());
    }

    public Snippet toModel() {
        final Snippet snippet = new Snippet();
        snippet.setTags(tags);
        snippet.setContent(content);
        snippet.setTitle(title);
        snippet.setLanguage(language);
        snippet.setCreatedAt(createdAt);
        snippet.setId(id);
        return snippet;
    }
}
