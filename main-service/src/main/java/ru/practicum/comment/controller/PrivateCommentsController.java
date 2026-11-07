package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.FullCommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateCommentsController {
    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    public List<FullCommentDto> findCommentsByEventId(@PathVariable Long userId,
                                                      @PathVariable Long eventId) {
        log.info("call findCommentsByEventId from the controller");

        return commentService.findCommentsByEventId(eventId, userId);
    }

    @GetMapping("/comments")
    public List<FullCommentDto> findCommentsByAuthorId(@PathVariable Long userId) {
        log.info("call findCommentsByAuthorId from the controller");

        return commentService.findCommentsByAuthorId(userId);
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public FullCommentDto createComment(@PathVariable Long eventId,
                                        @PathVariable Long userId,
                                        @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("call createComment from the controller");

        return commentService.createComment(newCommentDto, userId, eventId);
    }

    @PatchMapping("/comments/{commentId}")
    public FullCommentDto updateComment(@PathVariable Long commentId,
                                        @PathVariable Long userId,
                                        @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("call updateComment from the controller");

        return commentService.updateComment(newCommentDto, userId, commentId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable Long commentId,
                       @PathVariable Long userId) {
        log.info("call deleteComment from the controller");
        commentService.deleteCommentUser(commentId, userId);
    }
}
