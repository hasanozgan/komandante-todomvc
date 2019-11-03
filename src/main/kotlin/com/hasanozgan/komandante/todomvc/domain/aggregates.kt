package com.hasanozgan.komandante.todomvc.domain

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import arrow.data.extensions.list.foldable.exists
import com.hasanozgan.komandante.*

data class TodoItem(val itemID: Int, var description: String, var completed: Boolean)

class TodoListAggregate(override var id: TodoListID) : Aggregate() {
    var created: Boolean = false
    var nextItemID = 1
    var items = mutableListOf<TodoItem>()

    fun handle(command: CreateCommand): Validated<DomainError, Event> {
        if (created) return Invalid(DomainError("todoList is not created"))

        return Valid(Created(command.aggregateID))
    }

    fun handle(command: DeleteCommand): Validated<DomainError, Event> {
        if (!created) return Invalid(DomainError("todoList is not created"))

        return Valid(Deleted(command.aggregateID))
    }

    fun handle(command: AddItemCommand): Validated<DomainError, Event> {
        return Valid(ItemAdded(command.todolistID, nextItemID, command.description))
    }

    fun handle(command: RemoveItemCommand): Validated<DomainError, Event> {
        if (!created) return Invalid(DomainError("todoList is not created"))

        var record = items.find { it.itemID == command.todoItemID }
        if (record == null) {
            return Invalid(DomainError("item does not exists: ${command.todoItemID}"))
        }

        return Valid(ItemRemoved(command.aggregateID, command.todoItemID))
    }

    fun handle(command: RemoveCompletedItemsCommand): Validated<DomainError, List<Event>> {
        if (!created) return Invalid(DomainError("todoList is not created"))

        if (!items.exists { it.completed }) {
            return Invalid(DomainError("completed items not found"))
        }

        return Valid(items.filter { it.completed }.map { ItemRemoved(command.todolistID, it.itemID) }.toList())
    }

    fun handle(command: SetItemDescriptionCommand): Validated<DomainError, Event> {
        if (!created) return Invalid(DomainError("todoList is not created"))

        items.find { it.itemID == command.todoItemID }
                ?: return Invalid(DomainError("item does not exist: ${command.todoItemID}"))

        return Valid(ItemDescriptionSet(command.aggregateID, command.todoItemID, command.description))
    }

    fun handle(command: CheckItemCommand): Validated<DomainError, Event> {
        if (!created) return Invalid(DomainError("todoList is not created"))

        val item = items.find { it.itemID == command.todoItemID }
        item ?: return Invalid(DomainError("item does not exist: ${command.todoItemID}"))

        if (item.completed == command.checked) {
            return Invalid(DomainError("item is not changed"))
        }

        return Valid(ItemChecked(command.aggregateID, item.itemID, command.checked))
    }

    fun handle(command: CheckAllItemsCommand): Validated<DomainError, List<Event>> {
        if (!created) return Invalid(DomainError("todoList is not created"))

        val eventList = items.filter {
            it.completed != command.checked
        }.map {
            ItemChecked(command.aggregateID, it.itemID, command.checked)
        }.toList()

        return Valid(eventList)
    }

    fun apply(event: Created) {
        this.created = true
    }

    fun apply(event: Deleted) {
        this.created = false
    }

    fun apply(event: ItemAdded) {
        items.add(TodoItem(nextItemID, event.description, false))
        nextItemID++
    }

    fun apply(event: ItemRemoved): Option<DomainError> {
        if (items.removeIf { it.itemID == event.todoItemID }) {
            return None
        }
        return Some(DomainError("todo item is not exists: ${event.todoItemID}"))
    }

    fun apply(event: ItemDescriptionSet): Option<DomainError> {
        val item = items.find { it.itemID == event.todoItemID }
                ?: return Some(DomainError("item is not exists: ${event.todoItemID} "))

        val index = items.indexOf(item)
        item.description = event.description
        items.set(index, item)
        return None
    }

    fun apply(event: ItemChecked): Option<DomainError> {
        val item = items.find { it.itemID == event.todoItemID }
                ?: return Some(DomainError("item is not exists: ${event.todoItemID} "))

        val index = items.indexOf(item)
        item.completed = event.checked
        items.set(index, item)
        return None
    }
}

class TodoListAggregateFactory : AggregateFactory<TodoListCommand, TodoListEvent> {
    override fun create(aggregateID: AggregateID): Aggregate {
        return TodoListAggregate(aggregateID)
    }
}