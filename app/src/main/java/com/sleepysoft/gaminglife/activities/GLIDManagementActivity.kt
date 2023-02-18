package com.sleepysoft.gaminglife.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.king.zxing.CameraScan
import com.king.zxing.CaptureActivity
import com.king.zxing.util.CodeUtils
import com.sleepysoft.gaminglife.*
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import glcore.GlEncryption
import glcore.GlRoot
import pub.devrel.easypermissions.EasyPermissions

class GLIDManagementActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_FROM_QR = 1
        const val REQUEST_CODE_FROM_IMG = 2
        const val REQUEST_CODE_FROM_TEXT = 3
        const val REQUEST_CODE_FROM_CREATE = 4

        const val REQUEST_PERMISSION_CAMERA = 11
        const val REQUEST_PERMISSION_IMAGE = 12

        const val KEY_TITLE = "key_title"
        const val KEY_IS_QR_CODE = "key_code"
        const val KEY_IS_CONTINUOUS = "key_continuous_scan"
    }

    lateinit var mButtonViewGlid: Button
    lateinit var mButtonViewPubKey: Button
    lateinit var mButtonViewPrvKey: Button
    lateinit var mButtonRegOrCreate: Button
    lateinit var mButtonSignOut: Button
    lateinit var mLayoutGroupWithKey: LinearLayout

    lateinit var mButtonImportByQR: Button
    lateinit var mButtonImportByImg: Button
    lateinit var mButtonImportByText: Button
    lateinit var mButtonCreateNew: Button
    lateinit var mLayoutGroupWithoutKey: LinearLayout

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode() == GlControllerContext.RESULT_ACCEPTED) {
            when (val requestCode = result.requestCode()) {
                REQUEST_CODE_FROM_QR -> loadGlId()
                REQUEST_CODE_FROM_TEXT -> loadGlId()
                REQUEST_CODE_FROM_CREATE -> loadGlId()
                else -> Unit
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glidmanagement)

        mButtonViewGlid = findViewById(R.id.id_button_view_glid)
        mButtonViewPubKey = findViewById(R.id.id_button_view_public_key)
        mButtonViewPrvKey = findViewById(R.id.id_button_view_private_key)
        mButtonRegOrCreate = findViewById(R.id.id_button_register_check_in)
        mButtonSignOut = findViewById(R.id.id_button_sign_out)
        mLayoutGroupWithKey = findViewById(R.id.id_layout_with_key)

        mButtonViewGlid.setOnClickListener {
            val intent = Intent(this, QRCodeViewerActivity::class.java)
            intent.putExtra(QRCodeViewerActivity.KEY_QR_CODE, GlRoot.systemConfig.GLID)
            ActivityCompat.startActivity(this, intent, null)
        }

        mButtonViewPubKey.setOnClickListener {
            val intent = Intent(this, QRCodeViewerActivity::class.java)
            intent.putExtra(QRCodeViewerActivity.KEY_QR_CODE, GlRoot.systemConfig.publicKey)
            ActivityCompat.startActivity(this, intent, null)
        }

        mButtonViewPrvKey.setOnClickListener {
            val encoded = "%d|%s|%s".format(
                1, GlRoot.systemConfig.privateKey, GlRoot.systemConfig.publicKey)

            val intent = Intent(this, QRCodeViewerActivity::class.java)
            val keyPairSerialized = GlEncryption.serializeKeyPair(GlRoot.systemConfig.mainKeyPair)
            intent.putExtra(QRCodeViewerActivity.KEY_QR_CODE, keyPairSerialized)
            ActivityCompat.startActivity(this, intent, null)
        }

        mButtonRegOrCreate.setOnClickListener {

        }

        mButtonSignOut.setOnClickListener {
            GlRoot.systemConfig.GLID = ""
            GlRoot.systemConfig.publicKey = ""
            GlRoot.systemConfig.privateKey = ""
            loadGlId()
        }

        mButtonImportByQR = findViewById(R.id.id_button_import_scan)
        mButtonImportByImg = findViewById(R.id.id_button_import_image)
        mButtonImportByText = findViewById(R.id.id_button_import_text)
        mButtonCreateNew = findViewById(R.id.id_button_create)
        mLayoutGroupWithoutKey = findViewById(R.id.id_layout_without_key)

        mButtonImportByQR.setOnClickListener {
            requestScan()
        }

        mButtonImportByImg.setOnClickListener {
            requestPhoto()
        }

        mButtonImportByText.setOnClickListener {
            val activityIntent = Intent(this, CommonTextInputActivity::class.java)
            activityIntent.setRequestCode(REQUEST_CODE_FROM_TEXT)
            requestDataLauncher.launch(activityIntent)
        }

        mButtonCreateNew.setOnClickListener {
            val activityIntent = Intent(this, GeneratePairActivity::class.java)
            activityIntent.setRequestCode(REQUEST_CODE_FROM_CREATE)
            requestDataLauncher.launch(activityIntent)
        }

        loadGlId()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_FROM_QR -> {
                    val result = CameraScan.parseScanResult(data)
                    result?.run { parseQRResult(this) }
                }
                REQUEST_PERMISSION_CAMERA -> startScan()

                REQUEST_CODE_FROM_IMG -> parseImage(data)
                REQUEST_PERMISSION_IMAGE -> selectImage()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadGlId() {
        val privateKey = GlRoot.systemConfig.privateKey
        if (privateKey.isNotEmpty()) {
            mLayoutGroupWithKey.visibility = View.VISIBLE
            mLayoutGroupWithoutKey.visibility = View.GONE
        } else {
            mLayoutGroupWithKey.visibility = View.GONE
            mLayoutGroupWithoutKey.visibility = View.VISIBLE
        }
    }

    private fun requestScan() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            startScan()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.HINT_PERMISSION_CAMERA_QR),
                REQUEST_PERMISSION_CAMERA, *permissions)
        }
    }

    private fun startScan() {
        val intent = Intent(this, CaptureActivity::class.java)
        intent.putExtra(KEY_TITLE, "")
        intent.putExtra(KEY_IS_CONTINUOUS, false)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_FROM_QR, null)
    }

    private fun requestPhoto() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            selectImage()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.HINT_PERMISSION_EXT_STORAGE_QR),
                REQUEST_PERMISSION_IMAGE, *permissions
            )
        }
    }

    private fun selectImage() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(pickIntent, REQUEST_CODE_FROM_IMG)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseImage(data: Intent) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
            Thread {
                val result = CodeUtils.parseCode(bitmap)
                result?.run {
                    runOnUiThread {
                        parseQRResult(result)
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseQRResult(result: String) {

        // val keyPairSerialized = result
        val keyPairSerialized = "AQ==|AISSAWYstJP1xbUao886vzQU2N0lqWunbSKqwh7LFqkM64crS4d7751YZ7Ec1ppzjOLmJVvJVSKf0mv9KiPSpcbufQNgbGgySmcPqCEE39T96Bwjv3VmQqvBjLIMB0x6YMB5195FwpPjWVbnT0apQcOf1i6PMihm9ALl4K6aJeq9argMotOUEyxTM7jm33GJOTWPVJ40koGwtgFT5JBdmeRmVbzQD3sdIl7Y0paINW1Ohvxi+2/ze2Jrpcqqk3kJqVSas6Xf6z2Ijxssn0PL27Gue0L6CYpms5SCoRhQn77Jy0cToDZXMAOaQB9N4rTC2RP837z/ZP3tHPptNHO6TDk=|BPEYYkGqXZWc0Bp1HsV22S8pwlykCyJLnC43XER/zL9xjDFd+GfzT6869kw357wE9XRU0i4YIVlJ08K1Lm6sxVSgjl6qy9uMEHlx8AEpgtwQKfRfL7YnKBXn878lCoHJV4NzO5LoJPElpGqs9tpaXDt6FXQ7D2x/DhiZnoiafzyQm9g6XCv5HlDl1GI0JicoYf6TVDf1+1P2QVLyzld89970/7ES6fTwx74Grc574D4NGW6e/Pumyl1NEC6AUmTWQ87+oKHZvOxhIYnxhRgRzTqdn0I0QlJ1ULPAlz68OWt/NxcPSSpdntzbtzKNEcB4GlWnxZ9NWv8/zn7/7BkJ/Q==|AQAB"

        val keyPair = GlEncryption.deserializeKeyPair(keyPairSerialized)
        if (keyPair.keyPairValid()) {
            GlRoot.systemConfig.publicKey = keyPair.publicKeyString
            GlRoot.systemConfig.privateKey = keyPair.privateKeyString
            loadGlId()
        } else {
            toast(getString(R.string.HINT_LOAD_PRIVATE_KEY_ERROR))
        }
    }
}