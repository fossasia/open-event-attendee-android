package org.fossasia.openevent.general.paypal

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_paypal_payment.view.webView
import kotlinx.android.synthetic.main.fragment_paypal_payment.view.loadingWebViewLayout
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaypalPaymentFragment : Fragment(), ComplexBackPressFragment {
    private lateinit var rootView: View
    private val safeArgs: PaypalPaymentFragmentArgs by navArgs()
    private val paypalWebViewModel by viewModel<PaypalWebViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_paypal_payment, container, false)
        setToolbar(activity, getString(R.string.paypal_payment))
        setHasOptionsMenu(true)

        rootView.webView.settings.javaScriptEnabled = true
        rootView.webView.settings.saveFormData = false

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(rootView.webView, true)

        rootView.webView.settings.setAppCacheEnabled(true)
        rootView.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                rootView.loadingWebViewLayout.isVisible = true
                rootView.webView.isVisible = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                rootView.loadingWebViewLayout.isVisible = false
                rootView.webView.isVisible = true
            }
        }

        val url = "https://${getString(R.string.FRONTEND_HOST)}/orders/${safeArgs.orderIdentifier}/view"

        paypalWebViewModel.connection
            .nonNull()
            .observe(viewLifecycleOwner, Observer { connected ->
                if (connected) {
                    rootView.noInternetCard.isVisible = false
                    rootView.webView.loadUrl(url)
                } else {
                    rootView.noInternetCard.isVisible = true
                    rootView.webView.isVisible = false
                    rootView.loadingWebViewLayout.isVisible = false
                }
            })

        return rootView
    }

    override fun handleBackPress() {
        findNavController(rootView).popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.paypal_payment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.payLater -> {
                activity?.onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
