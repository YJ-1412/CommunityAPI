package com.portfolio.community.service

import com.portfolio.community.dto.board.*
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.RoleRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val roleRepository: RoleRepository,
) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional(readOnly = true)
    fun getAllBoards(): List<BoardResponse> = boardRepository.findAllByOrderByPriority().map { BoardResponse(it) }

    @Transactional
    fun createBoard(boardCreateRequest: BoardCreateRequest): BoardResponse {

        if(boardRepository.existsByName(boardCreateRequest.name!!)) throw IllegalArgumentException("Board with name already exists")
        if(boardRepository.existsByPriority(boardCreateRequest.priority!!)) throw IllegalArgumentException("Board with priority already exists")
        val role = roleRepository.findByIdOrNull(boardCreateRequest.readableRoleId) ?: throw NotFoundException("Role", "ID", boardCreateRequest.readableRoleId!!)

        val board = BoardEntity(
            name = boardCreateRequest.name,
            priority = boardCreateRequest.priority,
            readableRole = role
        )
        val createdBoard = boardRepository.save(board)
        return BoardResponse(createdBoard)
    }

    @Transactional
    fun updateBoard(boardId: Long, boardUpdateRequest: BoardUpdateRequest): BoardResponse {
        val board = boardRepository.findByIdOrNull(boardId) ?: throw NotFoundException("Board", "ID", boardId)

        val boardFoundByName = boardRepository.findByName(boardUpdateRequest.name!!)
        if(boardFoundByName != null && boardFoundByName.id != board.id) throw IllegalArgumentException("Board with name already exists")

        val boardFoundByPriority = boardRepository.findByPriority(boardUpdateRequest.priority!!)
        if(boardFoundByPriority != null && boardFoundByPriority.id != board.id) throw IllegalArgumentException("Board with priority already exists")

        val role = roleRepository.findByIdOrNull(boardUpdateRequest.readableRoleId) ?: throw NotFoundException("Role", "ID", boardUpdateRequest.readableRoleId!!)

        board.update(
            name = boardUpdateRequest.name,
            priority = boardUpdateRequest.priority,
            readableRole = role
        )

        return BoardResponse(board)
    }

    @Transactional
    fun deleteBoard(boardId: Long) {
        val board = boardRepository.findByIdOrNull(boardId) ?: throw NotFoundException("Board", "ID", boardId)
        boardRepository.delete(board)
    }

    @Transactional
    fun deleteBoardAndMovePosts(sourceBoardId: Long, targetBoardId: Long): BoardResponse {
        val sourceBoard = boardRepository.findByIdOrNull(sourceBoardId) ?: throw NotFoundException("Board", "ID", sourceBoardId)
        val targetBoard = boardRepository.findByIdOrNull(targetBoardId) ?: throw NotFoundException("Board", "ID", targetBoardId)

        val iterator = sourceBoard.posts.iterator()
        while (iterator.hasNext()) {
            val post = iterator.next()
            iterator.remove()
            post.update(board = targetBoard)
        }
        boardRepository.delete(sourceBoard)
        return BoardResponse(targetBoard)
    }

    @Transactional
    fun batchUpdateBoard(boardBatchUpdateRequest: BoardBatchUpdateRequest): List<BoardResponse> {
        val deletes = boardBatchUpdateRequest.deletes
        val moves = boardBatchUpdateRequest.moves
        val updates = boardBatchUpdateRequest.updates
        val creates = boardBatchUpdateRequest.creates

        deletes.forEach { deleteBoard(it) }
        moves.forEach { deleteBoardAndMovePosts(it.first, it.second) }
        updates.forEach {
            val boardId = it.first
            val board = boardRepository.findByIdOrNull(boardId) ?: throw NotFoundException("Board", "ID", boardId)
            board.update(name = "[temp]${board.name}", priority = -board.priority-1)
        }
        entityManager.flush()
        updates.forEach { updateBoard(it.first, it.second) }
        entityManager.flush()
        creates.forEach { createBoard(it) }

        return boardRepository.findAllByOrderByPriority().map { BoardResponse(it) }
    }

}