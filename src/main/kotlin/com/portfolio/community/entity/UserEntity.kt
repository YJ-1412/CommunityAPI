package com.portfolio.community.entity

import jakarta.persistence.*

@Entity
class UserEntity (
    username: String,
    password: String,
    role: Role,
    id: Long = 0
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = id
        protected set

    @Column(unique = true, nullable = false)
    var username: String = username

    @Column(nullable = false)
    var password: String = password

    @Column(nullable = false)
    var isAdmin: Boolean = false
        protected set

    @Column(nullable = false)
    var isStaff: Boolean = false
        protected set

    @Column(nullable = true, length = 512)
    var refreshToken: String? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    var role: Role = role
        protected set

    @OneToMany(mappedBy = "author", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var writtenPosts: MutableSet<PostEntity> = mutableSetOf()
        protected set

    @OneToMany(mappedBy = "author", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var writtenComments: MutableSet<CommentEntity> = mutableSetOf()
        protected set

    @OneToMany(mappedBy = "user", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var likedPosts: MutableSet<UserLikePost> = mutableSetOf()
        protected set

    fun update(username: String = this.username, password: String = this.password) {
        this.username = username
        this.password = password
    }

    fun isAdminOrStaff(): Boolean = this.isStaff||this.isAdmin

    fun setAdmin() {
        isStaff = false
        isAdmin = true
    }

    fun setStaff() {
        isStaff = true
        isAdmin = false
    }

    fun setRegular() {
        isStaff = false
        isAdmin = false
    }

    fun updateRole(role: Role) {
        this.role.users.remove(this)
        this.role = role
        role.users.add(this)
    }

    fun invalidateRefreshToken() {
        refreshToken = null
    }

}