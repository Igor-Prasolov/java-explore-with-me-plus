package ru.practicum.comment.service;

import ru.practicum.comment.dto.FullCommentDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    FullCommentDto createComment(NewCommentDto newCommentDto, Long userId, Long eventId);

    FullCommentDto updateComment(NewCommentDto newCommentDto, Long userId, Long commentId);

    List<FullCommentDto> findCommentsByEventId(Long eventId, Long userId);

    List<FullCommentDto> findCommentsByAuthorId(Long authorId);

    void deleteCommentUser(Long commentId, Long userId);

    void deleteCommentAdmin(Long commentId);


}
