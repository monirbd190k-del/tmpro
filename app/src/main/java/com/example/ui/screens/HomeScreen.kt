package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTool
import com.example.model.SampleVideoCatalog
import com.example.viewmodel.MainTab
import com.example.viewmodel.TMProViewModel

@Composable
fun HomeScreen(
    viewModel: TMProViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recentProjects by viewModel.allProjects.collectAsState()
    var showSamplePicker by remember { mutableStateOf(false) }

    // Modern Zero-Permission Photo Picker for videos
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openNewProjectFromUri(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0F))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TM PRO",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF06B6D4).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "STUDIO",
                            color = Color(0xFF06B6D4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Video Editor",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .clickable { viewModel.selectTab(MainTab.SETTINGS) }
                    .testTag("home_settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Create New Project Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF22222B),
                            Color(0xFF17171C)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(1.dp, Color(0xFF2E2E3A), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Create New Project",
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "Import video from gallery or test samples",
                            color = Color(0xFF9E9E9E),
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Video",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Main "Import from Gallery" Button
                    Button(
                        onClick = { viewModel.openGallery() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("import_from_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Import from Gallery",
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Import from Gallery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Native Video Picker Button
                    OutlinedButton(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A4A58)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_project_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Project",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Quick Sample Button (Ideal for Emulator & Instant Test)
                    OutlinedButton(
                        onClick = { showSamplePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A4A58)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("try_sample_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Try Sample",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Samples",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Quick Editor Tools Row
        Text(
            text = "Quick Tools",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickToolPill(
                icon = Icons.Default.AddPhotoAlternate,
                title = "Import Gallery",
                color = Color(0xFF06B6D4),
                onClick = { viewModel.openGallery() }
            )

            QuickToolPill(
                icon = Icons.Default.ClosedCaption,
                title = "Auto Captions",
                color = Color(0xFFFFE600),
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            QuickToolPill(
                icon = Icons.Default.ContentCut,
                title = "Trim & Cut",
                color = Color(0xFF6366F1),
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            QuickToolPill(
                icon = Icons.Default.Speed,
                title = "Speed FX",
                color = Color(0xFF06B6D4),
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            QuickToolPill(
                icon = Icons.Default.ColorLens,
                title = "Color Filters",
                color = Color(0xFFEC4899),
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            QuickToolPill(
                icon = Icons.Default.TextFields,
                title = "Add Text",
                color = Color(0xFFF59E0B),
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Recent Projects Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Projects",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            if (recentProjects.isNotEmpty()) {
                Text(
                    text = "See all (${recentProjects.size})",
                    color = Color(0xFF06B6D4),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.selectTab(MainTab.PROJECTS) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recent Projects List or Empty State
        if (recentProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = Color(0xFF161619),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(1.dp, Color(0xFF222228), RoundedCornerShape(18.dp))
                    .clickable { showSamplePicker = true },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Projects",
                        tint = Color(0xFF8A8A8A),
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "No projects yet",
                        color = Color(0xFF8A8A8A),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Tap New Project or try a sample clip",
                        color = Color(0xFF555560),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recentProjects.take(4), key = { it.id }) { project ->
                    ProjectCardItem(
                        project = project,
                        onClick = { viewModel.openProjectFromEntity(project) },
                        onDelete = { viewModel.deleteProject(project) }
                    )
                }
            }
        }

        // Bottom Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                icon = Icons.Default.Home,
                title = "Home",
                selected = true,
                onClick = { viewModel.selectTab(MainTab.HOME) }
            )

            BottomItem(
                icon = Icons.Default.Folder,
                title = "Projects",
                selected = false,
                onClick = { viewModel.selectTab(MainTab.PROJECTS) }
            )

            BottomItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                selected = false,
                onClick = { viewModel.selectTab(MainTab.SETTINGS) }
            )
        }
    }

    // Sample Video Selector Dialog
    if (showSamplePicker) {
        AlertDialog(
            onDismissRequest = { showSamplePicker = false },
            title = {
                Text(
                    text = "Select a Sample Video",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Instant video editing without uploading from device:",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp
                    )

                    SampleVideoCatalog.samples.forEach { sample ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF242430))
                                .clickable {
                                    showSamplePicker = false
                                    viewModel.openSampleProject(sample, context)
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.linearGradient(sample.gradientColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sample.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${sample.subtitle} • ${sample.durationText}",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSamplePicker = false }) {
                    Text("Close", color = Color(0xFFAAAAAA))
                }
            },
            containerColor = Color(0xFF191920)
        )
    }
}

@Composable
fun QuickToolPill(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF19191E))
            .border(1.dp, Color(0xFF282832), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BottomItem(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("nav_tab_${title.lowercase()}")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (selected) Color.White else Color(0xFF777777),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            color = if (selected) Color.White else Color(0xFF777777),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
