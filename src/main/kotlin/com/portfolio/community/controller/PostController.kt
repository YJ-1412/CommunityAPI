package com.portfolio.community.controller

import com.portfolio.community.dto.post.*
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.service.PostService
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

@Tag(name = "posts", description = "게시글 API")
@RestController
class PostController(
    private val postService: PostService,
) {

    @Operation(
        summary = "게시글 작성",
        description = "특정 게시판에 새로운 게시글을 작성합니다. 해당 게시판에 접근할 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "boardId", description = "게시글을 작성할 게시판의 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "게시글 생성 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = PostCreateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "게시글 작성 성공", content = [Content(schema = Schema(implementation = PostResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 게시판에 접근할 수 없음"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
        ]
    )
    @PostMapping("/boards/{boardId}/posts")
    @PreAuthorize("@securityService.canCreatePost(authentication, #boardId)")
    fun createPost(
        @PathVariable boardId: Long,
        @Valid @RequestBody postCreateRequest: PostCreateRequest,
        @AuthenticationPrincipal author: Principal
    ): ResponseEntity<PostResponse> {
        val post = postService.createPost(boardId, author.id, postCreateRequest)
        val location = URI.create("/posts/${post.id}")
        return ResponseEntity.created(location).body(post)
    }

    @Operation(
        summary = "모든 게시글 정보 조회",
        description = "모든 게시글의 기본 정보를 페이지네이션을 통해 조회합니다.",
        parameters = [
            Parameter(name = "page", description = "조회할 페이지 번호", required = false, example = "0"),
            Parameter(name = "size", description = "페이지당 게시글 수", required = false, example = "20")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "게시글 정보 조회 성공", content = [Content(schema = Schema(implementation = Page::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "게시글 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @GetMapping("/posts")
    fun getAllPostInfos(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<Page<PostInfoResponse>> {
        val postInfos = postService.getAllPostInfos(page, size)
        return if(postInfos.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(postInfos)
        }
    }

    @Operation(
        summary = "게시판에 등록된 게시글 정보 조회",
        description = "특정 게시판에 등록된 게시글 목록을 페이지네이션을 통해 조회합니다.",
        parameters = [
            Parameter(name = "boardId", description = "게시판의 ID", required = true, example = "1"),
            Parameter(name = "page", description = "조회할 페이지 번호", required = false, example = "0"),
            Parameter(name = "size", description = "페이지당 게시글 수", required = false, example = "20")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "게시글 정보 조회 성공", content = [Content(schema = Schema(implementation = Page::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "게시글 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
        ]
    )
    @GetMapping("/boards/{boardId}/posts")
    fun getPostInfosByBoard(
        @PathVariable boardId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostInfoResponse>> {
        val postInfos = postService.getPostInfosByBoardId(boardId, page, size)
        return if(postInfos.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(postInfos)
        }
    }

    @Operation(
        summary = "사용자가 작성한 게시글 정보 조회",
        description = "특정 사용자가 작성한 게시글 목록을 페이지네이션을 통해 조회합니다.",
        parameters = [
            Parameter(name = "userId", description = "사용자의 ID", required = true, example = "1"),
            Parameter(name = "page", description = "조회할 페이지 번호", required = false, example = "0"),
            Parameter(name = "size", description = "페이지당 게시글 수", required = false, example = "20")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "게시글 정보 조회 성공", content = [Content(schema = Schema(implementation = Page::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "게시글 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @GetMapping("/users/{userId}/posts")
    fun getPostInfosByAuthor(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostInfoResponse>> {
        val postInfos = postService.getPostInfosByAuthorId(userId, page, size)
        return if(postInfos.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(postInfos)
        }
    }

    @Operation(
        summary = "사용자가 좋아요한 게시글 정보 조회",
        description = "특정 사용자가 좋아요한 게시글 목록을 페이지네이션을 통해 조회합니다.",
        parameters = [
            Parameter(name = "userId", description = "사용자의 ID", required = true, example = "1"),
            Parameter(name = "page", description = "조회할 페이지 번호", required = false, example = "0"),
            Parameter(name = "size", description = "페이지당 게시글 수", required = false, example = "20")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "좋아요한 게시글 정보 조회 성공", content = [Content(schema = Schema(implementation = Page::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "좋아요한 게시글 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @GetMapping("/users/{userId}/liked-posts")
    fun getLikedPostsByUser(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostInfoResponse>> {
        val postInfos = postService.getPostInfosByLikedUserId(userId, page, size)
        return if(postInfos.isEmpty) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(postInfos)
        }
    }

    /*
    게시글을 요청하면 조회수가 1 증가하는 기능을 구현하는 방법은 2가지가 있음.
    1. 별도에 PATCH /posts/{id}/view-count 엔드포인트를 만든 뒤, 클라이언트에서 게시글 요청과 함께 요청하여 조회수를 증가시키는 방법.
    2. GET /posts/{id} 요청을 받으면 백엔드 내부에서 조회수를 증가시키는 방법.

    1번을 선택하면 GET의 멱등성을 지킬 수 있는 대신 클라이언트와 서버의 통신 횟수가 2배 증가하고, 클라이언트 측에서 불필요한 동작의 책임을 맡게 된다는 문제가 있음.
    2번을 선택하면 클라이언트와 서버의 분리가 보장되고 api가 간단해지지만, GET 메소드의 멱등성이 보장되지 않아 REST를 위배함.

    REST를 철저하게 지키는 것보다 클라이언트와 서버의 분리가 더 중요하다고 판단하여 2번 해결책을 채택했음.
     */
    @Operation(
        summary = "게시글 조회 및 조회수 증가",
        description = "특정 게시글을 조회하며, 조회수가 증가합니다. 해당 게시글이 등록된 게시판에 접근할 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "postId", description = "게시글의 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "게시글 조회 성공", content = [Content(schema = Schema(implementation = PostResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 게시글이 등록된 게시판에 접근할 수 없음"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
        ]
    )
    @GetMapping("/posts/{postId}")
    @PreAuthorize("@securityService.canReadPost(authentication, #postId)")
    fun getPost(@PathVariable postId: Long): ResponseEntity<PostResponse> {
        val updatedPost = postService.increaseViewCount(postId)
        return ResponseEntity.ok(updatedPost)
    }

    @Operation(
        summary = "게시글 수정",
        description = "특정 게시글을 수정합니다. 작성자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "postId", description = "수정할 게시글의 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "게시글 수정 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = PostUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "게시글 수정 성공", content = [Content(schema = Schema(implementation = PostResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 작성자 권한 필요"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
        ]
    )
    @PutMapping("/posts/{postId}")
    @PreAuthorize("@securityService.canUpdatePost(authentication, #postId, #postUpdateRequest.boardId)")
    fun updatePost(
        @PathVariable postId: Long,
        @Valid @RequestBody postUpdateRequest: PostUpdateRequest
    ): ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.updatePost(postId, postUpdateRequest))
    }

    @Operation(
        summary = "게시글 좋아요",
        description = "특정 게시글을 좋아요합니다. 해당 게시글에 접근할 권한이 필요합니다. 작성자이거나 이미 좋아요한 게시글이면 실패합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "postId", description = "좋아요할 게시글의 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "게시글 좋아요 성공", content = [Content(schema = Schema(implementation = PostResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 게시글에 접근할 수 없음"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "이미 좋아요한 게시글이거나 게시글의 작성자임")
        ]
    )
    @PostMapping("/posts/{postId}/liked-users")
    @PreAuthorize("@securityService.canReadPost(authentication, #postId)")
    fun likePost(@PathVariable postId: Long, @AuthenticationPrincipal user: Principal): ResponseEntity<PostResponse> {
        val likedPost = postService.likePost(user.id, postId)
        return ResponseEntity.ok(likedPost)
    }

    @Operation(
        summary = "게시글 좋아요 취소",
        description = "특정 게시글에 대한 좋아요를 취소합니다. 해당 게시글에 접근할 권한이 필요합니다. 이미 해당 게시글을 좋아요한 사용자만 이 작업을 수행할 수 있습니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "postId", description = "좋아요를 취소할 게시글의 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "게시글 좋아요 취소 성공", content = [Content(schema = Schema(implementation = PostResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 게시글에 접근할 수 없음"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "좋아요하지 않은 게시글")
        ]
    )
    @DeleteMapping("/posts/{postId}/liked-users")
    @PreAuthorize("@securityService.canReadPost(authentication, #postId)")
    fun unlikePost(@PathVariable postId: Long, @AuthenticationPrincipal user: Principal) : ResponseEntity<PostResponse> {
        val unlikedPost = postService.unlikePost(user.id, postId)
        return ResponseEntity.ok(unlikedPost)
    }

    @Operation(
        summary = "게시글 삭제",
        description = "특정 게시글을 삭제합니다. 작성자 권한 또는 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "postId", description = "삭제할 게시글의 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 작성자 권한 또는 관리자/스태프 권한 필요"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
        ]
    )
    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("@securityService.canDeletePost(authentication, #postId)")
    fun deletePost(@PathVariable postId: Long): ResponseEntity<Void> {
        postService.deletePost(postId)
        return ResponseEntity.noContent().build()
    }

}