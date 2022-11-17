import json
import traceback


# TODO: Update this table if any new support type been added.

INIT_VALUE_TABLE = {
    'String': '""',
    'Float': '0.0f',
    'Long': '0L',
    'Int': '0'
}


GL_FILE_TEMPLATE = """package glcore

import kotlin.reflect.KClass

/*****************************************************************************************
 *
 * This file is generated by GlDataDeclareGen.py
 * You should update GlDataDeclare.json instead of updating this file by manual.
 *
 * ***************************************************************************************/
<<class_declare_area>>
"""


GL_DATA_CLASS_TEMPLATE = """
// -------------------------------------------------------------------------------------------------

open class <<class_name>> : IGlDeclare() {

    val structDeclare = mapOf< String, KClass< * > >(
<<struct_dec_area>>
    )
    
<<member_area>>

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = toAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, structDeclare)) {
<<from_any_struct_area>>
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
<<to_any_struct_area>>
        )
    }
}
"""

try:
    data_declare_codes = []
    with open('GlDataDeclare.json', 'rt', encoding='utf-8') as f:
        json_dict = json.load(f)
        for class_name, class_declare in json_dict.items():
            member_area = []
            struct_dec_area = []
            from_any_struct_area = []
            to_any_struct_area = []
            for member, member_type in class_declare.items():
                member_area.append('    var %s: %s = %s' % (member, member_type, INIT_VALUE_TABLE[member_type]))
                struct_dec_area.append('        "%s" to %s::class' % (member, member_type))
                from_any_struct_area.append('            %s = anyStruct.get("%s") as %s' % (member, member, member_type))
                to_any_struct_area.append('            "%s" to %s' % (member, member))
            data_declare_code = GL_DATA_CLASS_TEMPLATE.\
                replace('<<class_name>>', class_name).\
                replace('<<member_area>>', '\n'.join(member_area)).\
                replace('<<struct_dec_area>>', ', \n'.join(struct_dec_area)).\
                replace('<<from_any_struct_area>>', '\n'.join(from_any_struct_area)).\
                replace('<<to_any_struct_area>>', ', \n'.join(to_any_struct_area))
            print(data_declare_code)
            data_declare_codes.append(data_declare_code)
    with open('GlDataDeclare.kt', 'wt') as f:
        f.write( GL_FILE_TEMPLATE.replace('<<class_declare_area>>', ''.join(data_declare_codes)))

except Exception as e:
    print(str(e))
    print(traceback.format_exc())
finally:
    pass
