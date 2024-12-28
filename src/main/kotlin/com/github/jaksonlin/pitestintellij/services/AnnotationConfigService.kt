package com.github.jaksonlin.pitestintellij.services
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Service(Service.Level.APP)
@State(
    name = "AnnotationConfig",
    storages = [Storage("pitestAnnotationConfig.xml")]
)
class AnnotationConfigService : PersistentStateComponent<AnnotationConfigService.State> {
    private val LOG = Logger.getInstance(AnnotationConfigService::class.java)

    data class State(
        var schemaJson: String = AnnotationSchema.DEFAULT_SCHEMA,
        var annotationPackage: String = "com.example.unittest.annotations",
        var autoImport: Boolean = true
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        LOG.info("Loading annotation config: ${state.schemaJson}")
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
        LOG.info("Updated annotation config: ${myState.schemaJson}")
    }

    fun getBuildInSchema(): AnnotationSchema {
        return Json.decodeFromString(AnnotationSchema.DEFAULT_SCHEMA)
    }

    // New methods for import configuration
    fun getAnnotationPackage(): String = myState.annotationPackage
    
    fun setAnnotationPackage(packageName: String) {
        myState.annotationPackage = packageName
        LOG.info("Updated annotation package: $packageName")
    }

    fun isAutoImport(): Boolean = myState.autoImport
    
    fun setAutoImport(auto: Boolean) {
        myState.autoImport = auto
        LOG.info("Updated auto import setting: $auto")
    }
}