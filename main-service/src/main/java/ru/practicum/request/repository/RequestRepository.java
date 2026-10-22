package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
}
