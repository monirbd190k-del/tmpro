package com.example.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.GalleryMediaItem
import com.example.model.MediaType
import com.example.util.MediaFileHelper
import com.example.util.TimeFormatter
import com.example.viewmodel.TMProViewModel
import kotlinx.coroutines.launch

class GalleryWebBridge(
    private val onStart: (Int) -> Unit,
    private val onFile: (name: String, mime: String, size: Long, dataUrl: String) -> Unit
) {
    @JavascriptInterface
    fun onStartImport(count: Int) {
        onStart(count)
    }

    @JavascriptInterface
    fun onFileSelected(name: String, mime: String, size: Long, dataUrl: String) {
        onFile(name, mime, size, dataUrl)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GalleryImportDialog(
    viewModel: TMProViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val items by viewModel.galleryItems.collectAsState()
    var selectedMediaId by remember { mutableStateOf(items.firstOrNull()?.id ?: "") }
    var filterType by remember { mutableIntStateOf(0) } // 0: All, 1: Videos, 2: Images
    var isImporting by remember { mutableStateOf(false) }
    var importStatusText by remember { mutableStateOf("") }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var fileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // Native Multiple Document Picker for images and videos
    val nativeFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isImporting = true
            importStatusText = "Processing ${uris.size} selected file${if (uris.size > 1) "s" else ""}..."
            fileCallback?.onReceiveValue(uris.toTypedArray())
            fileCallback = null

            coroutineScope.launch {
                val processed = uris.map { uri ->
                    MediaFileHelper.processContentUri(context, uri)
                }
                viewModel.addGalleryItems(processed)
                if (processed.isNotEmpty()) {
                    selectedMediaId = processed.first().id
                }
                isImporting = false
            }
        } else {
            fileCallback?.onReceiveValue(null)
            fileCallback = null
            isImporting = false
        }
    }

    val filteredItems = remember(items, filterType) {
        when (filterType) {
            1 -> items.filter { it.type == MediaType.VIDEO }
            2 -> items.filter { it.type == MediaType.IMAGE }
            else -> items
        }
    }

    val videoCount = remember(items) { items.count { it.type == MediaType.VIDEO } }
    val imageCount = remember(items) { items.count { it.type == MediaType.IMAGE } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D11))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gallery & Media",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF06B6D4).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${items.size} ITEMS",
                                    color = Color(0xFF06B6D4),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Import multiple videos & photos from device",
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E26))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Import Button Bar (With WebView hosting <input type="file" accept="image/*,video/*" multiple>)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // HTML File Picker rendered via WebView for authentic user gesture support
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .testTag("import_from_gallery_button")
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.allowFileAccess = true
                                    settings.domStorageEnabled = true
                                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)

                                    addJavascriptInterface(
                                        GalleryWebBridge(
                                            onStart = { count ->
                                                isImporting = true
                                                importStatusText = "Reading $count file${if (count > 1) "s" else ""}..."
                                            },
                                            onFile = { name, mime, size, dataUrl ->
                                                coroutineScope.launch {
                                                    val item = MediaFileHelper.saveBase64Media(ctx, name, mime, dataUrl)
                                                    if (item != null) {
                                                        viewModel.addGalleryItem(item)
                                                        selectedMediaId = item.id
                                                    }
                                                    isImporting = false
                                                }
                                            }
                                        ),
                                        "AndroidBridge"
                                    )

                                    webChromeClient = object : WebChromeClient() {
                                        override fun onShowFileChooser(
                                            webView: WebView?,
                                            filePathCallback: ValueCallback<Array<Uri>>?,
                                            fileChooserParams: FileChooserParams?
                                        ): Boolean {
                                            fileCallback = filePathCallback
                                            nativeFilePicker.launch(arrayOf("image/*", "video/*"))
                                            return true
                                        }
                                    }

                                    val htmlContent = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                            <style>
                                                * { margin: 0; padding: 0; box-sizing: border-box; font-family: system-ui, -apple-system, sans-serif; }
                                                html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; display: flex; }
                                                .btn-label {
                                                    width: 100%;
                                                    height: 100%;
                                                    background: linear-gradient(135deg, #06B6D4 0%, #3B82F6 100%);
                                                    color: #000000;
                                                    display: flex;
                                                    align-items: center;
                                                    justify-content: center;
                                                    gap: 8px;
                                                    font-size: 13.5px;
                                                    font-weight: 700;
                                                    cursor: pointer;
                                                    user-select: none;
                                                    border-radius: 12px;
                                                    text-decoration: none;
                                                    transition: transform 0.1s ease;
                                                }
                                                .btn-label:active { transform: scale(0.97); }
                                                .btn-label svg { width: 18px; height: 18px; fill: #000000; }
                                                input[type="file"] { display: none; }
                                            </style>
                                        </head>
                                        <body>
                                            <label for="galleryFileInput" class="btn-label" id="btnClick">
                                                <svg viewBox="0 0 24 24"><path d="M19 7v2.99s-1.99.01-2 0V7h-3s.01-1.99 0-2h3V2h2v3h3v2h-3zm-3 4V8h-3V5H5c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-8h-3zM5 19l3-4 2 3 3-4 4 5H5z"/></svg>
                                                <span>Import from Gallery</span>
                                            </label>
                                            <input type="file" id="galleryFileInput" accept="image/*,video/*" multiple onchange="handleSelection(this.files)">
                                            <script>
                                                function handleSelection(files) {
                                                    if (!files || files.length === 0) return;
                                                    if (window.AndroidBridge && window.AndroidBridge.onStartImport) {
                                                        window.AndroidBridge.onStartImport(files.length);
                                                    }
                                                    for (var i = 0; i < files.length; i++) {
                                                        (function(file) {
                                                            var reader = new FileReader();
                                                            reader.onload = function(e) {
                                                                if (window.AndroidBridge && window.AndroidBridge.onFileSelected) {
                                                                    window.AndroidBridge.onFileSelected(file.name, file.type, file.size, e.target.result);
                                                                }
                                                            };
                                                            reader.readAsDataURL(file);
                                                        })(files[i]);
                                                    }
                                                }
                                            </script>
                                        </body>
                                        </html>
                                    """.trimIndent()

                                    loadDataWithBaseURL("https://tmpro.app", htmlContent, "text/html", "UTF-8", null)
                                    webViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Direct Native Document Picker Fallback Button
                    OutlinedButton(
                        onClick = {
                            nativeFilePicker.launch(arrayOf("image/*", "video/*"))
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E3E4C)),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("native_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Pick Files",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Files", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Tabs (All, Videos, Images)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF191922), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "All (${items.size})" to 0,
                        "Videos ($videoCount)" to 1,
                        "Images ($imageCount)" to 2
                    )

                    tabs.forEach { (label, idx) ->
                        val isSel = filterType == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF2C2C3A) else Color.Transparent)
                                .clickable { filterType = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color(0xFFAAAAAA),
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Loading Indicator
                AnimatedVisibility(visible = isImporting) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF14141C), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF06B6D4),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = importStatusText.ifBlank { "Importing media files..." },
                                color = Color(0xFF06B6D4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Media Grid
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF14141A), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF22222C), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No media in this category",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Import from Gallery' to select videos or photos",
                                color = Color(0xFF9E9E9E),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("gallery_media_grid"),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val isSelected = item.id == selectedMediaId
                            GalleryGridCard(
                                item = item,
                                isSelected = isSelected,
                                onClick = { selectedMediaId = item.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Bar: Open Selected
                val selectedItem = items.find { it.id == selectedMediaId }
                if (selectedItem != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF191922), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF2B2B38), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedItem.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (selectedItem.type == MediaType.VIDEO) {
                                    "${selectedItem.formattedSize} • ${TimeFormatter.formatMs(selectedItem.durationMs)}"
                                } else {
                                    "${selectedItem.formattedSize} • Photo"
                                },
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                viewModel.openNewProjectFromUri(
                                    uri = Uri.parse(selectedItem.uri),
                                    title = selectedItem.name,
                                    durationMs = selectedItem.durationMs
                                )
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.testTag("open_in_editor_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Edit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Open in Editor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryGridCard(
    item: GalleryMediaItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1C1C24))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF282834),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag("gallery_item_${item.id}")
    ) {
        // Thumbnail or Type Icon
        if (item.uri.startsWith("content://") || item.uri.startsWith("file://")) {
            AsyncImage(
                model = item.uri,
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            // Gradient banner for sample clips
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            if (item.type == MediaType.VIDEO) listOf(Color(0xFF4338CA), Color(0xFF06B6D4))
                            else listOf(Color(0xFFDB2777), Color(0xFFF59E0B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.type == MediaType.VIDEO) Icons.Default.Videocam else Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Top-left Type badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .background(
                    if (item.type == MediaType.VIDEO) Color(0xFF06B6D4) else Color(0xFFEC4899),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (item.type == MediaType.VIDEO) "VIDEO" else "IMAGE",
                color = Color.Black,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Selected Checkmark
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(Color(0xFF06B6D4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Bottom duration & info bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (item.type == MediaType.VIDEO && item.durationMs > 0) {
                    Text(
                        text = TimeFormatter.formatMs(item.durationMs),
                        color = Color(0xFFFFE600),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
