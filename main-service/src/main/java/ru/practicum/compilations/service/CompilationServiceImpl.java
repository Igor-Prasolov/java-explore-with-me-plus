package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.entity.Compilation;
import ru.practicum.compilations.mapper.CompilationMapper;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.event.entity.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;


import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        List<Compilation> compilations = compilationRepository.getCompilations(pinned, pageable);

       log.info("Retrieving compilations with pinned = {}, page number = {}, page size = {}",
                pinned, pageable.getPageNumber(), pageable.getPageSize());

        return compilationMapper.toCompilationDto(compilations);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findCompilationById(compId)
                .orElseThrow(() -> NotFoundException.of(Compilation.class, compId));
        log.info("Retrieving compilation with id = {}", compId);
        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating new compilation with title = {}", newCompilationDto.getTitle());

        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        // Обрабатываем список событий, если он передан
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        } else {
            compilation.setEvents(new HashSet<>());
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto) {
        log.info("Updating compilation with id = {}", compId);

        Compilation compilation = compilationRepository.findCompilationById(compId)
                .orElseThrow(() -> NotFoundException.of(Compilation.class, compId));

        if (compilationDto.getTitle() != null && !compilationDto.getTitle().isBlank()) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }

        // Обновляем список событий при необходимости
        if (compilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        }

        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw NotFoundException.of(Compilation.class, compilationId);
        }
        log.info("Deleting compilation with id = {}", compilationId);
        compilationRepository.deleteById(compilationId);
    }

}
