package com.example

import android.net.Uri
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.model.EditorTool
import com.example.model.VideoSegment
import com.example.viewmodel.TMProViewModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EditorFeaturesTest {

    private lateinit var viewModel: TMProViewModel

    // Mock/Fake Dao for testing
    private val fakeDao = object : com.example.data.ProjectDao {
        private val list = mutableListOf<ProjectEntity>()
        override fun getAllProjects() = flowOf(list.toList())
        override suspend fun getProjectById(id: Long) = list.find { it.id == id }
        override suspend fun insertProject(project: ProjectEntity): Long {
            list.add(project)
            return project.id.takeIf { it > 0 } ?: (list.size.toLong())
        }
        override suspend fun updateProject(project: ProjectEntity) {
            val idx = list.indexOfFirst { it.id == project.id }
            if (idx >= 0) list[idx] = project
        }
        override suspend fun deleteProject(project: ProjectEntity) {
            list.removeAll { it.id == project.id }
        }
        override suspend fun deleteProjectById(id: Long) {
            list.removeAll { it.id == id }
        }
    }

    @Before
    fun setup() {
        val repo = ProjectRepository(fakeDao)
        viewModel = TMProViewModel(repo)
    }

    @Test
    fun testOpenNewProjectAndDefaultSegment() {
        viewModel.openNewProjectFromUri(Uri.parse("content://media/external/video/1"), "Test Edit")
        val state = viewModel.editState.value

        assertTrue(viewModel.isEditorOpen.value)
        assertEquals("Test Edit", state.projectTitle)
        assertEquals(1, state.segments.size)
        assertEquals("Clip 1", state.segments.first().name)
    }

    @Test
    fun testTrimmingVideoClip() {
        viewModel.openNewProjectFromUri(Uri.parse("content://media/external/video/1"), "Trim Test")
        viewModel.updateDuration(10000L)

        // Set Trim Start
        viewModel.setTrimStart(2000L)
        assertEquals(2000L, viewModel.editState.value.trimStartMs)

        // Set Trim End
        viewModel.setTrimEnd(7000L)
        assertEquals(7000L, viewModel.editState.value.trimEndMs)
        assertEquals(5000L, viewModel.editState.value.trimmedDurationMs)

        // Reset Trim
        viewModel.resetTrim()
        assertEquals(0L, viewModel.editState.value.trimStartMs)
        assertEquals(10000L, viewModel.editState.value.trimEndMs)
    }

    @Test
    fun testSplittingClipIntoSegments() {
        viewModel.openNewProjectFromUri(Uri.parse("content://media/external/video/1"), "Split Test")
        viewModel.updateDuration(12000L)

        // Position playhead at 4000ms
        viewModel.seekTo(4000L)

        // Perform Split
        viewModel.splitAtPlayhead()

        val state = viewModel.editState.value
        assertEquals(2, state.segments.size)

        val firstClip = state.segments[0]
        val secondClip = state.segments[1]

        assertEquals(0L, firstClip.startMs)
        assertEquals(4000L, firstClip.endMs)

        assertEquals(4000L, secondClip.startMs)
        assertEquals(12000L, secondClip.endMs)
    }

    @Test
    fun testTextOverlayAdditionAndVisibility() {
        viewModel.openNewProjectFromUri(Uri.parse("content://media/external/video/1"), "Text Test")
        viewModel.updateDuration(10000L)

        // Add Text Overlay
        viewModel.addTextOverlay("Hello World", "#06B6D4", "Bottom")

        val state = viewModel.editState.value
        val overlay = state.textOverlays.find { it.text == "Hello World" }
        assertNotNull(overlay)
        assertEquals("#06B6D4", overlay?.colorHex)
        assertEquals("Bottom", overlay?.position)

        // Test visibility
        assertTrue(overlay!!.isVisibleAt(0L, 10000L))
    }

    @Test
    fun testAutoCaptionGenerationBanglaAndEnglish() {
        val banglaCaptions = com.example.util.AutoCaptionGenerator.generateCaptions(
            durationMs = 12000L,
            language = com.example.model.CaptionLanguage.BANGLA
        )
        assertTrue(banglaCaptions.isNotEmpty())
        assertTrue(banglaCaptions.first().words.isNotEmpty())
        assertTrue(banglaCaptions.first().text.contains("ভিডিও") || banglaCaptions.first().text.contains("আজকে"))

        val englishCaptions = com.example.util.AutoCaptionGenerator.generateCaptions(
            durationMs = 12000L,
            language = com.example.model.CaptionLanguage.ENGLISH
        )
        assertTrue(englishCaptions.isNotEmpty())
        assertTrue(englishCaptions.first().words.isNotEmpty())
    }

    @Test
    fun testCaptionTemplatesAndConfig() {
        viewModel.openNewProjectFromUri(Uri.parse("content://media/external/video/1"), "Captions Test")
        val template = com.example.model.CaptionTemplatesCatalog.templates.first { it.id == "hormozi" }

        viewModel.applyCaptionTemplate(template)
        val config = viewModel.editState.value.captionConfig

        assertEquals("hormozi", config.templateId)
        assertEquals(androidx.compose.ui.graphics.Color.White, config.textColor)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFE600), config.activeWordColor)
    }
}
