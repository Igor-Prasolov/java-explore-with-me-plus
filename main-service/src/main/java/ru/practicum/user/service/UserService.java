package ru.practicum.user.service;


import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest userToCreate);
    List<UserDto> getUsers(List<Long> ids, int from, int size);
    UserDto getUserById(long id);
    void deleteUser(long userId);
}
