@file:OptIn(ExperimentalMaterial3Api::class)

package com.walfud.cc.android.page

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.walfud.cc.android.R
import com.walfud.cc.android.common.*
import com.walfud.cc.clientshare.*
import com.walfud.cc.projectshare.model.THING_TYPE_FILE
import com.walfud.cc.projectshare.model.THING_TYPE_TEXT
import com.walfud.cc.projectshare.model.Thing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


const val ROUTE_CONTENT_PAGE = "content_page"

@Composable
fun ContentPage(vm: ContentViewModel) {
    LaunchedEffect(true) {
        vm.refreshThings()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cross Copy") },
                actions = {
                    IconButton(
                        onClick = vm::onOpenTokenPage,
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "token")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    vm.onClickFloatingAction()
                }
            ) {
                Icon(Icons.Rounded.Add, "/thing/upload")
            }
        },
        snackbarHost = { SnackbarHost(vm.snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SwipeRefresh(
                state = vm.swipeRefreshState,
                onRefresh = {
                    vm.refreshThings()
                },
                modifier = Modifier.fillMaxWidth().weight(1.0f),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    items(vm.things, key = { it.id }) { thingData ->
                        ThingItem(thingData, vm::onClickItem)
                    }
                }
            }
            Banner()
        }

        if (vm.uploadDialog) {
            UploadDialog(vm::onUploadOk, vm::onUploadCancel)
        }
    }
}

@Composable
fun ThingItem(thingData: ThingData, onClick: (ThingData) -> Unit) {
    val iconResource = when (thingData.type) {
        THING_TYPE_TEXT -> R.drawable.text_content
        THING_TYPE_FILE -> R.drawable.folder_open
        else -> throw RuntimeException("`ThingItem`: unsupported thing type(${thingData.type})")
    }

    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable {
                onClick(thingData)
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Icon(
            painterResource(iconResource),
            "item icon",
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            thingData.text,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
        )
    }
}

@Composable
fun Banner() {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp)
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "这里应该有广告",
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Composable
fun UploadDialog(
    onOk: (text: String, file: File?) -> Unit,
    onCancel: () -> Unit,
) {
    var text: String by remember { mutableStateOf(copyFromClipboard() ?: "") }
    var file: File? by remember { mutableStateOf(null) }
    val openSelectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                file = uri.toAbsoluteFile()
                text = file!!.name
            }
        }
    )

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .width(360.dp).height(460.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                .background(MaterialTheme.colorScheme.background),
        ) {
            // title
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Upload",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W900,
                )
            }

            Divider(
                Modifier.fillMaxWidth(),
                thickness = Dp.Hairline,
            )

            // text
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    text,
                    modifier = Modifier.weight(1.0f),
                    maxLines = 5,
                    label = {
                        val hint = if (file == null) {
                            "text data to upload"
                        } else {
                            "filename"
                        }
                        Text(hint)
                    },
                    onValueChange = { newText ->
                        text = newText
                    }
                )

                Spacer(modifier = Modifier.width(10.dp))


                Column(
                    modifier = Modifier.clickable {
                        copyFromClipboard()?.let {
                            text = it
                        }
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painterResource(R.drawable.content_copy),
                        "clipboard",
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        "clipboard",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // choose file
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "or",
                    modifier = Modifier.padding(horizontal = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                )

                Button(
                    onClick = {
                        openSelectLauncher.launch("*/*")
                    },
                    modifier = Modifier.padding(horizontal = 5.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painterResource(R.drawable.folder_open),
                            "/thing/upload",
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            "select file",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (file != null) {
                    Text(file!!.absolutePath)
                }
            }

            Spacer(modifier = Modifier.weight(1.0f))

            // ok & cancel
            Spacer(modifier = Modifier.padding(top = 10.dp))

            Divider(
                Modifier.fillMaxWidth(),
                thickness = Dp.Hairline,
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1.0f),
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                    )
                }
                Box(
                    modifier = Modifier.width(0.5.dp).height(24.dp)
                        .background(Color.Gray),
                )
                TextButton(
                    onClick = {
                        if (text.isEmpty()) {
                            // error
                            return@TextButton
                        }

                        onOk(text, file)
                    },
                    modifier = Modifier.weight(1.0f),
                ) {
                    Text(
                        "Ok",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                    )
                }
            }
        }
    }
}

class ContentViewModel(
    navController: NavController,
) : BaseViewModel(navController) {
    lateinit var snackbarHostState: SnackbarHostState

    val swipeRefreshState = SwipeRefreshState(isRefreshing = false)
    var things: List<ThingData> by mutableStateOf(mutableListOf())
    var uploadDialog: Boolean by mutableStateOf(false)

    fun onOpenTokenPage() {
        navController.navigate(ROUTE_TOKEN_PAGE)
    }

    fun onClickFloatingAction() {
        uploadDialog = true
    }

    fun refreshThings() {
        viewModelScope.launch {
            swipeRefreshState.isRefreshing = true

            initializeConfigIfNeeded()

            things = getThingsFromServer()
                .sortedBy { it.updateTime }
                .reversed()
                .map { ThingData.fromThing(it) }
                .toList()

            swipeRefreshState.isRefreshing = false
        }
    }

    fun upload(text: String, file: File?) {
        val newThingData = ThingData(
            if (file == null) THING_TYPE_TEXT else THING_TYPE_FILE,
            text,
            ThingData.STATUS_UPLOADING,
            0,
        )
        things = things.toMutableList().apply {
            add(0, newThingData)
        }

        viewModelScope.launch {
            initializeConfigIfNeededSync()

            val res = try {
                if (file == null) {
                    uploadTextToServer(text)
                } else {
                    uploadFileToServer(text, file) { bytesSentTotal, contentLength ->
                        val progress = bytesSentTotal * 100 / contentLength
                        updateThings(newThingData.id, newThingData.copy(progress = progress.toInt()))
                    }
                }
            } catch (err: Exception) {
                null
            }
            val status = if (res != null) ThingData.STATUS_DONE else ThingData.STATUS_FAIL
            updateThings(newThingData.id, newThingData.copy(status = status))
        }
    }

    fun downloadFile(filename: String) {
        viewModelScope.launch(Dispatchers.IO) {
            initializeConfigIfNeededSync()

            val cacheFile = getCacheFile(filename)
            downloadFileFromServer(filename, cacheFile) { _, _ -> }
            val publicFile = copyPrivateFileToPublicDownloadDir(cacheFile)
            cacheFile.delete()

            val res = snackbarHostState.showSnackbar("请在 Downloads 目录中查看文件", "Open", withDismissAction = true, duration = SnackbarDuration.Short)
            if (res == SnackbarResult.ActionPerformed) {
                openFile(publicFile)
            }
        }
    }

    fun onClickItem(thingData: ThingData) {
        when (thingData.type) {
            THING_TYPE_TEXT -> {
                copyToClipboard(thingData.text)
                viewModelScope.launch {
                    snackbarHostState.showSnackbar("copied to clipboard!", withDismissAction = true, duration = SnackbarDuration.Short)
                }
            }

            THING_TYPE_FILE -> {
                downloadFile(thingData.text)
                viewModelScope.launch {
                    snackbarHostState.showSnackbar("download started", duration = SnackbarDuration.Short)
                }
            }

            else -> throw RuntimeException("`onClickItem`: unsupported thing type(${thingData.type})")
        }
    }

    /**************** upload dialog ***************/

    fun onUploadOk(text: String, file: File?) {
        upload(text, file)
        uploadDialog = false
    }

    fun onUploadCancel() {
        uploadDialog = false
    }

    /***************** common *********************/
    fun updateThings(id: Int, newThing: ThingData) {
        things = things.map {
            if (it.id == id) {
                newThing
            } else {
                it
            }
        }
            .toList()
    }
}

data class ThingData(
    val type: Int,
    val text: String,

    val status: Int,
    val progress: Int,      // 0~100
) {
    val id: Int = nextId()

    companion object {
        const val STATUS_DONE = 0
        const val STATUS_UPLOADING = 10
        const val STATUS_DOWNLOADING = 20
        const val STATUS_FAIL = -1

        var incId: Int = 0
        fun nextId(): Int = incId++

        fun fromThing(thing: Thing): ThingData {
            return ThingData(
                thing.type,
                thing.content,
                STATUS_DONE,
                0,
            )
        }
    }
}

@Preview
@Composable
fun ContentPreview() {
    ThingItem(
        ThingData(
            0,
            "this is a text",
            0,
            0,
        )
    ) {

    }
}