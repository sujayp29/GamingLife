package glcore
import kotlin.reflect.KClass

// --------------------------------------------- Path ----------------------------------------------

const val PATH_TASK_GROUP_TOP = "/Config/Meta/TaskGroup/TopGroups"
const val PATH_TASK_GROUP_SUB = "/Config/Meta/TaskGroup/SubGroups"
const val PATH_TASK_GROUP_LINK = "/Config/Meta/TaskGroup/GroupLinks"
const val PATH_TASK_GROUP_COLOR = "/Config/Meta/TaskGroup/GroupColor"

const val PATH_CURRENT_TASK = "/Runtime/TimeModule/CurrentTask"


// --------------------------------------------- Value ---------------------------------------------

const val GROUP_ID_RELAX  = "5084b76d-4e75-4c44-9786-bdf94075f94d"
const val GROUP_ID_ENJOY  = "f9fb401a-dc28-463f-92a6-0d30bd8730bb"
const val GROUP_ID_LIFE   = "3e9fd903-9c51-4301-b610-715205983573"
const val GROUP_ID_WORK   = "11000041-0376-4876-9efa-8a6a7028140d"
const val GROUP_ID_STUDY  = "1841978a-3adc-413a-a9ae-a34e019205f8"
const val GROUP_ID_CREATE = "fa94a546-beeb-4570-b266-c066a4a31233"


// ---------------------------------------- Struct Defines -----------------------------------------

val STRUCT_DEC_TASK_DATA = mapOf< String, KClass< * > >(
    "id" to String::class,
    "name" to String::class,
    "color" to String::class
)

val STRUCT_DEC_TASK_RECORD = mapOf< String, KClass< * > >(
    "taskID" to String::class,
    "groupID" to String::class,
    "startTime" to Long::class
)

fun checkStruct(structDict: Map< String, Any >,
                structDeclare: Map< String, KClass< * > >) : Boolean {
    for ((k, v) in structDeclare) {
        if (v == Any::class) {
            // Accept all
            continue
        }
        if ((structDict[k] == null) || (structDict[k]!!::class != v)) {
            System.out.println(
                "Structure mismatch: Field [$k], Expect [$v], But [${structDict[k]}]")
            return false
        }
    }
    return true;
}

fun checkListOfStruct(structDictList: List< Map< String, Any > >,
                      structDeclare: Map< String, KClass< * > >) : Boolean {
    for (structDict in structDictList) {
        if (!checkStruct(structDict, structDeclare)) {
            return false
        }
    }
    return true
}


// -------------------------------------------- Preset ---------------------------------------------

// https://material.io/design/color/the-color-system.html#tools-for-picking-colors

val TASK_GROUP_TOP_PRESET = mapOf(

    GROUP_ID_RELAX to mapOf(
        "id" to GROUP_ID_RELAX,
        "name" to "放松",
        "color" to "#BBDEFB"),      // Blue 100

    GROUP_ID_ENJOY to mapOf(
        "id" to GROUP_ID_ENJOY,
        "name" to "玩乐",
        "color" to "#FBBC05"),      // Yellow

    GROUP_ID_LIFE to mapOf(
        "id" to GROUP_ID_LIFE,
        "name" to "生活",
        "color" to "#34A853"),      // Green

    GROUP_ID_WORK to mapOf(
        "id" to GROUP_ID_WORK,
        "name" to "工作",
        "color" to "#EA4335"),      // Red

    GROUP_ID_STUDY to mapOf(
        "id" to GROUP_ID_STUDY,
        "name" to "学习",
        "color" to "#F9A825"),      // Yellow 800

    GROUP_ID_CREATE to mapOf(
        "id" to GROUP_ID_CREATE,
        "name" to "创作",
        "color" to "#4485F4"),      // Blue
)


val TASK_RECORD_TEMPLATE = mapOf(
    "taskID" to "",
    "groupID" to GROUP_ID_RELAX,
    "startTime" to System.currentTimeMillis()
)


