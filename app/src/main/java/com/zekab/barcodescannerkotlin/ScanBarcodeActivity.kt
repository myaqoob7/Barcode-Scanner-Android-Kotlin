package com.zekab.barcodescannerkotlin

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ScanBarcodeActivity : AppCompatActivity() {

    private var scanPrintList: ArrayList<ScanPrintItem>? = null
    var scanPrintImages = intArrayOf(
        R.drawable.ic_email2, R.drawable.ic_website,
        R.drawable.ic_location, R.drawable.ic_wifi, R.drawable.ic_contect_info,
        R.drawable.ic_password
    )

    var surfaceView: SurfaceView? = null
    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    var currentDateTime = ""

    var animator: ObjectAnimator? = null
    var scannerLayout: LinearLayout? = null
    var scannerBar: View? = null
    private lateinit var animation: Animation
    private var isScanned = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_barcode)

        initViews()
        initListeners()
        initScanningAnim()

    }

    private fun initViews() {
        surfaceView = findViewById(R.id.surfaceView)
        scannerLayout = findViewById(R.id.scannerLayout)
        scannerBar = findViewById(R.id.scannerBar)

        animator = null
    }

    private fun initScanningAnim() {

        animation = AnimationUtils.loadAnimation(this, R.anim.scanning_anim)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                scannerBar?.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        scannerBar?.startAnimation(animation)
    }

    @SuppressLint("MissingPermission")
    private fun initListeners() {

        scanPrintList = ArrayList()
        currentDateTime =
            SimpleDateFormat("dd-MMM-yyyy HH:mm aa", Locale.getDefault()).format(Date())


        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource?.start(surfaceView!!.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource?.stop()
            }
        })

        barcodeDetector?.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    if (isScanned){
                        isScanned = false
                        for (index in 0 until barcodes.size()) {
                            val code = barcodes.valueAt(index)
                            when (barcodes.valueAt(index).valueFormat) {
                                Barcode.CONTACT_INFO -> {
                                    val phones = code.contactInfo.phones
                                    val addresses = code.contactInfo.addresses
                                    val emails = code.contactInfo.emails
                                    val url = code.contactInfo.urls
                                    var myNumber = ""
                                    var myAddress = ""
                                    var myMail = ""
                                    if (phones.isNotEmpty()) {
                                        val phone = phones[0]
                                        myNumber = phone.number
                                    }
                                    if (addresses.isNotEmpty()) {
                                        val adrs = addresses[0]
                                        myAddress = adrs.addressLines[0]
                                    }
                                    if (emails.isNotEmpty()) {
                                        val mail = emails[0]
                                        myMail = mail.address
                                    }
                                    if (code.contactInfo.name.toString().isNotEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printFormat = "Name",
                                                printValue = code.contactInfo.name.formattedName,
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false
                                            )
                                        )
                                    }
                                    if (!code.contactInfo.title.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false,
                                                printValue = code.contactInfo.title,
                                                printFormat = "Title"
                                            )
                                        )
                                    }
                                    if (!code.contactInfo.organization.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false,
                                                printValue = code.contactInfo.organization,
                                                printFormat = "Organization"
                                            )
                                        )
                                    }
                                    if (!code.contactInfo.addresses.toString().isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false,
                                                printValue = myAddress,
                                                printFormat = "Address"
                                            )
                                        )
                                    }
                                    if (!code.contactInfo.phones.toString().isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[4],
                                                printImageVisibility = true,
                                                printValue = myNumber,
                                                printFormat = "Phone"
                                            )
                                        )
                                    }
                                    if (!code.contactInfo.emails.toString().isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = true,
                                                printValue = myMail,
                                                printFormat = "Email"
                                            )
                                        )
                                    }
                                    if (!code.contactInfo.urls.toString().isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[1],
                                                printImageVisibility = true,
                                                printValue = url[0],
                                                printFormat = "URL"
                                            )
                                        )
                                    }

                                }
                                Barcode.EMAIL -> {
                                    if (!code.email.address.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = true,
                                                printValue = code.email.address,
                                                printFormat = "Email"
                                            )
                                        )
                                    }
                                    if (!code.email.subject.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false,
                                                printValue = code.email.subject,
                                                printFormat = "Subject"
                                            )
                                        )
                                    }
                                    if (!code.email.body.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false,
                                                printValue = code.email.body,
                                                printFormat = "Body"
                                            )
                                        )
                                    }

                                }
                                Barcode.ISBN -> {
                                    scanPrintList?.add(
                                        ScanPrintItem(
                                            printImage = scanPrintImages[0],
                                            printImageVisibility = false,
                                            printValue = code.rawValue,
                                            printFormat = "ISBN"
                                        )
                                    )

                                }
                                Barcode.PHONE -> {
                                    if (!code.phone.number.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[4],
                                                printImageVisibility = true,
                                                printValue = code.phone.number,
                                                printFormat = "Phone"
                                            )
                                        )
                                    }

                                }
                                Barcode.PRODUCT -> {
                                    scanPrintList?.add(
                                        ScanPrintItem(
                                            printImage = scanPrintImages[0],
                                            printImageVisibility = false,
                                            printValue = code.rawValue,
                                            printFormat = "Product"
                                        )
                                    )

                                }
                                Barcode.SMS -> {
                                    if (!code.sms.phoneNumber.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[4],
                                                printImageVisibility = true,
                                                printValue = code.sms.phoneNumber,
                                                printFormat = "Phone"
                                            )
                                        )
                                    }
                                    if (!code.sms.message.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false,
                                                printValue = code.sms.message,
                                                printFormat = "Message"
                                            )
                                        )
                                    }

                                }
                                Barcode.TEXT -> {
                                    scanPrintList?.add(
                                        ScanPrintItem(
                                            printImage = scanPrintImages[0],
                                            printImageVisibility = false,
                                            printValue = code.displayValue,
                                            printFormat = "Text"
                                        )
                                    )

                                }
                                Barcode.URL -> {
                                    if (!code.url.title.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[0],
                                                printImageVisibility = false,
                                                printValue = code.url.title,
                                                printFormat = "Title"
                                            )
                                        )
                                    }
                                    if (!code.url.url.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[1],
                                                printImageVisibility = true,
                                                printValue = code.url.url,
                                                printFormat = "URL"
                                            )
                                        )
                                    }

                                }
                                Barcode.WIFI -> {
                                    if (!code.wifi.ssid.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = true,
                                                printValue = code.wifi.ssid,
                                                printFormat = "SSID"
                                            )
                                        )
                                    }
                                    if (!code.wifi.password.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[5],
                                                printImageVisibility = true,
                                                printValue = code.wifi.password,
                                                printFormat = "Password"
                                            )
                                        )
                                    }
                                    if (!code.wifi.encryptionType.toString().isEmpty()) {
                                        var ename = ""
                                        when (code.wifi.encryptionType) {
                                            Barcode.WiFi.OPEN -> ename = "OPEN"
                                            Barcode.WiFi.WPA -> ename = "WPA"
                                            Barcode.WiFi.WEP -> ename = "WEP"
                                        }
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = ename,
                                                printFormat = "EncryptionType"
                                            )
                                        )
                                    }

                                }
                                Barcode.GEO -> {
                                    scanPrintList?.add(
                                        ScanPrintItem(
                                            printImage = scanPrintImages[3],
                                            printImageVisibility = false,
                                            printValue = code.geoPoint.lat.toString(),
                                            printFormat = "Latitude"
                                        )
                                    )
                                    scanPrintList?.add(
                                        ScanPrintItem(
                                            printImage = scanPrintImages[3],
                                            printImageVisibility = false,
                                            printValue = code.geoPoint.lng.toString(),
                                            printFormat = "Longitude"
                                        )
                                    )

                                }
                                Barcode.CALENDAR_EVENT -> {
                                    if (!code.calendarEvent.summary.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.calendarEvent.summary,
                                                printFormat = "Summary"
                                            )
                                        )
                                    }
                                    if (!code.calendarEvent.start.toString().isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = dateChanger(code.calendarEvent.start.rawValue),
                                                printFormat = "Start"
                                            )
                                        )
                                    }
                                    if (!code.calendarEvent.end.toString().isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = dateChanger(code.calendarEvent.end.rawValue),
                                                printFormat = "End"
                                            )
                                        )
                                    }
                                    if (!code.calendarEvent.status.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.calendarEvent.status,
                                                printFormat = "Status"
                                            )
                                        )
                                    }
                                    if (!code.calendarEvent.location.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.calendarEvent.location,
                                                printFormat = "Location"
                                            )
                                        )
                                    }
                                    if (!code.calendarEvent.organizer.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.calendarEvent.organizer,
                                                printFormat = "Organizer"
                                            )
                                        )
                                    }
                                    if (!code.calendarEvent.description.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.calendarEvent.description,
                                                printFormat = "Description"
                                            )
                                        )
                                    }

                                }
                                Barcode.DRIVER_LICENSE -> {
                                    if (!code.driverLicense.firstName.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.firstName,
                                                printFormat = "First Name"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.middleName.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.middleName,
                                                printFormat = "Middle Name"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.lastName.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.lastName,
                                                printFormat = "Last Name"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.gender.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.gender,
                                                printFormat = "Gender"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.licenseNumber.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.licenseNumber,
                                                printFormat = "License Number"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.issuingCountry.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.issuingCountry,
                                                printFormat = "Issuing Country"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.addressCity.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.addressCity,
                                                printFormat = "Address City"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.addressState.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.addressState,
                                                printFormat = "Address State"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.addressStreet.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.addressStreet,
                                                printFormat = "Address Street"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.addressZip.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.addressZip,
                                                printFormat = "Address Zip"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.birthDate.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.birthDate,
                                                printFormat = "Birth Date"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.issueDate.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.issueDate,
                                                printFormat = "Issue Date"
                                            )
                                        )
                                    }
                                    if (!code.driverLicense.expiryDate.isEmpty()) {
                                        scanPrintList?.add(
                                            ScanPrintItem(
                                                printImage = scanPrintImages[3],
                                                printImageVisibility = false,
                                                printValue = code.driverLicense.expiryDate,
                                                printFormat = "Expiry Date"
                                            )
                                        )
                                    }

                                }
                            }
                        }
                        scanPrintList?.add(
                            ScanPrintItem(
                                printImage = scanPrintImages[0],
                                printImageVisibility = false,
                                printValue = barcodes.valueAt(0).displayValue,
                                printFormat = "Value"
                            )
                        )
                        scanPrintList?.add(
                            ScanPrintItem(
                                printImage = scanPrintImages[0],
                                printImageVisibility = false,
                                printValue = decodeFormat(barcodes.valueAt(0).format),
                                printFormat = "Format"
                            )
                        )
                        scanPrintList?.add(
                            ScanPrintItem(
                                printImage = scanPrintImages[0],
                                printImageVisibility = false,
                                printValue = currentDateTime,
                                printFormat = "Last updated"
                            )
                        )

                        val type = barcodes.valueAt(0).valueFormat
                        val jsonCars = Converters.setToBarcode(scanPrintList!!)
                        val intent = Intent(this@ScanBarcodeActivity, ScannedPrintActivity::class.java)
                        intent.putExtra("scanPrintData", jsonCars)
                        intent.putExtra("valueFormat", type)
                        startActivity(intent)
                        finish()
                    }

                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        cameraSource.let {
            it?.release()
        }

    }

    override fun onResume() {
        super.onResume()
    }

    private fun decodeFormat(format: Int): String {
        return when (format) {
            Barcode.CODE_128 -> "CODE_128"
            Barcode.CODE_39 -> "CODE_39"
            Barcode.CODE_93 -> "CODE_93"
            Barcode.CODABAR -> "CODABAR"
            Barcode.DATA_MATRIX -> "DATA_MATRIX"
            Barcode.EAN_13 -> "EAN_13"
            Barcode.EAN_8 -> "EAN_8"
            Barcode.ITF -> "ITF"
            Barcode.QR_CODE -> "QR_CODE"
            Barcode.UPC_A -> "UPC_A"
            Barcode.UPC_E -> "UPC_E"
            Barcode.PDF417 -> "PDF417"
            Barcode.AZTEC -> "AZTEC"
            else -> ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateChanger(mdate: String): String {
        var formatter = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
        var date: Date? = null
        try {
            date = formatter.parse(mdate)
        } catch (e: ParseException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        formatter = SimpleDateFormat("dd-MMM-yyyy HH:mm aa")
        return formatter.format(date)
    }

}