package org.fossasia.openevent.general.social

import io.reactivex.Flowable

class SocialLinksService(private val socialLinkApi: SocialLinkApi) {

    fun getSocialLinks(id: Long): Flowable<List<SocialLink>> {
        return socialLinkApi.getSocialLinks(id)
    }
}