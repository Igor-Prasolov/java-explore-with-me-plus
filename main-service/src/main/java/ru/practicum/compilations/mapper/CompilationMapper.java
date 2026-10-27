package ru.practicum.compilations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.entity.Compilation;
import ru.practicum.event.mapper.EventMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto newCompilationDto);

    CompilationDto toCompilationDto(Compilation compilation);

    List<CompilationDto> toCompilationDto(List<Compilation> compilations);
}
