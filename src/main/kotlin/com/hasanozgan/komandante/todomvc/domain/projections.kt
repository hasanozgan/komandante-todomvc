package com.hasanozgan.komandante.todomvc.domain

import org.jetbrains.exposed.dao.*
import java.util.*

object TodoRepo : UUIDTable() {
    var aggregateID = uuid("aggregate_id")
    var version = integer("version")
    var createdAt = datetime("created_at")
    var updatedAt = datetime("updated_at")
}

class TodoEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TodoEntity>(TodoRepo)

    var version by TodoRepo.version
    var createAt by TodoRepo.createdAt
    var updatedAt by TodoRepo.updatedAt
}

object TodoItemRepo : IntIdTable() {
    var index = integer("index")
    var description = varchar("description", length = 255)
    var completed = bool("completed")
    val todoListID = (uuid("todolist_id") references TodoRepo.aggregateID).nullable()
}

class TodoItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TodoItemEntity>(TodoItemRepo)

    var index by TodoItemRepo.index
    var description by TodoItemRepo.description
    var completed by TodoItemRepo.completed
}