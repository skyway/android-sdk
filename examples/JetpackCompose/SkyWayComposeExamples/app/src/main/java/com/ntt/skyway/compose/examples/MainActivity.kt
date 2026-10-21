package com.ntt.skyway.compose.examples

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.pip.BasicPictureInPicture
import androidx.core.pip.PictureInPictureDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ntt.skyway.compose.examples.pip.PipStateHolder
import com.ntt.skyway.compose.examples.ui.theme.ComposeAutoSubscribeAppTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity(),
    PictureInPictureDelegate.OnPictureInPictureEventListener {
    private lateinit var mainViewModel: MainViewModel

    // PiP サンプル（SampleType.PIP）専用。他のサンプルでは isCallActive が true にならないため影響しない
    private lateinit var pipImpl: BasicPictureInPicture

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        pipImpl = BasicPictureInPicture(this)
        pipImpl.addOnPictureInPictureEventListener(
            ContextCompat.getMainExecutor(this),
            this,
        )
        lifecycleScope.launch {
            // 通話中（PiP サンプルでルーム入室中）のみ PiP を有効化する
            PipStateHolder.isCallActive.collect { active ->
                pipImpl.setEnabled(active)
            }
        }

        enableEdgeToEdge()
        setContent {
            ComposeAutoSubscribeAppTheme {
                // PiP ウィンドウ表示中は映像のみにするため TopAppBar を描画しない
                val isPipMode by PipStateHolder.isPipMode.collectAsState()
                Scaffold(
                    topBar = {
                        if (!isPipMode) {
                            TopAppBar(
                                title = {
                                    Text(mainViewModel.sampleType.displayName)
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)) {
                            MainNavigation(
                                mainViewModel = mainViewModel
                            )
                        }
                    }
                )
            }
        }
        checkPermission()
    }

    override fun onPictureInPictureEvent(
        event: PictureInPictureDelegate.Event,
        config: Configuration?,
    ) {
        when (event) {
            // PiP ウィンドウへ遷移 → PipScreen 側でミニマル UI に切り替える
            PictureInPictureDelegate.Event.ENTERED -> PipStateHolder.isPipMode.value = true
            // 通常画面に復帰 → PipScreen 側で通常 UI に戻す
            PictureInPictureDelegate.Event.EXITED -> PipStateHolder.isPipMode.value = false
            else -> Unit
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // PiP が残ったまま Activity が破棄される場合に備えて状態をリセット
        PipStateHolder.isPipMode.value = false
        PipStateHolder.isCallActive.value = false
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                ),
                0
            )
        }
    }
}
