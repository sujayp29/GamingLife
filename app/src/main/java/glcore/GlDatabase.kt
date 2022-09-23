package glcore


val PRESET_TOP_CLASS = mapOf(
    "5084b76d-4e75-4c44-9786-bdf94075f94d" to "放松",
    "f9fb401a-dc28-463f-92a6-0d30bd8730bb" to "玩乐",
    "3e9fd903-9c51-4301-b610-715205983573" to "生活",
    "11000041-0376-4876-9efa-8a6a7028140d" to "工作",
    "1841978a-3adc-413a-a9ae-a34e019205f8" to "学习",
    "fa94a546-beeb-4570-b266-c066a4a31233" to "创作",
)


const val SEPERATOR = "/"
const val META_ROOT = "GL_META_ROOT"
const val DAILY_ROOT = "GL_DAILY_ROOT"
const val CONFIG_ROOT = "GL_CONFIG_ROOT"


class GlDatabase: GlObject() {


    fun getMetaData(path: String): Any {
        return ""
    }

    fun updateMetaData(path: String, data: Map< String, Any >) {

    }

    // ----------------------------------------------------------

    fun getDailyRecord(path: String): Any {
        return ""
    }

    fun updateDailyRecord(path: String, data: Map< String, Any >) {

    }

    // ----------------------------------------------------------

    fun getGlobalConfig(path: String): Any {
        return ""
    }

    fun updateGlobalConfig(path: String, data: Map< String, Any >) {

    }

    // ------------------------------------- Private Functions -------------------------------------

    private fun getData(path: String): Any? {
        return ""
    }
}

