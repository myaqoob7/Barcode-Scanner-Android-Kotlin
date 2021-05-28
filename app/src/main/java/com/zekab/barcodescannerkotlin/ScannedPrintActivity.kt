package com.zekab.barcodescannerkotlin

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class ScannedPrintActivity: AppCompatActivity() {

    private lateinit var scanPrintList: ArrayList<ScanPrintItem>
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: AdapterScan
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var floatingActionButton: LinearLayout
    private lateinit var floatingText: TextView
    private lateinit var floatingImage: ImageView
    private var valueTypeString = ""
    private var imageValue = R.drawable.ic_text
    private var valueType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_print)

        scanPrintList = ArrayList()
        val scanPrintData = intent.getStringExtra("scanPrintData")
        scanPrintList = Converters.getFromBarcode(scanPrintData!!)

        initViews()
        initListeners()
        buildRecyclerView()

        valueType = intent.getIntExtra("valueFormat", 0)
        when (valueType) {
            Barcode.CONTACT_INFO -> {
                imageValue = R.drawable.ic_contect_info
                valueTypeString = "CONTACT_INFO"
                floatingAction("Add", R.drawable.ic_add)
            }
            Barcode.EMAIL -> {
                imageValue = R.drawable.ic_email
                floatingAction("Send", R.drawable.ic_email2)
                valueTypeString = "EMAIL"
            }
            Barcode.ISBN -> {
                imageValue = R.drawable.ic_text
                floatingActionButton.visibility = View.GONE
                valueTypeString = "ISBN"
            }
            Barcode.PHONE -> {
                imageValue = R.drawable.ic_telephone
                floatingAction("Call", R.drawable.ic_phone_call)
                valueTypeString = "PHONE"
            }
            Barcode.PRODUCT -> {
                imageValue = R.drawable.ic_text
                floatingActionButton.visibility = View.GONE
                valueTypeString = "PRODUCT"
            }
            Barcode.SMS -> {
                imageValue = R.drawable.ic_message
                floatingAction("Send", R.drawable.ic_message_send)
                valueTypeString = "SMS"
            }
            Barcode.TEXT -> {
                imageValue = R.drawable.ic_text
                floatingActionButton.visibility = View.GONE
                valueTypeString = "TEXT"
            }
            Barcode.URL -> {
                imageValue = R.drawable.ic_website
                floatingAction("Open", R.drawable.ic_website)
                valueTypeString = "URL"
            }
            Barcode.WIFI -> {
                imageValue = R.drawable.ic_wifi
                floatingAction("Connect", R.drawable.ic_wifi)
                valueTypeString = "WIFI"
            }
            Barcode.GEO -> {
                imageValue = R.drawable.ic_location
                floatingAction("Locate", R.drawable.ic_location)
                valueTypeString = "GEO"
            }
            Barcode.CALENDAR_EVENT -> {
                imageValue = R.drawable.ic_event
                floatingAction("Add", R.drawable.ic_event)
                valueTypeString = "CALENDAR_EVENT"
            }
            Barcode.DRIVER_LICENSE -> {
                imageValue = R.drawable.ic_message
                floatingActionButton.visibility = View.GONE
                valueTypeString = "DRIVER_LICENSE"
            }
            else -> {
                imageValue = R.drawable.ic_text
                floatingActionButton.visibility = View.GONE
                valueTypeString = "TEXT"
            }
        }

    }

    private fun initViews(){
        floatingActionButton = findViewById(R.id.floatingActionButton)
        floatingText = findViewById(R.id.floatingText)
        floatingImage = findViewById(R.id.floatingImage)
    }

    private fun initListeners(){
        floatingActionButton.setOnClickListener {
            when (valueType) {
                Barcode.CONTACT_INFO -> addContactInfo(scanPrintList)
                Barcode.EMAIL -> addEmail(scanPrintList)
                Barcode.PHONE -> addPhone(scanPrintList)
                Barcode.SMS -> addSMS(scanPrintList)
                Barcode.URL -> addURL(scanPrintList)
                Barcode.WIFI -> addWiFi(scanPrintList)
                Barcode.GEO -> addGeo(scanPrintList)
                Barcode.CALENDAR_EVENT -> addEvent(scanPrintList)
                else -> {
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.scanedprint_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.shareprint -> {
                shareData(getDataFromList(scanPrintList))

                //                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                true
            }
            R.id.copy_print -> {
                copyData(getDataFromList(scanPrintList))
                //                Toast.makeText(this, "Copy", Toast.LENGTH_SHORT).show();
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView)
        mLayoutManager = LinearLayoutManager(this)
        mAdapter = AdapterScan()
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter
        mAdapter.submitList(scanPrintList)
        mAdapter.setOnItemClickListener(object : OnScanClickListener {
            override fun onItemClick(position: Int) {
                val value: String = scanPrintList[position].printValue
                showAdDialog(value)
            }

        })
    }

    private fun showAdDialog(value: String) {
        val mDialog = BottomSheetDialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setCanceledOnTouchOutside(true)
        mDialog.setContentView(R.layout.dialog_scan)

        val tvValue:TextView = mDialog.findViewById(R.id.tvValue)!!
        tvValue.text = value

        val btnCopy:TextView = mDialog.findViewById(R.id.btnCopy)!!
        btnCopy.setOnClickListener {
            copyData(value)
            mDialog.dismiss()
        }
        val btnSearch:TextView = mDialog.findViewById(R.id.btnSearch)!!
        btnSearch.setOnClickListener {
            searchData(value)
            mDialog.dismiss()
        }
        val btnShare:TextView = mDialog.findViewById(R.id.btnShare)!!
        btnShare.setOnClickListener {
            shareData(value)
            mDialog.dismiss()
        }
        val btnTranslate:TextView = mDialog.findViewById(R.id.btnTranslate)!!
        btnTranslate.setOnClickListener {
            translateDate(value)
            mDialog.dismiss()
        }
        mDialog.show()
    }

    private fun shareData(mData: String) {
        try {
            val link = Intent(Intent.ACTION_SEND)
            link.type = "text/plain"
            link.putExtra(Intent.EXTRA_SUBJECT, "QR & Barcode")
            link.putExtra(Intent.EXTRA_TEXT, mData)
            startActivity(Intent.createChooser(link, "Choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    private fun copyData(mData: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("simple text", mData)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun searchData(mData: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, mData)
        startActivity(intent)
    }

    private fun translateDate(mData: String) {
        val url =
            "https://translate.google.com/#view=home&op=translate&sl=auto&tl=en&text=$mData"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun getDataFromList(list: ArrayList<ScanPrintItem>): String {
        var dataList: String? = ""
        for (i in 0 until list.size - 3) {
            dataList += list[i].printValue
            dataList += "\n"
        }
        return dataList!!
    }

    private fun floatingAction(txt: String, n: Int) {
        floatingText.text = txt
        floatingImage.setImageResource(n)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun addContactInfo(list: ArrayList<ScanPrintItem>) {
        var name = ""
        var phone = ""
        var company = ""
        var mail= ""
        var title = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "Name" -> name = list[i].printValue
                "Title" -> title = list[i].printValue
                "Organization" -> company = list[i].printValue
                "Phone" -> phone = list[i].printValue
                "Email" -> mail = list[i].printValue
            }
        }
        val intent = Intent(ContactsContract.Intents.Insert.ACTION)
        intent.type = ContactsContract.RawContacts.CONTENT_TYPE
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name)
            .putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            .putExtra(ContactsContract.Intents.Insert.COMPANY, company)
            .putExtra(ContactsContract.Intents.Insert.EMAIL, mail)
            .putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title)
        startActivity(intent)
    }

    private fun addEmail(list: ArrayList<ScanPrintItem>) {
        var mail: String? = ""
        var subject: String? = ""
        var body: String? = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "Email" -> mail = list[i].printValue
                "Subject" -> subject = list[i].printValue
                "Body" -> body = list[i].printValue
            }
        }

        val send = Intent(Intent.ACTION_SENDTO)
        val uriText = "mailto:" + Uri.encode(mail) +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body)
        val uri = Uri.parse(uriText)
        send.data = uri
        startActivity(Intent.createChooser(send, "Send mail..."))
    }

    private fun addPhone(list: ArrayList<ScanPrintItem>) {
        var phone = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "Phone" -> phone = "tel:" + list[i].printValue
            }
        }
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(phone)
        startActivity(intent)
    }

    private fun addSMS(list: ArrayList<ScanPrintItem>) {
        var phone = ""
        var message = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "Phone" -> phone = list[i].printValue
                "Message" -> message = list[i].printValue
            }
        }
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse("sms:$phone")
        sendIntent.putExtra("sms_body", message)
        startActivity(sendIntent)
    }

    private fun addURL(list: ArrayList<ScanPrintItem>) {
        var url = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "URL" -> url = list[i].printValue
            }
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this, "No application can handle this request."
                        + " Please install a webbrowser", Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    private fun addWiFi(list: ArrayList<ScanPrintItem>) {
        var mSSID = ""
        var mPassword = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "SSID" -> mSSID = list[i].printValue
                "Password" -> mPassword = list[i].printValue
            }
        }
        val connectionManager = ConnectionManager(this)
        connectionManager.enableWifi()
        connectionManager.requestWIFIConnection(mSSID, mPassword)
    }

    private fun addGeo(list: ArrayList<ScanPrintItem>) {
        var mLatiLongi: String? = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "Value" -> mLatiLongi = list[i].printValue
            }
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mLatiLongi))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    private fun addEvent(list: ArrayList<ScanPrintItem>) {
        var mSummary = ""
        var mStart = ""
        var mEnd= ""
        var mStatus= ""
        var mLocation = ""
        var mOrganizer = ""
        var mDescription = ""
        for (i in list.indices) {
            when (list[i].printFormat) {
                "Summary" -> mSummary = list[i].printValue
                "Start" -> mStart = list[i].printValue
                "End" -> mEnd = list[i].printValue
                "Status" -> mStatus = list[i].printValue
                "Location" -> mLocation = list[i].printValue
                "Organizer" -> mOrganizer = list[i].printValue
                "Description" -> mDescription = list[i].printValue
            }
        }
        val intent = Intent(Intent.ACTION_EDIT)
        intent.setType("vnd.android.cursor.item/event")
            .putExtra(CalendarContract.Events.TITLE, mStatus)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, mStart)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, mEnd)
            .putExtra(CalendarContract.AUTHORITY, mOrganizer)
            .putExtra(CalendarContract.Events.DESCRIPTION, mDescription)
        startActivity(intent)
    }
}