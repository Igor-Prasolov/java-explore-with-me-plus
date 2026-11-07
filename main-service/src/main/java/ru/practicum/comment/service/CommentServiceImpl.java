package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.FullCommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.entity.Comment;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.entity.Event;
import ru.practicum.event.entity.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.entity.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    @Override
    public List<FullCommentDto> findCommentsByEventId(Long eventId, Long userId) {
        findEventById(eventId);
        findUserById(userId);

        List<Comment> commentList = commentRepository.findByEventId(eventId);

        return commentList.stream()
                .map(commentMapper::toFullCommentDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<FullCommentDto> findCommentsByAuthorId(Long authorId) {
        findUserById(authorId);

        List<Comment> commentList = commentRepository.findByAuthorId(authorId);

        return commentList.stream()
                .map(commentMapper::toFullCommentDto)
                .toList();
    }

    @Override
    public FullCommentDto createComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = findUserById(userId);
        Event event = findEventById(eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            log.warn("WARNING createComment: can only leave comments on published events");
            throw new NotFoundException("You can only leave comments on published events");
        }

        Comment comment = commentMapper.toComment(newCommentDto);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setEvent(event);
        commentRepository.save(comment);

        return commentMapper.toFullCommentDto(comment);
    }

    @Override
    public FullCommentDto updateComment(NewCommentDto newCommentDto, Long userId, Long commentId) {
        User user = findUserById(userId);
        Comment comment = findCommentById(commentId);

        if (!user.getId().equals(comment.getAuthor().getId())) {
            log.warn("WARNING updateComment: only the author can edit.");
            throw new ForbiddenException("Only the author can edit.");
        }
        comment.setText(newCommentDto.getText());
        commentRepository.save(comment);

        return commentMapper.toFullCommentDto(comment);
    }

    @Override
    public void deleteCommentUser(Long commentId, Long userId) {
        Comment comment = findCommentById(commentId);
        findUserById(userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            log.warn("WARNING deleteComment: only the author can delete.");
            throw new ForbiddenException("Only the author can delete.");
        }
        commentRepository.delete(comment);
    }

    @Override
    public void deleteCommentAdmin(Long commentId) {
        Comment comment = findCommentById(commentId);
        commentRepository.delete(comment);
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("WARNING commentRepository: comment not found");
                    return NotFoundException.of(Comment.class, commentId);
                });
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("WARNING userRepository: user not found");
                    return NotFoundException.of(User.class, userId);
                });
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("WARNING eventRepository: event not found");
                    return NotFoundException.of(Event.class, eventId);
                });
    }

}
