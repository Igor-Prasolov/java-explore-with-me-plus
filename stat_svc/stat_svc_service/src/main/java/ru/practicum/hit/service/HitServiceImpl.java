package ru.practicum.hit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.hit.mapper.EndpointHitDtoMapper;
import ru.practicum.hit.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;
    private final EndpointHitDtoMapper endpointHitDtoMapper;


    @Override
    public List<ViewStatsDto> findStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            return hitRepository.findUniqueStats(start, end, uris);
        }

        return hitRepository.findStats(start, end, uris);
    }

    @Override
    public void createHit(EndpointHitDto endpointHitDto) {
        hitRepository.save(endpointHitDtoMapper.toHit(endpointHitDto));
    }
}
