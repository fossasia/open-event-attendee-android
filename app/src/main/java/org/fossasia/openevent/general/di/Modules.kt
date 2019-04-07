package org.fossasia.openevent.general.di

import androidx.room.Room
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.OpenEventDatabase
import org.fossasia.openevent.general.about.AboutEventViewModel
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.attendees.AttendeeApi
import org.fossasia.openevent.general.attendees.AttendeeId
import org.fossasia.openevent.general.attendees.AttendeeService
import org.fossasia.openevent.general.attendees.AttendeeViewModel
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.auth.AuthApi
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.AuthService
import org.fossasia.openevent.general.auth.EditProfileViewModel
import org.fossasia.openevent.general.auth.LoginViewModel
import org.fossasia.openevent.general.auth.ProfileViewModel
import org.fossasia.openevent.general.auth.RequestAuthenticator
import org.fossasia.openevent.general.auth.SignUp
import org.fossasia.openevent.general.auth.SignUpViewModel
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventApi
import org.fossasia.openevent.general.event.EventDetailsViewModel
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventLayoutType
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.common.EventsDiffCallback
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.EventsListAdapter
import org.fossasia.openevent.general.event.EventsViewModel
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.topic.EventTopicApi
import org.fossasia.openevent.general.event.types.EventType
import org.fossasia.openevent.general.event.types.EventTypesApi
import org.fossasia.openevent.general.event.topic.SimilarEventsViewModel
import org.fossasia.openevent.general.favorite.FavoriteEventsRecyclerAdapter
import org.fossasia.openevent.general.favorite.FavoriteEventsViewModel
import org.fossasia.openevent.general.order.Charge
import org.fossasia.openevent.general.order.ConfirmOrder
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderApi
import org.fossasia.openevent.general.order.OrderCompletedViewModel
import org.fossasia.openevent.general.order.OrderDetailsViewModel
import org.fossasia.openevent.general.order.OrderService
import org.fossasia.openevent.general.order.OrdersUnderUserViewModel
import org.fossasia.openevent.general.paypal.Paypal
import org.fossasia.openevent.general.paypal.PaypalApi
import org.fossasia.openevent.general.search.GeoLocationViewModel
import org.fossasia.openevent.general.search.SearchLocationViewModel
import org.fossasia.openevent.general.search.SearchViewModel
import org.fossasia.openevent.general.search.LocationService
import org.fossasia.openevent.general.search.SearchTypeViewModel
import org.fossasia.openevent.general.search.LocationServiceImpl
import org.fossasia.openevent.general.settings.SettingsViewModel
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.social.SocialLinkApi
import org.fossasia.openevent.general.social.SocialLinksService
import org.fossasia.openevent.general.social.SocialLinksViewModel
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketApi
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.ticket.TicketService
import org.fossasia.openevent.general.ticket.TicketsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

val commonModule = module {
    single { Preference() }
    single { Network() }
    factory<LocationService> { LocationServiceImpl(androidContext()) }
}

val apiModule = module {
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(TicketApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(SocialLinkApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventTopicApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(AttendeeApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(OrderApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(PaypalApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventTypesApi::class.java)
    }

    factory { AuthHolder(get()) }
    factory { AuthService(get(), get(), get()) }

    factory { EventService(get(), get(), get(), get(), get()) }
    factory { TicketService(get(), get()) }
    factory { SocialLinksService(get(), get()) }
    factory { AttendeeService(get(), get(), get()) }
    factory { OrderService(get(), get(), get()) }
    factory { Resource() }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { EventsViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get(), get()) }
    viewModel { EventDetailsViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get(), get(), get()) }
    viewModel { AttendeeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SearchLocationViewModel(get()) }
    viewModel { SearchTypeViewModel(get()) }
    viewModel { TicketsViewModel(get(), get(), get(), get()) }
    viewModel { AboutEventViewModel(get(), get()) }
    viewModel { SocialLinksViewModel(get(), get()) }
    viewModel { FavoriteEventsViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { SimilarEventsViewModel(get(), get()) }
    viewModel { OrderCompletedViewModel(get(), get()) }
    viewModel { OrdersUnderUserViewModel(get(), get(), get(), get()) }
    viewModel { OrderDetailsViewModel(get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get(), get()) }
    viewModel { GeoLocationViewModel(get()) }
}

val networkModule = module {

    single {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper
    }

    single { RequestAuthenticator(get()) as Interceptor }

    single {
        val connectTimeout = 15 // 15s
        val readTimeout = 15 // 15s

        OkHttpClient().newBuilder()
            .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            )
            .addInterceptor(get())
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }

    single {
        val baseUrl = BuildConfig.DEFAULT_BASE_URL
        val objectMapper: ObjectMapper = get()

        Retrofit.Builder()
            .client(get())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(JSONAPIConverterFactory(objectMapper, Event::class.java, User::class.java,
                SignUp::class.java, Ticket::class.java, SocialLink::class.java, EventId::class.java,
                EventTopic::class.java, Attendee::class.java, TicketId::class.java, Order::class.java,
                AttendeeId::class.java, Charge::class.java, Paypal::class.java, ConfirmOrder::class.java,
                CustomForm::class.java, EventType::class.java))
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .baseUrl(baseUrl)
            .build()
    }
}

val databaseModule = module {

    single {
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
        database.ticketDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.socialLinksDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.attendeeDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.eventTopicsDao()
    }

    factory {
        val database: OpenEventDatabase = get()
        database.orderDao()
    }
}

val fragmentsModule = module {

    factory { EventsDiffCallback() }

    scope(Scopes.EVENTS_FRAGMENT.toString()) {
        EventsListAdapter(EventLayoutType.EVENTS, get())
    }

    scope(Scopes.SIMILAR_EVENTS_FRAGMENT.toString()) {
        EventsListAdapter(EventLayoutType.SIMILAR_EVENTS, get())
    }

    scope(Scopes.FAVORITE_FRAGMENT.toString()) {
        FavoriteEventsRecyclerAdapter(get())
    }

    scope(Scopes.SEARCH_RESULTS_FRAGMENT.toString()) {
        FavoriteEventsRecyclerAdapter(get())
    }
}
