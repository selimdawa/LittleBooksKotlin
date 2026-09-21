package com.flatcode.littlebooksadmin.ui.ads

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.loadWithGlide
import com.flatcode.littlebooksadmin.databinding.ActivitySliderShowBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.text.MessageFormat

class SliderShowActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySliderShowBinding
    private var activity: Activity? = null
    private val context: Context = also { activity = it }
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null
    private var IMAGE_NUMBER = 0
    private var item = 0

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            uploadImage(DATA.EMPTY + IMAGE_NUMBER)
        } else {
            val exception = result.error
            exception?.let {
                Toast.makeText(context, getString(R.string.error_message, it.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCrop() {
        cropImage.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    aspectRatioX = 16,
                    aspectRatioY = 9,
                    fixAspectRatio = true,
                    minCropResultWidth = DATA.MIX_SLIDER_X,
                    minCropResultHeight = DATA.MIX_SLIDER_Y,
                    cropShape = CropImageView.CropShape.RECTANGLE
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySliderShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.nameSpace.setText(R.string.slider_show)
        binding.toolbar.back.setOnClickListener { onBackPressed() }

        dialog = ProgressDialog(context)
        dialog!!.setTitle(R.string.please_wait)
        dialog!!.setCanceledOnTouchOutside(false)

        binding.addOne.setOnClickListener {
            IMAGE_NUMBER = 1
            startCrop()
        }
        binding.addTwo.setOnClickListener {
            IMAGE_NUMBER = 2
            startCrop()
        }
        binding.addThree.setOnClickListener {
            IMAGE_NUMBER = 3
            startCrop()
        }
        binding.addFour.setOnClickListener {
            IMAGE_NUMBER = 4
            startCrop()
        }
        binding.addFive.setOnClickListener {
            IMAGE_NUMBER = 5
            startCrop()
        }
        binding.addSix.setOnClickListener {
            IMAGE_NUMBER = 6
            startCrop()
        }
        binding.addSeven.setOnClickListener {
            IMAGE_NUMBER = 7
            startCrop()
        }
        binding.addEight.setOnClickListener {
            IMAGE_NUMBER = 8
            startCrop()
        }
        binding.addNine.setOnClickListener {
            IMAGE_NUMBER = 9
            startCrop()
        }
        binding.addTeen.setOnClickListener {
            IMAGE_NUMBER = 10
            startCrop()
        }
        binding.addEleven.setOnClickListener {
            IMAGE_NUMBER = 11
            startCrop()
        }
        binding.addTwelfth.setOnClickListener {
            IMAGE_NUMBER = 12
            startCrop()
        }
        binding.addThirteen.setOnClickListener {
            IMAGE_NUMBER = 13
            startCrop()
        }
        binding.addFourteenth.setOnClickListener {
            IMAGE_NUMBER = 14
            startCrop()
        }
        binding.addFifteenth.setOnClickListener {
            IMAGE_NUMBER = 15
            startCrop()
        }
        binding.addSixteen.setOnClickListener {
            IMAGE_NUMBER = 16
            startCrop()
        }
        binding.addSeventeen.setOnClickListener {
            IMAGE_NUMBER = 17
            startCrop()
        }
        binding.addEighteen.setOnClickListener {
            IMAGE_NUMBER = 18
            startCrop()
        }
        binding.addNineteen.setOnClickListener {
            IMAGE_NUMBER = 19
            startCrop()
        }
        binding.addTwenty.setOnClickListener {
            IMAGE_NUMBER = 20
            startCrop()
        }
    }

    private val nrSliderShow: Unit
        get() {
            val reference = FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    item = dataSnapshot.childrenCount.toInt()
                    binding.toolbar.nameSpace.text =
                        MessageFormat.format("Slider Show ( {0} )", item)
                    if (item >= 0) {
                        binding.linearOne.visibility = View.VISIBLE
                    } else {
                        binding.linearOne.visibility = View.GONE
                    }
                    if (item >= 1) {
                        binding.linearTwo.visibility = View.VISIBLE
                    } else {
                        binding.linearTwo.visibility = View.GONE
                    }
                    if (item >= 2) {
                        binding.linearThree.visibility = View.VISIBLE
                    } else {
                        binding.linearThree.visibility = View.GONE
                    }
                    if (item >= 3) {
                        binding.linearFour.visibility = View.VISIBLE
                    } else {
                        binding.linearFour.visibility = View.GONE
                    }
                    if (item >= 4) {
                        binding.linearFive.visibility = View.VISIBLE
                    } else {
                        binding.linearFive.visibility = View.GONE
                    }
                    if (item >= 5) {
                        binding.linearSix.visibility = View.VISIBLE
                    } else {
                        binding.linearSix.visibility = View.GONE
                    }
                    if (item >= 6) {
                        binding.linearSeven.visibility = View.VISIBLE
                    } else {
                        binding.linearSeven.visibility = View.GONE
                    }
                    if (item >= 7) {
                        binding.linearEight.visibility = View.VISIBLE
                    } else {
                        binding.linearEight.visibility = View.GONE
                    }
                    if (item >= 8) {
                        binding.linearNine.visibility = View.VISIBLE
                    } else {
                        binding.linearNine.visibility = View.GONE
                    }
                    if (item >= 9) {
                        binding.linearTeen.visibility = View.VISIBLE
                    } else {
                        binding.linearTeen.visibility = View.GONE
                    }
                    if (item >= 10) {
                        binding.linearEleven.visibility = View.VISIBLE
                    } else {
                        binding.linearEleven.visibility = View.GONE
                    }
                    if (item >= 11) {
                        binding.linearTwelfth.visibility = View.VISIBLE
                    } else {
                        binding.linearTwelfth.visibility = View.GONE
                    }
                    if (item >= 12) {
                        binding.linearThirteen.visibility = View.VISIBLE
                    } else {
                        binding.linearThirteen.visibility = View.GONE
                    }
                    if (item >= 13) {
                        binding.linearFourteenth.visibility = View.VISIBLE
                    } else {
                        binding.linearFourteenth.visibility = View.GONE
                    }
                    if (item >= 14) {
                        binding.linearFifteenth.visibility = View.VISIBLE
                    } else {
                        binding.linearFifteenth.visibility = View.GONE
                    }
                    if (item >= 15) {
                        binding.linearSixteen.visibility = View.VISIBLE
                    } else {
                        binding.linearSixteen.visibility = View.GONE
                    }
                    if (item >= 16) {
                        binding.linearEighteen.visibility = View.VISIBLE
                    } else {
                        binding.linearEighteen.visibility = View.GONE
                    }
                    if (item >= 17) {
                        binding.linearEighteen.visibility = View.VISIBLE
                    } else {
                        binding.linearEighteen.visibility = View.GONE
                    }
                    if (item >= 18) {
                        binding.linearNineteen.visibility = View.VISIBLE
                    } else {
                        binding.linearNineteen.visibility = View.GONE
                    }
                    if (item >= 19) {
                        binding.linearTwenty.visibility = View.VISIBLE
                    } else {
                        binding.linearTwenty.visibility = View.GONE
                    }
                    binding.bar.visibility = View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun SliderShow() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val one = DATA.EMPTY + dataSnapshot.child("1").value
                val two = DATA.EMPTY + dataSnapshot.child("2").value
                val three = DATA.EMPTY + dataSnapshot.child("3").value
                val four = DATA.EMPTY + dataSnapshot.child("4").value
                val five = DATA.EMPTY + dataSnapshot.child("5").value
                val six = DATA.EMPTY + dataSnapshot.child("6").value
                val seven = DATA.EMPTY + dataSnapshot.child("7").value
                val eight = DATA.EMPTY + dataSnapshot.child("8").value
                val nine = DATA.EMPTY + dataSnapshot.child("9").value
                val teen = DATA.EMPTY + dataSnapshot.child("10").value
                val eleven = DATA.EMPTY + dataSnapshot.child("11").value
                val twelfth = DATA.EMPTY + dataSnapshot.child("12").value
                val thirteen = DATA.EMPTY + dataSnapshot.child("13").value
                val fourteenth = DATA.EMPTY + dataSnapshot.child("14").value
                val fifteenth = DATA.EMPTY + dataSnapshot.child("15").value
                val sixteen = DATA.EMPTY + dataSnapshot.child("16").value
                val seventeen = DATA.EMPTY + dataSnapshot.child("17").value
                val eighteen = DATA.EMPTY + dataSnapshot.child("18").value
                val nineteen = DATA.EMPTY + dataSnapshot.child("19").value
                val twenty = DATA.EMPTY + dataSnapshot.child("20").value

                binding.imageOne.loadWithGlide(false, one)
                binding.imageTwo.loadWithGlide(false, two)
                binding.imageThree.loadWithGlide(false, three)
                binding.imageFour.loadWithGlide(false, four)
                binding.imageFive.loadWithGlide(false, five)
                binding.imageSix.loadWithGlide(false, six)
                binding.imageSeven.loadWithGlide(false, seven)
                binding.imageEight.loadWithGlide(false, eight)
                binding.imageNine.loadWithGlide(false, nine)
                binding.imageTeen.loadWithGlide(false, teen)
                binding.imageEleven.loadWithGlide(false, eleven)
                binding.imageTwelfth.loadWithGlide(false, twelfth)
                binding.imageThirteen.loadWithGlide(false, thirteen)
                binding.imageFourteenth.loadWithGlide(false, fourteenth)
                binding.imageFifteenth.loadWithGlide(false, fifteenth)
                binding.imageSixteen.loadWithGlide(false, sixteen)
                binding.imageSeventeen.loadWithGlide(false, seventeen)
                binding.imageEighteen.loadWithGlide(false, eighteen)
                binding.imageNineteen.loadWithGlide(false, nineteen)
                binding.imageTwenty.loadWithGlide(false, twenty)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun uploadImage(name: String) {
        dialog!!.setMessage(getString(R.string.posting_photo))
        dialog!!.show()
        MediaManager.get().upload(imageUri)
            .option("folder", "Images/SliderShow/")
            .option("public_id", name)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val uploadedImageUrl = resultData?.get("secure_url") as? String
                    updateImage(uploadedImageUrl!!, DATA.EMPTY + name)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    dialog!!.dismiss()
                    Toast.makeText(context, getString(R.string.error_message, error?.description), Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun updateImage(imageUrl: String, name: String) {
        dialog!!.setMessage(getString(R.string.posting_photo))
        dialog!!.show()
        val hashMap = HashMap<String, Any>()
        if (imageUri != null) {
            hashMap[DATA.EMPTY + name] = DATA.EMPTY + imageUrl
        }
        val reference = FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
        reference.updateChildren(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            Toast.makeText(context, R.string.photo_posted, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(context, getString(R.string.error_message, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        nrSliderShow
        SliderShow()
        super.onResume()
    }

    override fun onRestart() {
        nrSliderShow
        SliderShow()
        super.onRestart()
    }
}

