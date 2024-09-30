package com.portfolio.community.controller

import com.portfolio.community.dto.comment.*
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.service.CommentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@Tag(name = "comments", description = "댓글 API")
@RestController
class CommentController(
    private val commentService: CommentService
) {

    @Operation(
        summary = "댓글 작성",
        description = "특정 게시글에 댓글을 작성합니다. 해당 게시글에 접근할 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "postId", description = "댓글을 작성할 게시글의 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "댓글 생성 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = CommentCreateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "댓글 작성 성공", content = [Content(schema = Schema(implementation = CommentResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 게시글에 접근할 권한이 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("@securityService.canCreateComment(authentication, #postId)")
    fun createComment(
        @PathVariable postId: Long,
        @Valid @RequestBody commentCreateRequest: CommentCreateRequest,
        @AuthenticationPrincipal user: Principal
    ) : ResponseEntity<CommentResponse> {
        val comment = commentService.createComment(postId, user.id, commentCreateRequest)
        val location = URI.create("/posts/${postId}/comments/${comment.id}")
        return ResponseEntity.created(location).body(comment)
    }

    @Operation(
        summary = "특정 게시글의 댓글 목록 조회",
        description = "특정 게시글에 달린 댓글 목록을 조회합니다. 해당 게시글에 접근할 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "postId", description = "조회할 게시글의 ID", required = true, example = "1"),
            Parameter(name = "page", description = "조회할 페이지 번호", example = "0", required = false),
            Parameter(name = "size", description = "한 페이지에 보여줄 댓글 수", example = "100", required = false)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공", content = [Content(schema = Schema(implementation = Page::class, type = "array"), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "댓글 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 게시글에 접근할 권한이 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @GetMapping("/posts/{postId}/comments")
    @PreAuthorize("@securityService.canReadPost(authentication, #postId)")
    fun getCommentsByPost(
        @PathVariable postId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ): ResponseEntity<Page<CommentByPostResponse>> {
        val comments = commentService.getCommentsByPost(postId, page, size)
        return if(comments.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(comments)
        }
    }

    @Operation(
        summary = "특정 사용자가 작성한 댓글 목록 조회",
        description = "특정 사용자가 작성한 모든 댓글 목록을 조회합니다.",
        parameters = [
            Parameter(name = "userId", description = "댓글 작성자의 ID", required = true, example = "1"),
            Parameter(name = "page", description = "조회할 페이지 번호", example = "0", required = false),
            Parameter(name = "size", description = "한 페이지에 보여줄 댓글 수", example = "100", required = false)
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공", content = [Content(schema = Schema(implementation = Page::class, type = "array"), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "댓글 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @GetMapping("/users/{userId}/comments")
    fun getCommentsByAuthor(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ) : ResponseEntity<Page<CommentByAuthorResponse>> {
        val comments = commentService.getCommentsByAuthor(userId, page, size)
        return if(comments.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(comments)
        }
    }

    @Operation(
        summary = "댓글 수정",
        description = "특정 댓글을 수정합니다. 작성자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "commentId", description = "수정할 댓글의 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "댓글 수정 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = CommentUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "댓글 수정 성공", content = [Content(schema = Schema(implementation = CommentResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 작성자 권한 필요", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.canUpdateComment(authentication, #commentId)")
    fun updateComment(
        @PathVariable commentId: Long,
        @Valid @RequestBody commentUpdateRequest: CommentUpdateRequest
    ) : ResponseEntity<CommentResponse> {
        return ResponseEntity.ok(commentService.updateComment(commentId, commentUpdateRequest))
    }

    @Operation(
        summary = "댓글 삭제",
        description = "특정 댓글을 삭제합니다. 작성자 권한 또는 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "commentId", description = "삭제할 댓글의 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 작성자 권한 또는 관리자/스태프 권한 필요"),
            ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
        ]
    )
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.canDeleteComment(authentication, #commentId)")
    fun deleteComment(@PathVariable commentId: Long) : ResponseEntity<Void> {
        commentService.deleteComment(commentId)
        return ResponseEntity.noContent().build()
    }
}