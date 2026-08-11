package de.haevn.worksuite.cheatsheet;

import java.util.Collections;
import java.util.UUID;

public final class CheatSheetMapper {

    private CheatSheetMapper() {
    }

    public static CheatSheetModel toModel(CheatSheetRequestDto dto) {
        return toModel(UUID.randomUUID(), dto);
    }

    public static CheatSheetModel toModel(UUID id, CheatSheetRequestDto dto) {
        var flags = dto.flags() == null ?
            Collections.<CheatSheetModel.FlagItem>emptyList() :
            dto.flags().stream().map(f -> new CheatSheetModel.FlagItem(f.flag(), f.description())).toList();

        var examples = dto.examples() == null ?
            Collections.<CheatSheetModel.ExampleItem>emptyList() :
            dto.examples().stream().map(e -> new CheatSheetModel.ExampleItem(e.title(), e.command())).toList();

        return CheatSheetModel.builder().id(id).category(dto.category()).subcategory(dto.subcategory())
            .title(dto.title()).language(dto.language()).level(dto.level()).syntax(dto.syntax())
            .explanation(dto.explanation()).flags(flags).examples(examples)
            .tags(dto.tags() != null ? dto.tags() : Collections.emptyList()).destructive(dto.destructive())
            .warning(dto.warning()).docUrl(dto.docUrl()).build();
    }

    public static CheatSheetResponseDto toResponseDto(CheatSheetModel model) {
        var flags = model.getFlags() == null ?
            Collections.<CheatSheetResponseDto.FlagDto>emptyList() :
            model.getFlags().stream().map(f -> new CheatSheetResponseDto.FlagDto(f.getFlag(), f.getDescription()))
                .toList();

        var examples = model.getExamples() == null ?
            Collections.<CheatSheetResponseDto.ExampleDto>emptyList() :
            model.getExamples().stream().map(e -> new CheatSheetResponseDto.ExampleDto(e.getTitle(), e.getCommand()))
                .toList();

        return CheatSheetResponseDto.builder().id(model.getId()).category(model.getCategory())
            .subcategory(model.getSubcategory()).title(model.getTitle()).language(model.getLanguage())
            .level(model.getLevel()).syntax(model.getSyntax()).explanation(model.getExplanation()).flags(flags)
            .examples(examples).tags(model.getTags()).destructive(model.isDestructive()).warning(model.getWarning())
            .docUrl(model.getDocUrl()).createdAt(model.getCreatedAt()).updatedAt(model.getUpdatedAt()).build();
    }
}