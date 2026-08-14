package de.haevn.worksuite.download;

public record RequestDTO(String apiKey, String filename, String id, boolean isDraft, String url, String webUrl) {
}
