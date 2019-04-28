package schema2json

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "Schema2JsonState",
    storages = [Storage("Schema2JsonState.xml")]
)
class Schema2JsonState : PersistentStateComponent<Schema2JsonState> {

    var url: String? = null
    var user: String? = null
    var schema: String? = null
    var driverPath: String? = null
    var outputDirectory: String? = null
    var postHookCommand: String? = null
    var rememberPassword: Boolean = false

    override fun getState(): Schema2JsonState? {
        return this
    }

    override fun loadState(state: Schema2JsonState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): Schema2JsonState {
            return ServiceManager.getService(Schema2JsonState::class.java)
        }
    }
}
