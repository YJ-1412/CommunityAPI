package com.portfolio.community.service

import com.portfolio.community.dto.comment.*
import com.portfolio.community.entity.CommentEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.CommentRepository
import com.portfolio.community.repository.PostRepository
import com.portfolio.community.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getCommentsByPost(postId: Long, page: Int, size: Int): Page<CommentByPostResponse> {
        if(!postRepository.existsById(postId)) throw NotFoundException("Post", "ID", postId)
        return commentRepository.findByPostIdOrderByIdAsc(postId, PageRequest.of(page, size)).map { CommentByPostResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getCommentsByAuthor(authorId: Long, page: Int, size: Int): Page<CommentByAuthorResponse> {
        if(!userRepository.existsById(authorId)) throw NotFoundException("User", "ID", authorId)
        return commentRepository.findByAuthorIdOrderByIdAsc(authorId, PageRequest.of(page, size)).map { CommentByAuthorResponse(it) }
    }

    @Transactional
    fun createComment(postId: Long, authorId: Long, commentCreateRequest: CommentCreateRequest): CommentResponse {
        val author = userRepository.findByIdOrNull(authorId) ?: throw NotFoundException("User", "ID", authorId)
        val post = postRepository.findByIdOrNull(postId) ?: throw NotFoundException("Post", "ID", postId)

        val comment = CommentEntity(
            content = commentCreateRequest.content!!,
            author = author,
            post = post,
        )

        return CommentResponse(commentRepository.save(comment))
    }

    @Transactional
    fun updateComment(commentId: Long, commentUpdateRequest: CommentUpdateRequest): CommentResponse {
        val comment = commentRepository.findByIdOrNull(commentId) ?: throw NotFoundException("Comment", "ID", commentId)
        comment.update(content = commentUpdateRequest.content!!)
        return CommentResponse(comment)
    }

    @Transactional
    fun deleteComment(commentId: Long) {
        val comment = commentRepository.findByIdOrNull(commentId) ?: throw NotFoundException("Comment", "ID", commentId)
        commentRepository.delete(comment)
    }
}