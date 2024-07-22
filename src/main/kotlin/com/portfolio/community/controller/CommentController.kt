package com.portfolio.community.controller

import com.portfolio.community.dto.comment.*
import com.portfolio.community.service.CommentService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class CommentController(
    private val commentService: CommentService
) {
    @GetMapping("/posts/{postId}/comments")
    @PreAuthorize("@securityService.canReadPost(authentication, #postId)")
    fun getCommentsByPost(
        @PathVariable postId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ): ResponseEntity<Page<CommentResponse>> {
        val comments = commentService.getCommentsByPost(postId, page, size)
        return if(comments.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(comments)
        }
    }

    @GetMapping("/users/{userId}/comments")
    @PreAuthorize("permitAll()")
    fun getCommentsByAuthor(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ) : ResponseEntity<Page<CommentResponse>> {
        val comments = commentService.getCommentsByAuthor(userId, page, size)
        return if(comments.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(comments)
        }
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("@securityService.canCreateComment(authentication, #postId, #commentCreateRequest.authorId)")
    fun createComment(
        @PathVariable postId: Long,
        @Valid @RequestBody commentCreateRequest: CommentCreateRequest
    ) : ResponseEntity<CommentResponse> {
        val comment = commentService.createComment(postId, commentCreateRequest)
        val location = URI.create("/posts/${postId}/comments/${comment.id}")
        return ResponseEntity.created(location).body(comment)
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.canUpdateComment(authentication, #commentId)")
    fun updateComment(
        @PathVariable commentId: Long,
        @Valid @RequestBody commentUpdateRequest: CommentUpdateRequest
    ) : ResponseEntity<CommentResponse> {
        return ResponseEntity.ok(commentService.updateComment(commentId, commentUpdateRequest))
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.canDeleteComment(authentication, #commentId)")
    fun deleteComment(@PathVariable commentId: Long) : ResponseEntity<Void> {
        commentService.deleteComment(commentId)
        return ResponseEntity.noContent().build()
    }
}