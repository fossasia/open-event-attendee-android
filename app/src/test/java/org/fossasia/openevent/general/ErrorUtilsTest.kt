package org.fossasia.openevent.general

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.ResponseBody
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import android.accounts.Account
import com.google.common.io.Resources
import com.google.common.base.Charsets
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.fossasia.openevent.general.utils.ErrorUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class ErrorUtilsTest {
    var contentType = "application/vnd.api+json"

    private lateinit var url1: URL
    private lateinit var url2: URL
    private lateinit var url3: URL
    private lateinit var url4: URL
    private lateinit var url5: URL

    private lateinit var content1: String
    private lateinit var content2: String
    private lateinit var content3: String
    private lateinit var content4: String
    private lateinit var content5: String

    private lateinit var httpException1: HttpException
    private lateinit var httpException2: HttpException
    private lateinit var httpException3: HttpException
    private lateinit var httpException4: HttpException
    private lateinit var httpException5: HttpException

    private lateinit var responseBody1: ResponseBody
    private lateinit var responseBody2: ResponseBody
    private lateinit var responseBody3: ResponseBody
    private lateinit var responseBody4: ResponseBody
    private lateinit var responseBody5: ResponseBody

    private lateinit var errorResponse1: Response<Account>
    private lateinit var errorResponse2: Response<Account>
    private lateinit var errorResponse3: Response<Account>
    private lateinit var errorResponse4: Response<Account>
    private lateinit var errorResponse5: Response<Account>

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        url1 = Resources.getResource("raw/content1.json")
        url2 = Resources.getResource("raw/content2.json")
        url3 = Resources.getResource("raw/content3.json")
        url4 = Resources.getResource("raw/content4.json")
        url5 = Resources.getResource("raw/content5.json")

        try {
            content1 = Resources.toString(url1, Charsets.UTF_8)
            content2 = Resources.toString(url2, Charsets.UTF_8)
            content3 = Resources.toString(url3, Charsets.UTF_8)
            content4 = Resources.toString(url4, Charsets.UTF_8)
            content5 = Resources.toString(url5, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        responseBody1 = ResponseBody.create(contentType.toMediaTypeOrNull(), content1)
        errorResponse1 = Response.error(422, responseBody1)

        responseBody2 = ResponseBody.create(contentType.toMediaTypeOrNull(), content2)
        errorResponse2 = Response.error(422, responseBody2)

        responseBody3 = ResponseBody.create(contentType.toMediaTypeOrNull(), content3)
        errorResponse3 = Response.error(422, responseBody3)

        responseBody4 = ResponseBody.create(contentType.toMediaTypeOrNull(), content4)
        errorResponse4 = Response.error(400, responseBody4)

        responseBody5 = ResponseBody.create(contentType.toMediaTypeOrNull(), content5)
        errorResponse5 = Response.error(400, responseBody5)

        httpException1 = HttpException(errorResponse1)
        httpException2 = HttpException(errorResponse2)
        httpException3 = HttpException(errorResponse3)
        httpException4 = HttpException(errorResponse4)
        httpException5 = HttpException(errorResponse5)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun shouldReturnNullOnNullAnsEmptyPointedField() {
        assertNull(ErrorUtils.getPointedField(null))
        assertNull(ErrorUtils.getPointedField(""))
    }

    @Test
    fun shouldReturnPointedField() {
        val pointer = "/data/attributes/form/end_field"
        val pointer1 = "/data/attributes/form_field"
        val pointer2 = "/data/attributes/"
        val pointer3 = "/data"

        assertEquals("end_field", ErrorUtils.getPointedField(pointer))
        assertEquals("form_field", ErrorUtils.getPointedField(pointer1))
        assertNull(ErrorUtils.getPointedField(pointer2))
        assertNull(ErrorUtils.getPointedField(pointer3))
    }

    @Test
    fun shouldReturnErrorDetailsWithPointedFieldSuccessfully() {
        val str = ErrorUtils.getErrorDetails(httpException1).toString()
        assertEquals("Missing data for required field - licence", str)
    }

    @Test
    fun shouldReturnErrorDetailsWithNullOrEmptyPointedFieldSuccessfully() {
        val str = ErrorUtils.getErrorDetails(httpException3).toString()
        val str1 = ErrorUtils.getErrorDetails(httpException2).toString()
        assertEquals("Missing data for required field.", str1)
        assertEquals("Missing data for required field.", str)
    }

    @Test
    fun shouldReturnErrorMessageSuccessfully() {
        val str = ErrorUtils.getMessage(httpException1).toString()
        val str1 = ErrorUtils.getMessage(httpException5).toString()
        assertEquals("Missing data for required field - licence", str)
        assertEquals("Access Forbidden: Co-Organizer access required - order_id", str1)
    }

    @Test
    fun shouldReturnErrorMessageTitleAndDetailSuccessfully() {
        val str = ErrorUtils.getErrorTitleAndDetails(httpException4).toString()
        assertEquals("Bad Request: The URL does not exist", str)
    }

    @Test
    fun shouldReturnErrorMessageTitleDetailAndPointerSuccessfully() {
        val str = ErrorUtils.getErrorTitleAndDetails(httpException5).toString()
        assertEquals("Access Forbidden: Co-Organizer access required - order_id", str)
    }
}
