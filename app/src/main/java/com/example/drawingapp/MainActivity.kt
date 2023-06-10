 package com.example.drawingapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

 class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
     private var mimagebuttoncurrentpaint :ImageButton? = null
     var customProgressDialog: Dialog? = null

     val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
         if(result.resultCode == RESULT_OK && result.data!=null){
             val imagebackground: ImageView = findViewById(R.id.ib_background)
             imagebackground.setImageURI(result.data?.data)
         }
     }

     val requestpermission: ActivityResultLauncher<Array<String>> =
         registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
             permissions ->
             permissions.entries.forEach(){
                 val permissionName = it.key
                 val isGranted = it.value
                 if(isGranted){
                     Toast.makeText(
                         this@MainActivity,
                         "Permission granted now you can read the storage files",
                         Toast.LENGTH_LONG
                     ).show()

                     val pickIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                     openGalleryLauncher.launch(pickIntent)
                 }
                 else{
                     if(permissionName== android.Manifest.permission.READ_EXTERNAL_STORAGE){
                         Toast.makeText(
                             this@MainActivity,
                             "Oops Permission Denied",
                             Toast.LENGTH_LONG
                         ).show()
                     }
                 }
             }
         }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeforbrush(20.toFloat())
        val linearLayoutpaintcolour = findViewById<LinearLayout>(R.id.ll_paint_colour)
        mimagebuttoncurrentpaint = linearLayoutpaintcolour[1] as ImageButton
        mimagebuttoncurrentpaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )

        val ib_brush : ImageButton = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener{
            showbrushsizeDialog()
        }
        val ib_undo : ImageButton = findViewById(R.id.ib_undo)
        ib_undo.setOnClickListener{
            drawingView?.onclickundo()
        }

         val ibGallery : ImageButton = findViewById(R.id.ic_gallery)
        ibGallery.setOnClickListener{
            requestStoragePermission()
        }
        val ibSave: ImageButton = findViewById(R.id.ib_save)
        ibSave.setOnClickListener{
            if(isReadStorageAllowed()){
                lifecycleScope.launch{
                    val flDrswingView: FrameLayout = findViewById(R.id.fl_drawinng_vierw_container)
                   val mybitmap: Bitmap =  getbitmapfromview(flDrswingView)
                    saveBitmap(mybitmap)
                }
            }

        }

    }
    private  fun showbrushsizeDialog(){
        var brushdialog = Dialog(this)
        brushdialog.setContentView(R.layout.dialog_brush_size)
        brushdialog.setTitle("Brush Size: ")
        val smallbtn = brushdialog.findViewById<ImageButton>(R.id.ib_small_brush)
        smallbtn.setOnClickListener{
            drawingView?.setSizeforbrush(10.toFloat())
            brushdialog.dismiss()
        }
        val mediumbtn = brushdialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumbtn.setOnClickListener{
            drawingView?.setSizeforbrush(20.toFloat())
            brushdialog.dismiss()
        }
        val largebtn = brushdialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largebtn.setOnClickListener{
            drawingView?.setSizeforbrush(30.toFloat())
            brushdialog.dismiss()
        }


        brushdialog.show()



    }
     fun paintclicked(view:View){
         if(view!=mimagebuttoncurrentpaint){
             val imageButton = view as ImageButton
              val colourtag = imageButton.tag.toString()
             drawingView?.setcolor(colourtag)

             imageButton.setImageDrawable(
                 ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
             )
             mimagebuttoncurrentpaint?.setImageDrawable(
                 ContextCompat.getDrawable(this,R.drawable.pallet_normal)
             )

             mimagebuttoncurrentpaint = view
         }
     }
     private fun isReadStorageAllowed(): Boolean{
         showprogressdialog()
         val result = ContextCompat.checkSelfPermission(this,
         android.Manifest.permission.READ_EXTERNAL_STORAGE)

         return result == PackageManager.PERMISSION_GRANTED
     }
     private fun requestStoragePermission(){
         if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
         ){
             showrationaldialog("Kids Drawing App", "Kids Drawing App "
+ "needs to Access Your External Storage")
             }else{
                 requestpermission.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                 android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
         }
     }
     private fun getbitmapfromview(view: View): Bitmap{
         val returnedBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888 )
         val canvas = Canvas(returnedBitmap)
         val   bgDrawable = view.background
         if(bgDrawable!=null){
             bgDrawable.draw(canvas)
         }else{
             canvas.drawColor(Color.WHITE)
         }
         view.draw(canvas)
         return returnedBitmap
     }
     private suspend fun saveBitmap(mBitmap: Bitmap?): String{
         var result = ""
         withContext(Dispatchers.IO){
             if(mBitmap!=null){
                 try {
                     val bytes = ByteArrayOutputStream()
                     mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

                     val f  = File(externalCacheDir?.absoluteFile.toString() + File.separator + "KidsDrawingApp_" + System.currentTimeMillis()/1000 + ".png")

                     val fo = FileOutputStream(f)

                     fo.write(bytes.toByteArray())
                     fo.close()

                     result = f.absolutePath

                     runOnUiThread{
                         cancelProgressDialog()
                         if(result.isNotEmpty()){
                             Toast.makeText(this@MainActivity,"File Saved Succesfully :$result",Toast.LENGTH_SHORT).show()
                            shareImage(result)
                         }else{
                             Toast.makeText(this@MainActivity,"Something Went Wrong",Toast.LENGTH_SHORT).show()


                         }
                     }


                 }
                 catch (e : java.lang.Exception){
                     result = ""
                     e.printStackTrace()
                 }
             }
         }
        return  result
     }
     private fun showrationaldialog(
         title: String,
         message: String
     ){
         val builder: AlertDialog.Builder = AlertDialog.Builder(this)
         builder.setTitle(title)
             .setMessage(message)
             .setPositiveButton("Cancel"){
                 dialog, _ ->
                 dialog.dismiss()
             }
         builder.create().show()
     }
     private fun showprogressdialog(){
         customProgressDialog = Dialog(this@MainActivity)

         customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

         customProgressDialog?.show()
     }
     private fun cancelProgressDialog(){
         if(customProgressDialog!= null){
             customProgressDialog?.dismiss()
             customProgressDialog = null
         }
     }
     private fun shareImage(result: String){
         MediaScannerConnection.scanFile(this, arrayOf(result),null){
             path,uri->
             val shareintent = Intent()
             shareintent.action = Intent.ACTION_SEND
             shareintent.putExtra(Intent.EXTRA_STREAM,uri)
             shareintent.type = "image/png"
             startActivity(Intent.createChooser(shareintent,"Share"))

         }
     }

}