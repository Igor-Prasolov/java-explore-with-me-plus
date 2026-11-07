package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/comments/{commentId}")
public class AdminCommentsController {
    private final CommentService commentService;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCommentAdm(@PathVariable Long commentId) {
        log.info("call deleteCommentAdm from the controller");
        commentService.deleteCommentAdmin(commentId);
    }
}
