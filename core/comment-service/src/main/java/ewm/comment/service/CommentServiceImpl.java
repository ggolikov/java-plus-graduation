package ewm.comment.service;

import ewm.comment.dto.NewCommentDto;
import ewm.comment.dto.UpdateCommentRequest;
import ewm.comment.mapper.CommentMapper;
import ewm.comment.model.Comment;
import ewm.comment.model.CommentStatus;
import ewm.comment.repository.CommentRepository;
import ewm.common.dto.comment.CommentDto;
import ewm.common.exception.ConflictException;
import ewm.common.exception.NotFoundException;
import ewm.common.model.EventState;
import ewm.event.client.EventClient;
import ewm.event.client.dto.EventInternalDto;
import ewm.user.client.UserClient;
import ewm.common.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long eventId, NewCommentDto newCommentDto) {
        UserDto user = userClient.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        EventInternalDto event = eventClient.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (!EventState.PUBLISHED.name().equals(event.getState())) {
            throw new ConflictException("Comments can only be added to published events");
        }

        Comment comment = CommentMapper.mapToComment(newCommentDto);
        comment.setAuthorId(user.getId());
        comment.setEventId(eventId);
        comment.setStatus(CommentStatus.NEW);

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CommentDto update(Long userId, Long commentId, UpdateCommentRequest updateCommentRequest) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        if (comment.getStatus() == CommentStatus.REJECTED) {
            throw new ConflictException("Cannot update rejected comment");
        }

        CommentMapper.updateComment(comment, updateCommentRequest);
        comment.setStatus(CommentStatus.NEW); // Reset to NEW when updated

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getEventComments(Long eventId, int from, int size) {
        eventClient.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        Pageable page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED, page);

        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(int from, int size) {
        Pageable page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByStatus(CommentStatus.APPROVED, page);

        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentDto approve(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        comment.setStatus(CommentStatus.APPROVED);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CommentDto reject(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        comment.setStatus(CommentStatus.REJECTED);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }
}
