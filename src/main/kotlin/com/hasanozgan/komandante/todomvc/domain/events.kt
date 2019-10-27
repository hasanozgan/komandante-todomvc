package com.hasanozgan.komandante.todomvc.domain

import com.hasanozgan.komandante.Event

sealed class TodoListEvent(override val aggregateID: TodoListID) : Event()
data class Created(val todolistID: TodoListID) : TodoListEvent(todolistID)
data class Deleted(val todolistID: TodoListID) : TodoListEvent(todolistID)
data class ItemAdded(val todolistID: TodoListID, val todoItemID: TodoItemID, val description: String) : TodoListEvent(todolistID)
data class ItemRemoved(val todolistID: TodoListID, val todoItemID: TodoItemID) : TodoListEvent(todolistID)
data class CompletedItemsRemoved(val todolistID: TodoListID) : TodoListEvent(todolistID)
data class AllItemsChecked(val todolistID: TodoListID, val checked: Boolean) : TodoListEvent(todolistID)
data class ItemDescriptionSet(val todolistID: TodoListID, val todoItemID: TodoItemID, val description: String) : TodoListEvent(todolistID)
data class ItemChecked(val todolistID: TodoListID, val todoItemID: TodoItemID, val checked: Boolean) : TodoListEvent(todolistID)
