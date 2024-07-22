package com.portfolio.community.entity

import jakarta.persistence.*

@Entity
class PostEntity (
    title: String,
    content: String,
    author: UserEntity,
    board: BoardEntity,
    id: Long = 0
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = id
        protected set

    @Column(length = 100, nullable = false)
    var title: String = title
        protected set

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String = content
        protected set

    @Column(nullable = false)
    var viewCount: Int = 0
        protected set

    @Column(nullable = false)
    var isAnnouncement: Boolean = false
        protected set

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    var author: UserEntity = author
        protected set

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id")
    var board: BoardEntity = board
        protected set

    @OneToMany(mappedBy = "post", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var comments: MutableSet<CommentEntity> = mutableSetOf()
        protected set

    @OneToMany(mappedBy = "post", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var likedUsers: MutableSet<UserLikePost> = mutableSetOf()
        protected set

    fun increaseViewCount() {
        viewCount += 1
    }

    fun update(title: String = this.title, content: String = this.content, board: BoardEntity = this.board) {
        this.title = title
        this.content = content

        this.board.posts.remove(this)
        this.board = board
        board.posts.add(this)
    }
}