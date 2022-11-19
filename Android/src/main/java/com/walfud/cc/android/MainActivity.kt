package com.walfud.cc.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.walfud.cc.android.common.setupUtils
import com.walfud.cc.clientshare.setupConfigDir

class MainActivity : ComponentActivity() {

    val neededPermissions = listOf(
        Permission.CAMERA,
//        Permission.MANAGE_EXTERNAL_STORAGE,
        Permission.READ_EXTERNAL_STORAGE,
        Permission.WRITE_EXTERNAL_STORAGE,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup
        setupConfigDir(filesDir.absolutePath)
        setupUtils(applicationContext)
        XXPermissions.setCheckMode(false)

        XXPermissions.with(this)
            .apply {
                neededPermissions.forEach {
                    permission(it)
                }
            }
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    if (all) {
                        setNormalContent()
                    } else {
                        setLackPermissionContent()
                    }
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    setLackPermissionContent()
                }
            })
    }

    override fun onResume() {
        super.onResume()

        if (XXPermissions.isGranted(this, neededPermissions)) {
            setNormalContent()
        }
    }

    fun setNormalContent() {
        setContent {
            App()
        }
    }

    fun setLackPermissionContent() {
        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "上传下载文件需要系统存储权限, 扫码互传需要相机权限!!!",
                    style = MaterialTheme.typography.titleLarge,
                )

                Button(
                    onClick = {
                        XXPermissions.startPermissionActivity(this@MainActivity, Permission.CAMERA, Permission.MANAGE_EXTERNAL_STORAGE)
                    }
                ) {
                    Text("点我打开设置界面")
                }
            }
        }
    }
}