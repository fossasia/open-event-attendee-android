package org.fossasia.openevent.general.sponsor

object SponsorUtil {
    fun sortSponsorByLevel(sponsors: List<Sponsor>): List<Sponsor> {
        return sponsors.sortedWith(compareBy(Sponsor::level))
    }
}
