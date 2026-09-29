package com.example.util

import com.example.model.AutoCaptionPhrase
import com.example.model.CaptionLanguage
import com.example.model.CaptionWord

object AutoCaptionGenerator {

    private val banglaSentences = listOf(
        "আজকে আমরা দেখব কিভাবে ভিডিও এডিট করতে হয়",
        "টিএম প্রো দিয়ে প্রফেশনাল ভিডিও বানান খুব সহজে",
        "ভিডিওতে সুন্দর কালার এবং ট্রানজিশন ইফেক্ট দিন",
        "দারুণ চমৎকার লুক চলে এসেছে আমাদের ভিডিওতে",
        "অটো ক্যাপশন দিয়ে ভিডিও আরও আকর্ষণীয় করুন",
        "লাইক এবং সাবস্ক্রাইব করতে একদম ভুলবেন না",
        "পরবর্তী দারুণ ভিডিওর জন্য আমাদের সাথেই থাকুন"
    )

    private val englishSentences = listOf(
        "Welcome back to another video editing tutorial",
        "Today we are creating viral captions with TM PRO",
        "Watch how smooth this word by word animation looks",
        "You can customize colors, fonts and stroke easily",
        "Level up your editing workflow like a real pro",
        "Make your Shorts and Reels pop with auto captions",
        "Don't forget to hit like and subscribe for more"
    )

    private val bilingualSentences = listOf(
        "Welcome সবাইকে আজকের TM PRO ভিডিও এডিটিং-এ",
        "এখন আমরা Auto Captions এড করব মাত্র এক ক্লিকে",
        "Viral Reels এবং Shorts বানিয়ে ফেলুন খুব সহজে",
        "স্মুথ ট্রানজিশন এবং সাউন্ড ইফেক্ট দেখতে দারুণ লাগে",
        "Next level content তৈরি করতে TM PRO ব্যবহার করুন",
        "Stay tuned and subscribe for more editing tips"
    )

    fun generateCaptions(
        durationMs: Long,
        language: CaptionLanguage,
        customPromptText: String? = null
    ): List<AutoCaptionPhrase> {
        val totalMs = durationMs.coerceAtLeast(3000L)
        val sentences = if (!customPromptText.isNullOrBlank()) {
            customPromptText.split("\n", ".", "।").map { it.trim() }.filter { it.isNotBlank() }
        } else {
            when (language) {
                CaptionLanguage.BANGLA -> banglaSentences
                CaptionLanguage.ENGLISH -> englishSentences
                CaptionLanguage.BILINGUAL -> bilingualSentences
            }
        }

        val result = mutableListOf<AutoCaptionPhrase>()
        val phraseDurationMs = 2800L
        val phraseGapMs = 400L
        var currentStart = 300L

        var sentenceIndex = 0
        while (currentStart + 1000L < totalMs) {
            val sentence = sentences[sentenceIndex % sentences.size]
            val wordsList = sentence.split(" ").filter { it.isNotBlank() }
            if (wordsList.isEmpty()) {
                sentenceIndex++
                continue
            }

            val phraseEnd = (currentStart + phraseDurationMs).coerceAtMost(totalMs - 200L)
            val durationForWords = phraseEnd - currentStart
            val msPerWord = durationForWords / wordsList.size

            val captionWords = wordsList.mapIndexed { idx, word ->
                val wStart = currentStart + (idx * msPerWord)
                val wEnd = if (idx == wordsList.lastIndex) phraseEnd else wStart + msPerWord
                CaptionWord(
                    word = word,
                    startMs = wStart,
                    endMs = wEnd
                )
            }

            result.add(
                AutoCaptionPhrase(
                    text = sentence,
                    words = captionWords,
                    startMs = currentStart,
                    endMs = phraseEnd
                )
            )

            currentStart = phraseEnd + phraseGapMs
            sentenceIndex++
        }

        return result
    }
}
