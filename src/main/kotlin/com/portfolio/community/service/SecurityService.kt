package com.portfolio.community.service

import com.portfolio.community.dto.user.Principal
import com.portfolio.community.repository.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class SecurityService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
) {

    fun isAdmin(authentication: Authentication): Boolean {
        val principal = authentication.principal as Principal
        return principal.isAdmin
    }

    fun isStaff(authentication: Authentication): Boolean {
        val principal = authentication.principal as Principal
        return principal.isStaff
    }

    fun isAdminOrStaff(authentication: Authentication): Boolean {
        return isAdmin(authentication) || isStaff(authentication)
    }

    fun canReadBoard(authentication: Authentication, boardId: Long): Boolean {
        val principal = authentication.principal as Principal
        val board = boardRepository.findByIdOrNull(boardId) ?: return false

        return isAdminOrStaff(authentication) || board.readableRole.level <= principal.role.level
    }

    fun canReadPost(authentication: Authentication, postId: Long): Boolean{
        val principal = authentication.principal as Principal
        val post = postRepository.findByIdOrNull(postId) ?: return false

        return isAdminOrStaff(authentication) || post.board.readableRole.level <= principal.role.level
    }

    fun canUnlikePost(authentication: Authentication, postId: Long, userId: Long): Boolean{
        val principal = authentication.principal as Principal
        return principal.id == userId && canReadPost(authentication, postId)
    }

    fun canCreatePost(authentication: Authentication, boardId: Long, authorId: Long): Boolean{
        val principal = authentication.principal as Principal
        val board = boardRepository.findByIdOrNull(boardId) ?: return false
        return principal.id == authorId && (isAdminOrStaff(authentication) || board.readableRole.level <= principal.role.level)
    }

    fun canUpdatePost(authentication: Authentication, postId: Long, boardId: Long): Boolean{
        val principal = authentication.principal as Principal
        return isAuthorOfPost(principal, postId) && canReadBoard(authentication, boardId)
    }

    fun canDeletePost(authentication: Authentication, postId: Long): Boolean{
        val principal = authentication.principal as Principal
        postRepository.findByIdOrNull(postId) ?: return false
        return isAuthorOfPost(principal, postId) || isAdminOrStaff(authentication)
    }

    fun canCreateComment(authentication: Authentication, postId: Long, authorId: Long): Boolean{
        val principal = authentication.principal as Principal
        return principal.id == authorId && canReadPost(authentication, postId)
    }

    fun canUpdateComment(authentication: Authentication, commentId: Long): Boolean{
        val principal = authentication.principal as Principal
        return isAuthorOfComment(principal, commentId)
    }

    fun canDeleteComment(authentication: Authentication, commentId: Long): Boolean{
        val principal = authentication.principal as Principal
        commentRepository.findByIdOrNull(commentId) ?: return false
        return isAuthorOfComment(principal, commentId) || isAdminOrStaff(authentication)
    }

    fun canUpdateUser(authentication: Authentication, userId: Long): Boolean {
        val principal = authentication.principal as Principal
        return principal.id == userId
    }

    fun canDeleteUser(authentication: Authentication, userId: Long): Boolean {
        val principal = authentication.principal as Principal
        val user = userRepository.findByIdOrNull(userId) ?: return false
        if(user.isAdmin) return false
        if(principal.id == userId) return true
        if(user.isStaff) return isAdmin(authentication)
        return isAdminOrStaff(authentication)
    }

    fun canChangeStaff(authentication: Authentication, userId: Long): Boolean{
        val user = userRepository.findByIdOrNull(userId) ?: return false
        return isAdmin(authentication) && !user.isAdmin
    }

    private fun isAuthorOfPost(user: Principal, postId: Long): Boolean {
        val post = postRepository.findByIdOrNull(postId) ?: return false
        return user.id == post.author.id
    }

    private fun isAuthorOfComment(user: Principal, commentId: Long): Boolean {
        val comment = commentRepository.findByIdOrNull(commentId) ?: return false
        return user.id == comment.author.id
    }

}