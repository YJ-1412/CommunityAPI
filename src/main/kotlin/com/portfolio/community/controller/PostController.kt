package com.portfolio.community.controller

import com.portfolio.community.dto.post.*
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.service.PostService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class PostController(
    private val postService: PostService,
) {

    @PostMapping("/boards/{boardId}/posts")
    @PreAuthorize("@securityService.canCreatePost(authentication, #boardId, #postCreateRequest.authorId)")
    fun createPost(
        @PathVariable boardId: Long,
        @Valid @RequestBody postCreateRequest: PostCreateRequest
    ): ResponseEntity<PostResponse> {
        val post = postService.createPost(boardId, postCreateRequest)
        val location = URI.create("/posts/${post.id}")
        return ResponseEntity.created(location).body(post)
    }

    @GetMapping("/posts")
    @PreAuthorize("permitAll()")
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

    @GetMapping("/boards/{boardId}/posts")
    @PreAuthorize("permitAll()")
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

    @GetMapping("/users/{userId}/posts")
    @PreAuthorize("permitAll()")
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

    @GetMapping("/users/{userId}/liked-posts")
    @PreAuthorize("permitAll()")
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
    @GetMapping("/posts/{postId}")
    @PreAuthorize("@securityService.canReadPost(authentication, #postId)")
    fun getPost(@PathVariable postId: Long): ResponseEntity<PostResponse> {
        val updatedPost = postService.increaseViewCount(postId)
        return ResponseEntity.ok(updatedPost)
    }

    @PutMapping("/posts/{postId}")
    @PreAuthorize("@securityService.canUpdatePost(authentication, #postId, #postUpdateRequest.boardId)")
    fun updatePost(
        @PathVariable postId: Long,
        @Valid @RequestBody postUpdateRequest: PostUpdateRequest
    ): ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.updatePost(postId, postUpdateRequest))
    }

    @PostMapping("/posts/{postId}/liked-users")
    @PreAuthorize("@securityService.canReadPost(authentication, #postId)")
    fun likePost(@PathVariable postId: Long, @AuthenticationPrincipal user: Principal): ResponseEntity<PostResponse> {
        val likedPost = postService.likePost(user.id, postId)
        return ResponseEntity.ok(likedPost)
    }

    @DeleteMapping("/posts/{postId}/liked-users/{userId}")
    @PreAuthorize("@securityService.canUnlikePost(authentication, #postId, #userId)")
    fun unlikePost(@PathVariable postId: Long, @PathVariable userId: Long) : ResponseEntity<PostResponse> {
        val unlikedPost = postService.unlikePost(userId, postId)
        return ResponseEntity.ok(unlikedPost)
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("@securityService.canDeletePost(authentication, #postId)")
    fun deletePost(@PathVariable postId: Long): ResponseEntity<Void> {
        postService.deletePost(postId)
        return ResponseEntity.noContent().build()
    }

}