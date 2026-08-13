package de.haevn.worksuite.vcs;

import java.util.List;

public record GitLabRepository(long number, String webUrl, String name, String lastPipelineStatus, String path,
                               int openMRCount, List<MergeRequestDto> mergeRequests) {
}
