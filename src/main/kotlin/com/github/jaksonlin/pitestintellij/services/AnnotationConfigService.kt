import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Service(Service.Level.APP)
class AnnotationConfigService : PersistentStateComponent<AnnotationConfigService.State> {
    data class State(
        var schemaJson: String = AnnotationSchema.DEFAULT_SCHEMA
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun getSchema(): AnnotationSchema {
        return try {
            Json.decodeFromString(myState.schemaJson)
        } catch (e: Exception) {
            Json.decodeFromString(AnnotationSchema.DEFAULT_SCHEMA)
        }
    }

    fun updateSchema(schema: AnnotationSchema) {
        myState.schemaJson = Json.encodeToString(schema)
    }
}