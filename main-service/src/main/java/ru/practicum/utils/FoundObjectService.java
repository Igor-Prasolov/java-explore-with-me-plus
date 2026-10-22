package ru.practicum.utils;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.entity.User;
import ru.practicum.user.repository.UserRepository;

/* тут писать паблик методы для поиска по айди и если не найден, то кидать исключение 404 и писать в лог
понадобится во многих методах и классах, что бы не плодить приватные одинаковые методы во многих сервисах
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FoundObjectService {
    // ниже пример метода, так же внедрять сюда репозитории которые нужны будут для работы, например для поиска ивента, реквеста и тд
    private final UserRepository userRepository;

    //пример - эти методы использовать в сервисах
    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }
}
