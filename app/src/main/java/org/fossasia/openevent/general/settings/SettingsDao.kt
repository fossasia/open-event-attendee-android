package org.fossasia.openevent.general.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Single

@Dao
interface SettingsDao {

    @Insert(onConflict = REPLACE)
    fun insertSettings(settings: Settings)

    @Query("SELECT * FROM Settings")
    fun getSettings(): Single<Settings>
}
