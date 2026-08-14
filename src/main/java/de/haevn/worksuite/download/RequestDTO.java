package de.haevn.worksuite.download;

public record RequestDTO(String apiKey, String filename, String id, Boolean isDraft, String url, String webUrl) {
}
