package de.haevn.worksuite.share.text;

import java.time.LocalDateTime;
import java.util.List;

public record TextShareDTO(String title, String content, List<String> tags, String password, LocalDateTime createdAt) {

    public TextModel toModel() {
        final TextModel textModel = new TextModel();
        textModel.setTags(tags);
        textModel.setContent(content);
        textModel.setTitle(title);
        textModel.setPassword(password);
        textModel.setCreatedAt(createdAt);
        return textModel;
    }

    public static TextShareDTO fromModel(final TextModel textModel) {
        final boolean hasPassword = textModel.getPassword() != null && !textModel.getPassword().isEmpty();
        final String content = hasPassword ? "********" : textModel.getContent();
        final String password = hasPassword ? "<REDACTED>" : textModel.getPassword();
        return new TextShareDTO(textModel.getTitle(), content, textModel.getTags(), password, textModel.getCreatedAt());
    }
}
