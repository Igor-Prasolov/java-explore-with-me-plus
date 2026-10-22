package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.FoundObjectService;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final FoundObjectService foundService; // пример - использовать это в методах, где нужно найти пользователя или проверять его существование
}
