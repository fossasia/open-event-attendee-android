package org.fossasia.openevent.general.order

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_order_details.view.orderDetailCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_order_details.view.orderDetailsRecycler
import kotlinx.android.synthetic.main.fragment_order_details.view.backgroundImage
import kotlinx.android.synthetic.main.item_card_order_details.view.orderDetailCardView
import kotlinx.android.synthetic.main.item_enlarged_qr.view.enlargedQrImage
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.order.invoice.DownloadInvoiceService
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import java.io.File
import java.io.FileOutputStream

class OrderDetailsFragment : Fragment() {

    private lateinit var rootView: View
    private val orderDetailsViewModel by viewModel<OrderDetailsViewModel>()
    private val ordersRecyclerAdapter: OrderDetailsRecyclerAdapter = OrderDetailsRecyclerAdapter()
    private val safeArgs: OrderDetailsFragmentArgs by navArgs()

    private var writePermissionGranted = false
    private val WRITE_REQUEST_CODE = 1
    private val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ordersRecyclerAdapter.setOrderIdentifier(safeArgs.orderIdentifier)

        orderDetailsViewModel.event
            .nonNull()
            .observe(this, Observer {
                ordersRecyclerAdapter.setEvent(it)
                Picasso.get()
                    .load(it.originalImageUrl)
                    .error(R.drawable.header)
                    .placeholder(R.drawable.header)
                    .into(rootView.backgroundImage)
            })

        orderDetailsViewModel.attendees
            .nonNull()
            .observe(this, Observer {
                if (it.isEmpty()) {
                    Toast.makeText(context, getString(R.string.error_fetching_attendees), Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed()
                }
                ordersRecyclerAdapter.addAll(it)
                Timber.d("Fetched attendees of size %s", ordersRecyclerAdapter.itemCount)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_order_details, container, false)
        setToolbar(activity)
        setHasOptionsMenu(true)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.orderDetailsRecycler.layoutManager = linearLayoutManager
        rootView.orderDetailsRecycler.adapter = ordersRecyclerAdapter
        rootView.orderDetailsRecycler.isNestedScrollingEnabled = false

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rootView.orderDetailsRecycler)
        rootView.orderDetailsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(linearLayoutManager)
                    centerView?.let {
                        val itemPosition = linearLayoutManager.getPosition(it)
                        orderDetailsViewModel.currentTicketPosition = itemPosition
                    }
                }
            }
        })

        val eventDetailsListener = object : OrderDetailsRecyclerAdapter.EventDetailsListener {
            override fun onClick(eventID: Long) {
                findNavController(rootView).navigate(OrderDetailsFragmentDirections
                    .actionOrderDetailsToEventDetails(eventID))
            }
        }

        ordersRecyclerAdapter.setSeeEventListener(eventDetailsListener)

        val qrImageListener = object : OrderDetailsRecyclerAdapter.QrImageClickListener {
            override fun onClick(qrImage: Bitmap) {
                showEnlargedQrImage(qrImage)
            }
        }

        ordersRecyclerAdapter.setQrImageClickListener(qrImageListener)

        val progressBar = progressDialog(context, getString(R.string.loading_message))
        orderDetailsViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressBar.show(it)
            })

        orderDetailsViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.orderDetailCoordinatorLayout.longSnackbar(it)
            })

        orderDetailsViewModel.loadEvent(safeArgs.eventId)
        orderDetailsViewModel.loadAttendeeDetails(safeArgs.orderId)

        writePermissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.share_ticket -> {
                shareCurrentTicket()
                true
            }

            R.id.download_invoice -> {
                if (writePermissionGranted) {
                    downloadInvoice()
                } else {
                    requestPermissions(permission, WRITE_REQUEST_CODE)
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writePermissionGranted = true
                rootView.snackbar(getString(R.string.permission_granted_message, getString(R.string.external_storage)))
                downloadInvoice()
            } else {
                rootView.snackbar(getString(R.string.permission_denied_message, getString(R.string.external_storage)))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ordersRecyclerAdapter.setQrImageClickListener(null)
        ordersRecyclerAdapter.setSeeEventListener(null)
    }

    private fun downloadInvoice() {
        val downloadPath = "${BuildConfig.DEFAULT_BASE_URL}orders/invoices/${safeArgs.orderIdentifier}"
        val destinationPath = "${Environment.getExternalStorageDirectory().absolutePath}/DownloadManager/"
        val fileName = "Invoice - ${orderDetailsViewModel.event.value?.name} - ${safeArgs.orderIdentifier}"
        activity?.startService(DownloadInvoiceService.getDownloadService(requireContext(), downloadPath,
            destinationPath, fileName, orderDetailsViewModel.getToken()))
    }

    private fun showEnlargedQrImage(bitmap: Bitmap) {
        val brightAttributes = activity?.window?.attributes
        orderDetailsViewModel.brightness = brightAttributes?.screenBrightness
        brightAttributes?.screenBrightness = 1f
        activity?.window?.attributes = brightAttributes

        val dialogLayout = layoutInflater.inflate(R.layout.item_enlarged_qr, null)
        dialogLayout.enlargedQrImage.setImageBitmap(bitmap)
        AlertDialog.Builder(requireContext())
            .setOnDismissListener {
                val attributes = activity?.window?.attributes
                attributes?.screenBrightness = orderDetailsViewModel.brightness
                activity?.window?.attributes = attributes
            }.setView(dialogLayout)
            .create().show()
    }

    private fun shareCurrentTicket() {
        val currentTicketViewHolder =
            rootView.orderDetailsRecycler.findViewHolderForAdapterPosition(orderDetailsViewModel.currentTicketPosition)
        if (currentTicketViewHolder != null && currentTicketViewHolder is OrderDetailsViewHolder) {
            val bitmap = getBitmapFromView(currentTicketViewHolder.itemView.rootView.orderDetailCardView)
            val bitmapUri = getBitmapUri(bitmap)
            if (bitmapUri == null) {
                rootView.snackbar(getString(R.string.fail_sharing_ticket))
                return
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
            startActivity(intent)
        } else {
            rootView.snackbar(getString(R.string.fail_sharing_ticket))
        }
    }

    private fun getBitmapUri(bitmap: Bitmap): Uri? {
        val myContext = context ?: return null
        val file = File(myContext.cacheDir, "shared_image.png")
        return FileOutputStream(file)
            .use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream)
                FileProvider.getUriForFile(myContext, BuildConfig.APPLICATION_ID + ".provider", file)
            }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
