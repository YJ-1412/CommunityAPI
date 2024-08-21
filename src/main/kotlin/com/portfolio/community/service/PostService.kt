package com.portfolio.community.service

import com.portfolio.community.dto.post.*
import com.portfolio.community.entity.PostEntity
import com.portfolio.community.entity.UserLikePost
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.LikeRepository
import com.portfolio.community.repository.PostRepository
import com.portfolio.community.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository,
) {

    @Transactional(readOnly = true)
    fun getAllPostInfos(page: Int, size: Int): Page<PostInfoResponse> {
        return postRepository.findAllByOrderByIdDesc(PageRequest.of(page, size)).map{ PostInfoResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getPostInfosByBoardId(boardId: Long, page: Int, size: Int): Page<PostInfoResponse> {
        if(!boardRepository.existsById(boardId)) throw NotFoundException("Board", "ID", boardId)
        return postRepository.findByBoardIdOrderByIdDesc(boardId, PageRequest.of(page, size)).map{ PostInfoResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getPostInfosByAuthorId(userId: Long, page: Int, size: Int): Page<PostInfoResponse> {
        if(!userRepository.existsById(userId)) throw NotFoundException("User", "ID", userId)
        return postRepository.findByAuthorIdOrderByIdDesc(userId, PageRequest.of(page, size)).map{ PostInfoResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getPostInfosByLikedUserId(likedUserId: Long, page: Int, size: Int): Page<PostInfoResponse> {
        if(!userRepository.existsById(likedUserId)) throw NotFoundException("User", "ID", likedUserId)
        val likes = likeRepository.findAllByUserIdOrderByIdDesc(likedUserId, PageRequest.of(page, size))
        return likes.map{ PostInfoResponse(it.post) }
    }

    @Transactional
    fun likePost(userId: Long, postId: Long): PostResponse {
        if(likeRepository.existsByUserIdAndPostId(userId, postId)) throw IllegalStateException("User already liked this post")

        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        val post = postRepository.findByIdOrNull(postId) ?: throw NotFoundException("Post", "ID", postId)
        if (user.id == post.author.id) throw IllegalStateException("Author of post can not like post")

        likeRepository.save(UserLikePost(user = user, post = post))

        return PostResponse(post)
    }

    @Transactional
    fun unlikePost(userId: Long, postId: Long): PostResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        val post = postRepository.findByIdOrNull(postId) ?: throw NotFoundException("Post", "ID", postId)

        val like = likeRepository.findByUserIdAndPostId(userId, postId) ?: throw IllegalStateException("User did not like this post")

        user.likedPosts.remove(like)
        post.likedUsers.remove(like)
        likeRepository.delete(like)

        return PostResponse(post)
    }

    @Transactional
    fun increaseViewCount(postId: Long): PostResponse {
        val post = postRepository.findByIdOrNull(postId) ?: throw NotFoundException("Post", "ID", postId)
        post.increaseViewCount()
        return PostResponse(post)
    }

    @Transactional
    fun createPost(boardId: Long, authorId:Long, postCreateRequest: PostCreateRequest): PostResponse {
        val author = userRepository.findByIdOrNull(authorId) ?: throw NotFoundException("User", "ID", authorId)
        val board = boardRepository.findByIdOrNull(boardId) ?: throw NotFoundException("Board", "ID", boardId)

        val post = PostEntity(
            title = postCreateRequest.title!!,
            content = postCreateRequest.content!!,
            author = author,
            board = board,
        )

        return PostResponse(postRepository.save(post))
    }

    @Transactional
    fun updatePost(postId: Long, postUpdateRequest: PostUpdateRequest): PostResponse {
        val post = postRepository.findByIdOrNull(postId) ?: throw NotFoundException("Post", "ID", postId)
        val board = boardRepository.findByIdOrNull(postUpdateRequest.boardId) ?: throw NotFoundException("Board", "ID", postUpdateRequest.boardId!!)

        post.update(
            title = postUpdateRequest.title!!,
            content = postUpdateRequest.content!!,
            board = board
        )

        return PostResponse(post)
    }

    @Transactional
    fun deletePost(postId: Long) {
        val post = postRepository.findByIdOrNull(postId) ?: throw NotFoundException("Post", "ID", postId)
        postRepository.delete(post)
    }

}