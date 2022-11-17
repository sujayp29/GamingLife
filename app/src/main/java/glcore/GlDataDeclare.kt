package glcore

import kotlin.reflect.KClass

/*****************************************************************************************
 *
 * This file is generated by GlDataDeclareGen.py
 * You should update GlDataDeclare.json instead of updating this file by manual.
 *
 * ***************************************************************************************/

// -------------------------------------------------------------------------------------------------

class TaskData() : IGlDeclare {

    val structDeclare = mapOf< String, KClass< * > >(
        "id" to String::class, 
        "name" to String::class, 
        "color" to String::class
    )
    
    var id: String = ""
    var name: String = ""
    var color: String = ""

    override var dataValid: Boolean = false

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = toAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, structDeclare)) {
            id = anyStruct.get("id") as String
            name = anyStruct.get("name") as String
            color = anyStruct.get("color") as String
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
            "id" to id, 
            "name" to name, 
            "color" to color
        )
    }
}

// -------------------------------------------------------------------------------------------------

class TaskRecord() : IGlDeclare {

    val structDeclare = mapOf< String, KClass< * > >(
        "taskID" to String::class, 
        "groupID" to String::class, 
        "startTime" to Long::class
    )
    
    var taskID: String = ""
    var groupID: String = ""
    var startTime: Long = 0L

    override var dataValid: Boolean = false

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = toAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, structDeclare)) {
            taskID = anyStruct.get("taskID") as String
            groupID = anyStruct.get("groupID") as String
            startTime = anyStruct.get("startTime") as Long
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
            "taskID" to taskID, 
            "groupID" to groupID, 
            "startTime" to startTime
        )
    }
}

