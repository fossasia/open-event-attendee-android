package org.fossasia.openevent.general.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_notification.view.notificationRecycler
import kotlinx.android.synthetic.main.fragment_notification.view.swiperefresh
import kotlinx.android.synthetic.main.fragment_notification.view.shimmerNotifications
import kotlinx.android.synthetic.main.fragment_notification.view.notificationCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_notification.view.noNotification
import kotlinx.android.synthetic.main.fragment_notification.view.filterChipGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.LoginFragmentArgs
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.NEW_NOTIFICATIONS
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val NOTIFICATION_FRAGMENT = "notificationFragment"

class NotificationFragment : Fragment() {

    private val notificationViewModel by viewModel<NotificationViewModel>()
    private val recyclerAdapter = NotificationsRecyclerAdapter()
    private lateinit var rootView: View
    private val preference = Preference()
    private var showAll = false

    override fun onStart() {
        super.onStart()
        if (!notificationViewModel.isLoggedIn()) {
            redirectToLogin()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_notification, container, false)
        setToolbar(activity, getString(R.string.title_notifications), true)
        setHasOptionsMenu(true)

        setFilterChips()

        if (notificationViewModel.isLoggedIn()) {
            initObservers()
            if (notificationViewModel.notifications.value == null) {
                notificationViewModel.getNotifications(showAll)
            }
            rootView.notificationRecycler.layoutManager = LinearLayoutManager(requireContext())
            rootView.notificationRecycler.adapter = recyclerAdapter
            preference.putBoolean(NEW_NOTIFICATIONS, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView.retry.setOnClickListener {
            notificationViewModel.getNotifications(showAll)
        }

        rootView.swiperefresh.setOnRefreshListener {
            notificationViewModel.getNotifications(showAll)
        }
    }

    private fun setFilterChips() {
        val newContext = ContextThemeWrapper(context, R.style.CustomChipChoice)
        val chipUnread = Chip(newContext)
        val chipAll = Chip(newContext)
        chipUnread.text = getString(R.string.unread)
        chipUnread.isCheckable = true
        chipUnread.isChecked = true
        chipUnread.isClickable = true
        chipAll.text = getString(R.string.all)
        chipAll.isCheckable = true
        chipAll.isChecked = false
        chipAll.isClickable = true
        chipUnread.setOnCheckedChangeListener { _, isChecked ->
            chipAll.isChecked = !isChecked
            showAll = !isChecked
            notificationViewModel.getNotifications(showAll)
        }
        chipAll.setOnCheckedChangeListener { _, isChecked ->
            chipUnread.isChecked = !isChecked
            showAll = isChecked
            notificationViewModel.getNotifications(showAll)
        }
        rootView.filterChipGroup.addView(chipUnread)
        rootView.filterChipGroup.addView(chipAll)
    }

    private fun initObservers() {
        notificationViewModel.notifications
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                showNoNotifications(it.isEmpty())
                recyclerAdapter.addAll(it)
                recyclerAdapter.notifyDataSetChanged()
                notificationViewModel.updateReadStatus(it)
            })

        notificationViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.swiperefresh.isRefreshing = false
                rootView.notificationCoordinatorLayout.snackbar(it)
            })

        notificationViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it) {
                    rootView.shimmerNotifications.startShimmer()
                    rootView.noNotification.isVisible = false
                    rootView.notificationRecycler.isVisible = false
                } else {
                    rootView.shimmerNotifications.stopShimmer()
                    rootView.swiperefresh.isRefreshing = it
                }
                rootView.shimmerNotifications.isVisible = it
            })

        notificationViewModel.noInternet
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it) {
                    rootView.notificationCoordinatorLayout
                        .snackbar(resources.getString(R.string.no_internet_connection_message))
                    rootView.swiperefresh.isRefreshing = !it
                }
                showNoInternet(it && notificationViewModel.notifications.value.isNullOrEmpty())
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNoInternet(visible: Boolean) {
        rootView.noInternetCard.isVisible = visible
        rootView.notificationRecycler.isVisible = !visible
    }

    private fun showNoNotifications(visible: Boolean) {
        rootView.noNotification.isVisible = visible
        rootView.notificationRecycler.isVisible = !visible
    }

    private fun redirectToLogin() {
        LoginFragmentArgs(getString(R.string.log_in_first))
            .toBundle()
            .also {
                Navigation.findNavController(rootView).navigate(
                    NotificationFragmentDirections.actionNotificationToAuth(
                        getString(R.string.log_in_first),
                        NOTIFICATION_FRAGMENT)
                )
            }
    }
}
