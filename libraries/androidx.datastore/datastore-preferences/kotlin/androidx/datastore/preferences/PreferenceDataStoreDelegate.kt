package androidx.datastore.preferences

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Android's `preferencesDataStore` delegate: one store per name, in the app's files folder like on a TV. */
fun preferencesDataStore(
    name: String,
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    produceMigrations: (Context) -> List<DataMigration<Preferences>> = { listOf() },
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
): ReadOnlyProperty<Context, DataStore<Preferences>> = object : ReadOnlyProperty<Context, DataStore<Preferences>> {
    private val lock = Any()
    @Volatile private var store: DataStore<Preferences>? = null

    override fun getValue(thisRef: Context, property: KProperty<*>): DataStore<Preferences> = store ?: synchronized(lock) {
        store ?: PreferenceDataStoreFactory.create(corruptionHandler, produceMigrations(thisRef.applicationContext), scope) {
            File(thisRef.applicationContext.filesDir, "datastore/$name.preferences_pb")
        }.also { store = it }
    }
}
