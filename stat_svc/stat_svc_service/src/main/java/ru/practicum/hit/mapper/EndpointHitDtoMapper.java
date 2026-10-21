package ru.practicum.hit.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.hit.entity.Hit;

@Component
public class EndpointHitDtoMapper {

    public Hit toHit(EndpointHitDto endpointHitDto) {
        Hit hit = new Hit();
        hit.setApp(endpointHitDto.getApp());
        hit.setIp(endpointHitDto.getIp());
        hit.setUri(endpointHitDto.getUri());
        hit.setTimestamp(endpointHitDto.getTimestamp());

        return hit;
    }
}
