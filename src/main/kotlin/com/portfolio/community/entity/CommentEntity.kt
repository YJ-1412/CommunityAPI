package com.portfolio.community.entity

import jakarta.persistence.*

@Entity(name = "comments")
class CommentEntity (
    content: String,
    author: UserEntity,
    post: PostEntity,
    id: Long = 0
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = id
        protected set

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String = content
        protected set

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    var author: UserEntity = author
        protected set

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    var post: PostEntity = post
        protected set

    fun update(content: String = this.content) {
        this.content = content
    }

}