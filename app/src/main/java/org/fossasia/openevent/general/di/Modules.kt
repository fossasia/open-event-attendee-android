package org.fossasia.openevent.general.di

import android.arch.persistence.room.Room
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.fossasia.openevent.general.OpenEventDatabase
import org.fossasia.openevent.general.about.AboutEventViewModel
import org.fossasia.openevent.general.auth.*
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.*
import org.fossasia.openevent.general.favorite.FavouriteEventsViewModel
import org.fossasia.openevent.general.search.SearchViewModel
import org.fossasia.openevent.general.settings.SettingsFragmentViewModel
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.social.SocialLinkApi
import org.fossasia.openevent.general.social.SocialLinksService
import org.fossasia.openevent.general.social.SocialLinksViewModel
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketApi
import org.fossasia.openevent.general.ticket.TicketService
import org.fossasia.openevent.general.ticket.TicketsViewModel
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


val commonModule = applicationContext {
    bean { Preference() }
}

val apiModule = applicationContext {
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(EventApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(TicketApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(SocialLinkApi::class.java)
    }
    bean {
        val retrofit: Retrofit = get()
        retrofit.create(EventTopicApi::class.java)
    }

    factory { AuthHolder(get()) }
    factory { AuthService(get(), get(), get()) }

    factory { EventService(get(), get()) }
    factory { TicketService(get(), get()) }
    factory { SocialLinksService(get(), get()) }
    factory { EventTopicService(get()) }

}

val viewModelModule = applicationContext {
    viewModel { LoginFragmentViewModel(get()) }
    viewModel { EventsViewModel(get(), get()) }
    viewModel { ProfileFragmentViewModel(get()) }
    viewModel { SignUpFragmentViewModel(get()) }
    viewModel { EventDetailsViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { TicketsViewModel(get()) }
    viewModel { AboutEventViewModel(get()) }
    viewModel { SocialLinksViewModel(get()) }
    viewModel { FavouriteEventsViewModel(get()) }
    viewModel { SettingsFragmentViewModel(get()) }
}

val networkModule = applicationContext {

    bean {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper
    }

    bean { RequestAuthenticator(get()) as Authenticator }

    bean {
        val connectTimeout = 15 // 15s
        val readTimeout = 15 // 15s

        OkHttpClient().newBuilder()
                .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
                .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
                .addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .authenticator(get())
                .build()
    }

    bean {
        val baseUrl = "https://open-event-api-dev.herokuapp.com/v1/"
        val objectMapper: ObjectMapper = get()

        Retrofit.Builder()
                .client(get())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JSONAPIConverterFactory(objectMapper, Event::class.java, User::class.java, SignUp::class.java, Ticket::class.java, SocialLink::class.java, EventId::class.java, EventTopic::class.java))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(baseUrl)
                .build()
    }

}

val databaseModule = applicationContext {

    bean {
        Room.databaseBuilder(androidApplication(),
                OpenEventDatabase::class.java, "open_event_database")
                .fallbackToDestructiveMigration()
                .build()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.eventDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.userDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.ticketsDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.socialLinksDao()
    }
}