package com.hasanozgan.komandante.todomvc.domain

import com.hasanozgan.komandante.Command

sealed class TodoListCommand(override val aggregateID: TodoListID) : Command()
data class CreateCommand(val todolistID: TodoListID) : TodoListCommand(todolistID)
data class DeleteCommand(val todolistID: TodoListID) : TodoListCommand(todolistID)
data class AddItemCommand(val todolistID: TodoListID, val description: String) : TodoListCommand(todolistID)
data class RemoveItemCommand(val todolistID: TodoListID, val todoItemID: TodoItemID) : TodoListCommand(todolistID)
data class RemoveCompletedItemsCommand(val todolistID: TodoListID) : TodoListCommand(todolistID)
data class SetItemDescriptionCommand(val todolistID: TodoListID, val todoItemID: TodoItemID, val description: String) : TodoListCommand(todolistID)
data class CheckItemCommand(val todolistID: TodoListID, val todoItemID: TodoItemID, val checked: Boolean) : TodoListCommand(todolistID)
data class CheckAllItemsCommand(val todolistID: TodoListID, val checked: Boolean) : TodoListCommand(todolistID)