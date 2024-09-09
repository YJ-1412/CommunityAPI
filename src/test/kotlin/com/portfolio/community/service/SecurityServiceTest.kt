package com.portfolio.community.service

import com.portfolio.community.dto.user.Principal
import com.portfolio.community.entity.*
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.CommentRepository
import com.portfolio.community.repository.PostRepository
import com.portfolio.community.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication

class SecurityServiceTest {
    private lateinit var securityService: SecurityService
    private lateinit var postRepository: PostRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var boardRepository: BoardRepository
    private lateinit var userRepository: UserRepository

    private val level1 = Role(name = "LV1", level = 1)
    private val level2 = Role(name = "LV1", level = 2)

    private lateinit var adminUser: UserEntity
    private lateinit var staffUser: UserEntity
    private lateinit var level1User: UserEntity
    private lateinit var level2User: UserEntity

    private lateinit var adminAuth: Authentication
    private lateinit var staffAuth: Authentication
    private lateinit var level1Auth: Authentication
    private lateinit var level2Auth: Authentication

    @BeforeEach
    fun setUp() {
        postRepository = mockk()
        commentRepository = mockk()
        boardRepository = mockk()
        userRepository = mockk()
        securityService = SecurityService(postRepository, commentRepository, boardRepository, userRepository)

        adminUser = UserEntity(username = "Admin", password = "password", role = level1, id = 1L).apply { setAdmin() }
        staffUser = UserEntity(username = "Staff", password = "password", role = level1, id = 2L).apply { setStaff() }
        level1User = UserEntity(username = "Lv1 User", password = "password", role = level1, id = 3L)
        level2User = UserEntity(username = "Lv2 User", password = "password", role = level2, id = 4L)

        adminAuth = TestingAuthenticationToken(
            Principal(adminUser),
            "password"
        )
        staffAuth = TestingAuthenticationToken(
            Principal(staffUser),
            "password"
        )
        level1Auth = TestingAuthenticationToken(
            Principal(level1User),
            "password"
        )
        level2Auth = TestingAuthenticationToken(
            Principal(level2User),
            "password"
        )
    }

    @Test
    fun given_UserIsAdmin_when_IsAdmin_then_ReturnTrue() {
        //When
        val resultAdmin = securityService.isAdmin(adminAuth)
        val resultStaff = securityService.isAdmin(staffAuth)
        val resultLV1 = securityService.isAdmin(level1Auth)
        val resultLV2 = securityService.isAdmin(level2Auth)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsStaff_when_IsStaff_then_ReturnTrue() {
        //When
        val resultAdmin = securityService.isStaff(adminAuth)
        val resultStaff = securityService.isStaff(staffAuth)
        val resultLV1 = securityService.isStaff(level1Auth)
        val resultLV2 = securityService.isStaff(level2Auth)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaff_when_IsAdminOrStaff_then_ReturnTrue() {
        //When
        val resultAdmin = securityService.isAdminOrStaff(adminAuth)
        val resultStaff = securityService.isAdminOrStaff(staffAuth)
        val resultLV1 = securityService.isAdminOrStaff(level1Auth)
        val resultLV2 = securityService.isAdminOrStaff(level2Auth)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffOrReadableLevel_when_CanReadBoard_then_ReturnTrueOrFalse() {
        //Given
        val testBoard = BoardEntity(name = "Test Board", priority = 0, readableRole = level2, id = 1L)
        every { boardRepository.findByIdOrNull(1L) } returns testBoard

        //When
        val resultAdmin = securityService.canReadBoard(adminAuth, 1)
        val resultStaff = securityService.canReadBoard(staffAuth, 1)
        val resultLV1 = securityService.canReadBoard(level1Auth, 1)
        val resultLV2 = securityService.canReadBoard(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isTrue()
    }

    @Test
    fun given_InvalidBoardId_when_CanReadBoard_then_ReturnFalse() {
        //Given
        every { boardRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canReadBoard(adminAuth, 1)
        val resultStaff = securityService.canReadBoard(staffAuth, 1)
        val resultLV1 = securityService.canReadBoard(level1Auth, 1)
        val resultLV2 = securityService.canReadBoard(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffOrReadableLevel_when_CanReadPost_then_ReturnTrueOrFalse() {
        //Given
        val author = UserEntity(username = "Author", password = "password", role = level2)
        val board = BoardEntity(name = "Board", priority = 0, readableRole = level2, id = 1L)
        val testPost = PostEntity(title = "Test Post", content = "content", author = author, board = board, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns testPost

        //When
        val resultAdmin = securityService.canReadPost(adminAuth, 1)
        val resultStaff = securityService.canReadPost(staffAuth, 1)
        val resultLV1 = securityService.canReadPost(level1Auth, 1)
        val resultLV2 = securityService.canReadPost(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isTrue()
    }

    @Test
    fun given_InvalidPostId_when_CanReadPost_then_ReturnFalse() {
        //Given
        every { postRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canReadPost(adminAuth, 1)
        val resultStaff = securityService.canReadPost(staffAuth, 1)
        val resultLV1 = securityService.canReadPost(level1Auth, 1)
        val resultLV2 = securityService.canReadPost(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffOrReadableLevel_when_CanCreatePost_then_ReturnTrueOrFalse() {
        //Given
        val board = BoardEntity(name = "Board", priority = 0, readableRole = level2, id = 1L)
        every { boardRepository.findByIdOrNull(1L) } returns board

        //When
        val resultAdmin = securityService.canCreatePost(adminAuth, 1)
        val resultStaff = securityService.canCreatePost(staffAuth, 1)
        val resultLV1 = securityService.canCreatePost(level1Auth, 1)
        val resultLV2 = securityService.canCreatePost(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isTrue()
    }

    @Test
    fun given_InvalidBoardId_when_CanCreatePost_then_ReturnFalse() {
        //Given
        every { boardRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canCreatePost(adminAuth, 1)
        val resultStaff = securityService.canCreatePost(staffAuth, 1)
        val resultLV1 = securityService.canCreatePost(level1Auth, 1)
        val resultLV2 = securityService.canCreatePost(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAuthorOfPostAndReadableLevelOfTargetBoard_when_CanUpdatePost_then_ReturnTrueOrFalse() {
        //Given
        val author = level1User
        val board1 = BoardEntity(name = "Board 1", priority = 0, readableRole = level1, id = 1L)
        val board2 = BoardEntity(name = "Board 2", priority = 1, readableRole = level2, id = 2L)
        val testPost = PostEntity(title = "Test Post", content = "content", author = author, board = board1, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns testPost
        every { boardRepository.findByIdOrNull(1L) } returns board1
        every { boardRepository.findByIdOrNull(2L) } returns board2

        //When
        val resultAdmin = securityService.canUpdatePost(adminAuth, 1, 2)
        val resultStaff = securityService.canUpdatePost(staffAuth, 1, 2)
        val resultLV1ToNotReadableBoard = securityService.canUpdatePost(level1Auth, 1, 2)
        val resultLV1ToReadableBoard = securityService.canUpdatePost(level1Auth, 1, 1)
        val resultLV2 = securityService.canUpdatePost(level2Auth, 1, 2)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1ToNotReadableBoard).isFalse()
        assertThat(resultLV1ToReadableBoard).isTrue()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_InvalidPostId_when_CanUpdatePost_then_ReturnFalse() {
        //Given
        every { postRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canUpdatePost(adminAuth, 1, 2)
        val resultStaff = securityService.canUpdatePost(staffAuth, 1, 2)
        val resultLV1ToNotReadableBoard = securityService.canUpdatePost(level1Auth, 1, 2)
        val resultLV1ToReadableBoard = securityService.canUpdatePost(level1Auth, 1, 1)
        val resultLV2 = securityService.canUpdatePost(level2Auth, 1, 2)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1ToNotReadableBoard).isFalse()
        assertThat(resultLV1ToReadableBoard).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_InvalidBoardId_when_CanUpdatePost_then_ReturnFalse() {
        //Given
        val author = level1User
        val board = BoardEntity(name = "Board 1", priority = 0, readableRole = level1, id = 1L)
        val testPost = PostEntity(title = "Test Post", content = "content", author = author, board = board, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns testPost
        every { boardRepository.findByIdOrNull(2L) } returns null


        //When
        val resultAdmin = securityService.canUpdatePost(adminAuth, 1, 2)
        val resultStaff = securityService.canUpdatePost(staffAuth, 1, 2)
        val resultLV1 = securityService.canUpdatePost(level1Auth, 1, 2)
        val resultLV2 = securityService.canUpdatePost(level2Auth, 1, 2)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffOrAuthorOfPost_when_CanDeletePost_then_ReturnTrueOrFalse() {
        //Given
        val author = level1User
        val board = BoardEntity(name = "Board 1", priority = 0, readableRole = level1, id = 1L)
        val testPost = PostEntity(title = "Test Post", content = "content", author = author, board = board, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns testPost

        //When
        val resultAdmin = securityService.canDeletePost(adminAuth, 1)
        val resultStaff = securityService.canDeletePost(staffAuth, 1)
        val resultLV1 = securityService.canDeletePost(level1Auth, 1)
        val resultLV2 = securityService.canDeletePost(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isTrue()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_InvalidPostId_when_CanDeletePost_then_ReturnFalse() {
        //Given
        every { postRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canDeletePost(adminAuth, 1)
        val resultStaff = securityService.canDeletePost(staffAuth, 1)
        val resultLV1 = securityService.canDeletePost(level1Auth, 1)
        val resultLV2 = securityService.canDeletePost(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffOrReadableLevel_when_CanCreateComment_then_ReturnTrueOrFalse() {
        //Given
        val author = level2User
        val board = BoardEntity(name = "Board 1", priority = 0, readableRole = level2, id = 1L)
        val testPost = PostEntity(title = "Test Post", content = "content", author = author, board = board, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns testPost

        //When
        val resultAdmin = securityService.canCreateComment(adminAuth, 1)
        val resultStaff = securityService.canCreateComment(staffAuth, 1)
        val resultLV1 = securityService.canCreateComment(level1Auth, 1)
        val resultLV2 = securityService.canCreateComment(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isTrue()
    }

    @Test
    fun given_InvalidPostId_when_CanCreateComment_then_ReturnFalse() {
        //Given
        every { postRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canCreateComment(adminAuth, 1)
        val resultStaff = securityService.canCreateComment(staffAuth, 1)
        val resultLV1 = securityService.canCreateComment(level1Auth, 1)
        val resultLV2 = securityService.canCreateComment(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAuthorOfComment_when_CanUpdateComment_then_ReturnTrueOrFalse() {
        //Given
        val postAuthor = level1User
        val board = BoardEntity(name = "Board 1", priority = 0, readableRole = level1, id = 1L)
        val testPost = PostEntity(title = "Test Post", content = "content", author = postAuthor, board = board, id = 1L)
        val commentAuthor = level2User
        val testComment = CommentEntity(content = "Test Comment", author = commentAuthor, testPost, id = 1L)
        every { commentRepository.findByIdOrNull(1L) } returns testComment

        //When
        val resultAdmin = securityService.canUpdateComment(adminAuth, 1)
        val resultStaff = securityService.canUpdateComment(staffAuth, 1)
        val resultLV1 = securityService.canUpdateComment(level1Auth, 1)
        val resultLV2 = securityService.canUpdateComment(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isTrue()
    }

    @Test
    fun given_InvalidCommentId_when_CanUpdateComment_then_ReturnFalse() {
        //Given
        every { commentRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canUpdateComment(adminAuth, 1)
        val resultStaff = securityService.canUpdateComment(staffAuth, 1)
        val resultLV1 = securityService.canUpdateComment(level1Auth, 1)
        val resultLV2 = securityService.canUpdateComment(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffOrAuthorOfComment_when_CanDeleteComment_then_ReturnTrue() {
        //Given
        val postAuthor = level1User
        val board = BoardEntity(name = "Board 1", priority = 0, readableRole = level1, id = 1L)
        val testPost = PostEntity(title = "Test Post", content = "content", author = postAuthor, board = board, id = 1L)
        val commentAuthor = level2User
        val testComment = CommentEntity(content = "Test Comment", author = commentAuthor, testPost, id = 1L)
        every { commentRepository.findByIdOrNull(1L) } returns testComment

        //When
        val resultAdmin = securityService.canDeleteComment(adminAuth, 1)
        val resultStaff = securityService.canDeleteComment(staffAuth, 1)
        val resultLV1 = securityService.canDeleteComment(level1Auth, 1)
        val resultLV2 = securityService.canDeleteComment(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isTrue()
    }

    @Test
    fun given_InvalidCommentId_when_CanDeleteComment_then_ReturnFalse() {
        //Given
        every { commentRepository.findByIdOrNull(1L) } returns null

        //When
        val resultAdmin = securityService.canDeleteComment(adminAuth, 1)
        val resultStaff = securityService.canDeleteComment(staffAuth, 1)
        val resultLV1 = securityService.canDeleteComment(level1Auth, 1)
        val resultLV2 = securityService.canDeleteComment(level2Auth, 1)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsTargetUser_when_CanUpdateUser_then_ReturnTrue() {
        //When
        val resultAdmin = securityService.canUpdateUser(adminAuth, 1)
        val resultStaff = securityService.canUpdateUser(staffAuth, 2)
        val resultLV1 = securityService.canUpdateUser(level1Auth, 3)
        val resultLV2 = securityService.canUpdateUser(level2Auth, 4)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isTrue()
        assertThat(resultLV2).isTrue()
    }

    @Test
    fun given_UserIsNotTargetUser_when_CanUpdateUser_then_ReturnFalse() {
        //When
        val resultAdmin = securityService.canUpdateUser(adminAuth, 0)
        val resultStaff = securityService.canUpdateUser(staffAuth, 0)
        val resultLV1 = securityService.canUpdateUser(level1Auth, 0)
        val resultLV2 = securityService.canUpdateUser(level2Auth, 0)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffOrTargetUser_when_CanDeleteUser_then_ReturnTrue() {
        //Given
        val targetUser = level1User
        every { userRepository.findByIdOrNull(1L) } returns adminUser
        every { userRepository.findByIdOrNull(2L) } returns staffUser
        every { userRepository.findByIdOrNull(3L) } returns level1User
        every { userRepository.findByIdOrNull(4L) } returns level2User

        //When
        val resultAdmin = securityService.canDeleteUser(adminAuth, targetUser.id)
        val resultStaff = securityService.canDeleteUser(staffAuth, targetUser.id)
        val resultLV1 = securityService.canDeleteUser(level1Auth, targetUser.id)
        val resultLV2 = securityService.canDeleteUser(level2Auth, targetUser.id)

        //Then
        assertThat(resultAdmin).isTrue()
        assertThat(resultStaff).isTrue()
        assertThat(resultLV1).isTrue()
        assertThat(resultLV2).isFalse()
    }

    @Test
    fun given_UserIsAdminOrStaffAndTargetUserIsNotAdminAndNotStaff_when_CanSetStaff_then_ReturnTrue() {
        //Given
        every { userRepository.findByIdOrNull(1L) } returns adminUser
        every { userRepository.findByIdOrNull(2L) } returns staffUser
        every { userRepository.findByIdOrNull(3L) } returns level1User

        //When
        val resultAdminToAdmin = securityService.canChangeStaff(adminAuth, 1)
        val resultAdminToStaff = securityService.canChangeStaff(adminAuth, 2)
        val resultAdminToRegular = securityService.canChangeStaff(adminAuth, 3)
        val resultStaffToAdmin = securityService.canChangeStaff(staffAuth, 1)
        val resultStaffToStaff = securityService.canChangeStaff(staffAuth, 2)
        val resultStaffToRegular = securityService.canChangeStaff(staffAuth, 3)
        val resultRegularToAdmin = securityService.canChangeStaff(level1Auth, 1)
        val resultRegularToStaff = securityService.canChangeStaff(level1Auth, 2)
        val resultRegularToRegular = securityService.canChangeStaff(level1Auth, 3)

        //Then
        assertThat(resultAdminToAdmin).isFalse()
        assertThat(resultAdminToStaff).isTrue()
        assertThat(resultAdminToRegular).isTrue()
        assertThat(resultStaffToAdmin).isFalse()
        assertThat(resultStaffToStaff).isFalse()
        assertThat(resultStaffToRegular).isFalse()
        assertThat(resultRegularToAdmin).isFalse()
        assertThat(resultRegularToStaff).isFalse()
        assertThat(resultRegularToRegular).isFalse()
    }

    @Test
    fun given_InvalidUserId_when_CanSetStaff_then_ReturnFalse() {
        //Given
        every { userRepository.findByIdOrNull(0L) } returns null

        //When
        val resultAdmin = securityService.canChangeStaff(adminAuth, 0)
        val resultStaff = securityService.canChangeStaff(staffAuth, 0)
        val resultLV1 = securityService.canChangeStaff(level1Auth, 0)
        val resultLV2 = securityService.canChangeStaff(level2Auth, 0)

        //Then
        assertThat(resultAdmin).isFalse()
        assertThat(resultStaff).isFalse()
        assertThat(resultLV1).isFalse()
        assertThat(resultLV2).isFalse()
    }

}