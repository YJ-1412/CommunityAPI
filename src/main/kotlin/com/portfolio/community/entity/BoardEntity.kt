package com.portfolio.community.entity

import jakarta.persistence.*

@Entity
class BoardEntity(
    name: String,
    priority: Int,
    readableRole: Role,
    id: Long = 0
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    var id: Long = id
        protected set

    @Column(nullable = false, unique = true)
    var name: String = name
        protected set

    @Column(nullable = false, unique = true)
    var priority: Int = priority
        protected set

    @OneToMany(mappedBy = "board", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var posts: MutableSet<PostEntity> = mutableSetOf()
        protected set

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    var readableRole: Role = readableRole
        protected set

    fun update(name: String = this.name, priority: Int = this.priority, readableRole: Role = this.readableRole) {
        this.name = name
        this.priority = priority

        this.readableRole.readableBoards.remove(this)
        this.readableRole = readableRole
        readableRole.readableBoards.add(this)
    }
}