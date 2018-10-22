package org.fossasia.openevent.general.social

import io.reactivex.Flowable

class SocialLinksService(
    private val socialLinkApi: SocialLinkApi,
    private val socialLinksDao: SocialLinksDao
) {

    fun getSocialLinks(id: Long): Flowable<List<SocialLink>> {

        val socialFlowable = socialLinksDao.getAllSocialLinks(id)
        return socialFlowable.switchMap {
            if (it.isNotEmpty())
                socialFlowable
            else
                socialLinkApi.getSocialLinks(id)
                        .map {
                            socialLinksDao.insertSocialLinks(it)
                        }
                        .flatMap {
                            socialFlowable
                        }
        }
    }
}
