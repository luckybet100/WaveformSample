package dev.luckybet100.waveform

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dev.luckybet100.waveform.ui.MainScene
import dev.luckybet100.waveform.utils.PermissionManager
import dev.luckybet100.waveform.vm.MainViewModelImpl
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private val permissionManager by lazy { PermissionManager(WeakReference(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainViewModel: MainViewModelImpl by viewModels()
        val scene = MainScene.create(MainScene.Contract.create(window, permissionManager))
        scene.bind(this, mainViewModel)
        lifecycle.addObserver(mainViewModel)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults))
            return
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
