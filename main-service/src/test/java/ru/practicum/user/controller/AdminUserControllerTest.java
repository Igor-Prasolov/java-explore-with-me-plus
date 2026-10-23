package ru.practicum.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserControllerTest {

    private static final String USERS_PATH = "/admin/users";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Autowired
    AdminUserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, UserRepository userRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void deleteUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registerUserReturnsCreatedUser() throws Exception {
        postUser("ivan.petrov@practicummail.ru", "Иван Петров")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("ivan.petrov@practicummail.ru"))
                .andExpect(jsonPath("$.name").value("Иван Петров"));
    }

    @Test
    void registerUserAcceptsBoundaryValues() throws Exception {
        String shortestEmail = "a@b.co";
        String shortestName = "Ан";
        String longestName = "я".repeat(250);
        String longestEmail = longestEmail();

        postUser(shortestEmail, shortestName)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(shortestEmail))
                .andExpect(jsonPath("$.name").value(shortestName));

        postUser(longestEmail, longestName)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(longestEmail))
                .andExpect(jsonPath("$.name").value(longestName));
    }

    @ParameterizedTest
    @MethodSource("invalidUserRequests")
    void registerUserRejectsInvalidBody(String email, String name, String field) throws Exception {
        postUser(email, name)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message", containsString("Field: " + field)))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerUserRejectsEmptyBody() throws Exception {
        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message", containsString("Field: email")))
                .andExpect(jsonPath("$.message", containsString("Field: name")));
    }

    @Test
    void registerUserRejectsMissingBody() throws Exception {
        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    void registerUserRejectsDuplicateEmail() throws Exception {
        postUser("ivan.petrov@practicummail.ru", "Иван Петров")
                .andExpect(status().isCreated());

        postUser("ivan.petrov@practicummail.ru", "Пётр Иванов")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.reason").value("Integrity constraint has been violated."))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());

        mockMvc.perform(get(USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Иван Петров"));
    }

    @Test
    void getUsersReturnsEmptyListWhenNoUsers() throws Exception {
        mockMvc.perform(get(USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUsersReturnsCreatedUsers() throws Exception {
        register("first@mail.com", "First");
        register("second@mail.com", "Second");

        mockMvc.perform(get(USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("first@mail.com", "second@mail.com")));
    }

    @Test
    void getUsersFiltersByIds() throws Exception {
        UserDto first = register("first@mail.com", "First");
        register("second@mail.com", "Second");
        UserDto third = register("third@mail.com", "Third");

        mockMvc.perform(get(USERS_PATH)
                        .param("ids", first.getId().toString(), third.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("first@mail.com", "third@mail.com")));
    }

    @Test
    void getUsersReturnsEmptyListForUnknownIds() throws Exception {
        register("first@mail.com", "First");

        mockMvc.perform(get(USERS_PATH)
                        .param("ids", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUsersSkipsOffsetAndLimitsSize() throws Exception {
        List<String> emails = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            String email = "user" + index + "@mail.com";
            emails.add(email);
            register(email, "User" + index);
        }

        mockMvc.perform(get(USERS_PATH)
                        .param("from", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        String firstPage = mockMvc.perform(get(USERS_PATH)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String secondPage = mockMvc.perform(get(USERS_PATH)
                        .param("from", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        List<String> pageEmails = new ArrayList<>();
        pageEmails.addAll(emailsOf(firstPage));
        pageEmails.addAll(emailsOf(secondPage));
        assertEquals(emails.size(), pageEmails.size());
        assertTrue(pageEmails.containsAll(emails));
    }

    @Test
    void getUsersUsesDefaultPage() throws Exception {
        for (int index = 0; index < 11; index++) {
            register("page" + index + "@mail.com", "User" + index);
        }

        mockMvc.perform(get(USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));

        mockMvc.perform(get(USERS_PATH)
                        .param("from", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get(USERS_PATH)
                        .param("from", "100")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUsersLimitsFilteredIds() throws Exception {
        UserDto first = register("first@mail.com", "First");
        UserDto second = register("second@mail.com", "Second");
        UserDto third = register("third@mail.com", "Third");

        mockMvc.perform(get(USERS_PATH)
                        .param("ids", first.getId().toString(), second.getId().toString(), third.getId().toString())
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get(USERS_PATH)
                        .param("ids", first.getId().toString(), second.getId().toString(), third.getId().toString())
                        .param("from", "3")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUsersRejectsNegativeFrom() throws Exception {
        mockMvc.perform(get(USERS_PATH)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message", containsString("Field: from")));
    }

    @Test
    void getUsersRejectsNonPositiveSize() throws Exception {
        mockMvc.perform(get(USERS_PATH)
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message", containsString("Field: size")));
    }

    @Test
    void getUsersRejectsNonNumericFrom() throws Exception {
        mockMvc.perform(get(USERS_PATH)
                        .param("from", "ad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    void deleteUserRemovesExistingUser() throws Exception {
        UserDto created = register("ivan.petrov@practicummail.ru", "Иван Петров");
        register("second@mail.com", "Second");

        mockMvc.perform(delete(USERS_PATH + "/{userId}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(USERS_PATH)
                        .param("ids", created.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get(USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("second@mail.com"));
    }

    @Test
    void deleteUserReturnsNotFoundForMissingUser() throws Exception {
        mockMvc.perform(delete(USERS_PATH + "/{userId}", 555))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("User with id=555 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deleteUserReturnsNotFoundOnSecondDelete() throws Exception {
        UserDto created = register("ivan.petrov@practicummail.ru", "Иван Петров");

        mockMvc.perform(delete(USERS_PATH + "/{userId}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete(USERS_PATH + "/{userId}", created.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User with id=" + created.getId() + " was not found"));
    }

    @Test
    void deleteUserRejectsNonNumericId() throws Exception {
        mockMvc.perform(delete(USERS_PATH + "/{userId}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    private static Stream<Arguments> invalidUserRequests() {
        return Stream.of(
                Arguments.of("not-email", "Ivan", "email"),
                Arguments.of("user@mail.com", " ", "name"),
                Arguments.of("user@mail.com", "A", "name"),
                Arguments.of("user@mail.com", "я".repeat(251), "name"),
                Arguments.of("a@b.c", "Ivan", "email"),
                Arguments.of(null, "Ivan", "email"),
                Arguments.of("user@mail.com", null, "name"),
                Arguments.of("a".repeat(255), "Ivan", "email")
        );
    }

    private static String longestEmail() {
        String localPart = "a".repeat(64);
        String domain = "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(58) + ".co";
        return localPart + "@" + domain;
    }

    private UserDto register(String email, String name) throws Exception {
        String body = postUser(email, name)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(body, UserDto.class);
    }

    private ResultActions postUser(String email, String name) throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail(email);
        request.setName(name);
        return mockMvc.perform(post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));
    }

    private List<String> emailsOf(String body) throws Exception {
        List<UserDto> users = objectMapper.readValue(body, new TypeReference<>() {
        });
        return users.stream()
                .map(UserDto::getEmail)
                .toList();
    }
}
