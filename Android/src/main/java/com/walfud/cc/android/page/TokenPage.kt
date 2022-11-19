@file:OptIn(ExperimentalMaterial3Api::class)

package com.walfud.cc.android.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.walfud.cc.android.R
import com.walfud.cc.android.common.*
import com.walfud.cc.clientshare.*
import com.walfud.extention.isUuid
import kotlinx.coroutines.launch


const val ROUTE_TOKEN_PAGE = "token_page"

@Composable
fun TokenPage(vm: TokenViewModel) {
    LaunchedEffect(true) {
        vm.load()
    }

    if (!vm.token.isUuid()) {
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(vm.snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val qrImage = stringToBitmap(vm.token)

            Image(
                qrImage.asImageBitmap(),
                "qrcode",
                modifier = Modifier.size(200.dp),
            )

            Text(vm.token)

            Button(
                onClick = vm::onCopy,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(
                    painterResource(R.drawable.content_copy),
                    "clipboard",
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("copy to clipboard")
            }
        }
    }
}

class TokenViewModel(
    navController: NavController,
) : BaseViewModel(navController) {
    lateinit var snackbarHostState: SnackbarHostState
    var token: String by mutableStateOf("")

    fun load() {
        viewModelScope.launch {
            val ccConfig = initializeConfigIfNeeded()
            this@TokenViewModel.token = ccConfig.token
        }
    }

    fun onCopy() {
        copyToClipboard(this.token)
        viewModelScope.launch {
            snackbarHostState.showSnackbar("copied!", withDismissAction = true, duration = SnackbarDuration.Short)
        }
    }
}

@Preview
@Composable
fun TokenPreview() {

}