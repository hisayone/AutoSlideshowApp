package jp.techacademy.hisashi.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.android.synthetic.main.activity_main.* //アクティビティ用
import java.util.*  //Timer用
import android.os.Handler //Handler用

class MainActivity : AppCompatActivity() {

    //表示中の画像識別用
    private var m_id: Long =0//表示中の画像のインデックス番号
    private var m_idStartIndex:Long = 0//画像データの最初のインデックス番号
    private var m_idLastIndex:Long = 0//画像データの最後のインデックス番号

    //画像データアクセス許可用識別値
    private val PERMISSIONS_REQUEST_CODE = 100

    //再生、停止用変数
    private var mTimer: Timer? = null
    private var mTimerSec = 0.0// タイマー用の時間のための変数
    private var mHandler = Handler()
    private  var bln_working = false//再生中変数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo("最初")
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo("最初")
        }

        //進むボタン
        next_button.setOnClickListener{
            getContentsInfo("進む")
        }

        //戻るボタン
        back_button.setOnClickListener{
            getContentsInfo("戻る")
        }

        //再生/停止ボタン
        start_stop_button.setOnClickListener {
            if (bln_working){
                //停止ボタン
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                    bln_working = false
                    start_stop_button.text = "再生"
                    next_button.isEnabled = true
                    back_button.isEnabled = true
                }
            }else{
                //開始ボタン
                if (mTimer == null){
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mTimerSec += 2
                            mHandler.post {
                                Log.d("hisashi",String.format("%.1f", mTimerSec))
                                getContentsInfo("進む")
                            }
                        }
                    }, 2000, 2000) // 最初に始動させるまで 2000ミリ秒、ループの間隔を 2000ミリ秒 に設定
                    bln_working = true
                    start_stop_button.text = "停止"
                    next_button.isEnabled = false
                    back_button.isEnabled = false
                }
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo("最初")
                }
        }
    }



    private fun getContentsInfo(text :String) {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する

            when (text) {
                "最初" ->{
                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    m_id = cursor.getLong(fieldIndex)
                    m_idStartIndex = m_id
                    m_idLastIndex = cursor.count + m_idStartIndex
                    val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, m_id)
                    imageView.setImageURI(imageUri)
                }
                "進む" ->{
                    m_id +=1
                    if (m_id >= cursor.count.toLong() + m_idStartIndex){
                        m_id =cursor.getLong(0)
                    }
                    val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, m_id)
                    Log.d("hisashi","m_id:$m_id")
                    imageView.setImageURI(imageUri)
                }
                "戻る" ->{
                    m_id -=1
                    if (m_id < m_idStartIndex){
                        m_id = m_idLastIndex
                    }
                    val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, m_id)
                    Log.d("hisashi","m_id:$m_id")
                    imageView.setImageURI(imageUri)
                }
            }
        }
        cursor.close()
    }

}
