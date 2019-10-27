package com.hasanozgan.komandante.todomvc.domain

import arrow.core.Option
import arrow.core.none
import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.DomainError
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.Projector
import com.hasanozgan.komandante.todomvc.domain.TodoRepo.aggregateID
import com.hasanozgan.komandante.todomvc.domain.TodoRepo.version
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.*

class TodoListProjector : Projector<TodoListEvent>() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun project(event: Event): Option<Command> {
        println(DomainError("Event ${event} is not projected"))
        return none()
    }

    fun project(event: Created) {
        transaction {
            val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
            if (query.empty()) {
                TodoRepo.insert {
                    it[aggregateID] = event.aggregateID
                    it[createdAt] = DateTime.now()
                    it[updatedAt] = DateTime.now()
                    it[version] = event.version
                }
                commit()
            } else {
                logger.error("todo is already exists")
            }
        }
    }

    fun project(event: Deleted) {
//        transaction {
//            val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
//        }
    }

    fun project(event: ItemAdded) {
        transaction {
            val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
            query.filterNot {
                it[version] >= event.version
            }.forEach { row ->
                TodoRepo.update({ aggregateID.eq(event.aggregateID) }, 1, {
                    it[version] = event.version
                    it[updatedAt] = DateTime.now()
                })
                TodoItemRepo.insert {
                    it[index] = event.todoItemID
                    it[todoListID] = event.aggregateID
                    it[description] = event.description
                    it[completed] = false
                }
                commit()
            }
        }

        fun project(event: ItemRemoved) {
            transaction {
                val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
                query.filterNot {
                    it[version] >= event.version
                }.forEach { row ->
                    TodoRepo.update({ aggregateID.eq(event.aggregateID) }, 1, {
                        it[version] = event.version
                        it[updatedAt] = DateTime.now()
                    })
                    TodoItemRepo.deleteWhere {
                        TodoRepo.id.eq(event.todolistID).and(TodoItemRepo.index.eq(event.todoItemID))
                    }
                    commit()
                }
            }
        }

        fun project(event: CompletedItemsRemoved) {
            transaction {
                val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
                query.filterNot {
                    it[version] >= event.version

                }.forEach { row ->
                    TodoRepo.update({ aggregateID.eq(event.aggregateID) }, 1, {
                        it[version] = event.version
                        it[updatedAt] = DateTime.now()
                    })
                    TodoItemRepo.deleteWhere {
                        TodoRepo.id.eq(event.todolistID).and(TodoItemRepo.completed.eq(true))
                    }
                }
            }
        }

        fun project(event: ItemDescriptionSet) {
            transaction {
                val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
                query.filterNot {
                    it[version] >= event.version
                }.forEach { row ->
                    TodoRepo.update({ aggregateID.eq(event.aggregateID) }, 1, {
                        it[version] = event.version
                        it[updatedAt] = DateTime.now()
                    })
                    TodoItemRepo.update({ TodoRepo.id.eq(event.aggregateID).and(TodoItemRepo.index.eq(event.todoItemID)) }, 1, {
                        it[description] = event.description
                    })
                    commit()
                }
            }
        }

        fun project(event: ItemChecked) {
            transaction {
                val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
                query.filterNot {
                    it[version] >= event.version
                }.forEach { row ->
                    TodoRepo.update({ aggregateID.eq(event.aggregateID) }, 1, {
                        it[version] = event.version
                        it[updatedAt] = DateTime.now()
                    })
                    TodoItemRepo.update({ TodoRepo.id.eq(event.aggregateID).and(TodoItemRepo.index.eq(event.todoItemID)) }, 1, {
                        it[completed] = event.checked
                    })
                    commit()
                }
            }
        }

        fun project(event: AllItemsChecked) {
            transaction {
                val query = TodoRepo.select { aggregateID.eq(event.aggregateID) }
                query.filterNot {
                    it[version] >= event.version
                }.forEach { row ->
                    TodoRepo.update({ aggregateID.eq(event.aggregateID) }, 1, {
                        it[version] = event.version
                        it[updatedAt] = DateTime.now()
                    })
                    TodoItemRepo.update({ TodoRepo.id.eq(event.aggregateID) }, 1, {
                        it[completed] = true
                    })
                    commit()
                }
            }
        }
    }
}