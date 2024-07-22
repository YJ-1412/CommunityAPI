package com.portfolio.community.entity

import jakarta.persistence.*

@Entity
class UserLikePost (
    user: UserEntity,
    post: PostEntity,
    id: Long = 0
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = id

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    var user: UserEntity = user
        protected set

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    var post: PostEntity = post
        protected set
}