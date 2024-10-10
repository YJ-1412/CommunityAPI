package com.portfolio.community.entity

import jakarta.persistence.*

@Entity(name = "roles")
class RoleEntity(
    name: String,
    level: Int,
    id: Long = 0
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = id
        protected set

    @Column(nullable = false, unique = true)
    var name: String = name
        protected set

    @Column(nullable = false, unique = true)
    var level: Int = level
        protected set

    @OneToMany(mappedBy = "readableRole")
    var readableBoards: MutableSet<BoardEntity> = mutableSetOf()
        protected set

    @OneToMany(mappedBy = "role")
    var users: MutableSet<UserEntity> = mutableSetOf()
        protected set

    fun update(name: String = this.name, level: Int = this.level) {
        this.name = name
        this.level = level
    }

}
