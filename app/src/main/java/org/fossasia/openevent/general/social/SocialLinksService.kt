package org.fossasia.openevent.general.social

import io.reactivex.Single

class SocialLinksService(private val socialLinkApi: SocialLinkApi, private val socialLinksDao: SocialLinksDao) {

    fun getSocialLinks(id: Long): Single<List<SocialLink>> {

        val socialSingle = socialLinksDao.getAllSocialLinks(id)
        return socialSingle.flatMap {
            if (it.isNotEmpty())
                socialSingle
            else
                socialLinkApi.getSocialLinks(id)
                        .map {
                            socialLinksDao.insertSocialLinks(it)
                        }
                        .flatMap {
                            socialSingle
                        }
        }
    }
}