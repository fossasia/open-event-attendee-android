package org.fossasia.openevent.general.social

import android.arch.persistence.room.*
import io.reactivex.Single

@Dao
interface SocialLinksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSocialLinks(socialLinks: List<SocialLink>)

    @Query("DELETE FROM SocialLink")
    fun deleteAll()

    @Query("SELECT * from SocialLink WHERE event = :eventId")
    fun getAllSocialLinks(eventId: Long): Single<List<SocialLink>>
}