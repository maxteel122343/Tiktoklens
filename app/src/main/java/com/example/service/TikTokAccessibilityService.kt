package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.ScrollSearchStateBus
import java.util.ArrayDeque
import java.util.Locale

open class TikTokAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "TikTokDetector"
        val TIKTOK_PACKAGES = setOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.ss.android.ugc.aweme"
        )
        private const val THROTTLE_MS = 35L

        // Known TikTok resource IDs for creator usernames across different versions
        private val TARGET_VIEW_IDS = listOf(
            "author_name",
            "user_name",
            "tv_author",
            "tv_user_name",
            "title_view",
            "name_view",
            "tv_title",
            "author_text",
            "feed_author",
            "profile_user_name",
            "video_author"
        )

        // Reserved non-username words common in TikTok
        private val BLACKLIST_TERMS = hashSetOf(
            "tiktok", "musically", "fyp", "foryou", "foryoupage", "para você", "paravoce",
            "seguindo", "following", "amigos", "friends", "compartilhar", "share",
            "comentários", "comentarios", "comments", "curtir", "curtidas", "like", "likes",
            "som original", "original sound", "original audio", "som", "audio",
            "pesquisar", "search", "adicionar", "add", "início", "inicio", "home",
            "perfil", "profile", "notificações", "notificacoes", "inbox", "caixa de entrada",
            "ao vivo", "live", "login", "cadastre-se", "explore", "descubra",
            "seguir", "follow", "salvar", "save", "favorito", "favoritos", "bookmark"
        )
    }

    private var lastEventTime = 0L
    private var lastDetectedUser: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return
        if (!TIKTOK_PACKAGES.contains(pkgName)) {
            ScrollSearchStateBus.setTikTokForeground(false)
            return
        }

        ScrollSearchStateBus.setTikTokForeground(true)

        val now = SystemClock.uptimeMillis()
        if (now - lastEventTime < THROTTLE_MS) {
            return
        }
        lastEventTime = now

        val rootNode = rootInActiveWindow ?: return
        try {
            val username = extractUsernameFast(rootNode)
            if (!username.isNullOrBlank() && username != lastDetectedUser) {
                lastDetectedUser = username
                Log.d(TAG, "Detected TikTok creator: @$username")
                ScrollSearchStateBus.updateDetectedUsername(username)
            }
        } finally {
            rootNode.recycle()
        }
    }

    /**
     * Ultra-fast BFS scanner limited to 70 nodes to ensure <2ms execution
     * and zero UI lag during TikTok video scrolling.
     */
    private fun extractUsernameFast(root: AccessibilityNodeInfo): String? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        var visited = 0
        val maxNodes = 70

        while (!queue.isEmpty() && visited < maxNodes) {
            val current = queue.poll() ?: break
            visited++

            // Strategy 1: Check known TikTok View ID
            val viewId = current.viewIdResourceName
            if (viewId != null) {
                for (targetId in TARGET_VIEW_IDS) {
                    if (viewId.contains(targetId, ignoreCase = true)) {
                        val text = current.text?.toString()?.trim()
                        val valid = sanitizeUsername(text)
                        if (valid != null) {
                            return valid
                        }
                    }
                }
            }

            // Strategy 2: Check Content Description pattern (e.g. "Perfil de charlidamelio" or "@khaby.lame")
            val contentDesc = current.contentDescription?.toString()
            if (!contentDesc.isNullOrBlank()) {
                val extracted = extractFromContentDescription(contentDesc)
                if (extracted != null) {
                    return extracted
                }
            }

            // Strategy 3: Check plain text if it strictly starts with @ or matches TikTok username pattern
            val text = current.text?.toString()
            if (!text.isNullOrBlank()) {
                if (text.startsWith("@")) {
                    val candidate = sanitizeUsername(text)
                    if (candidate != null) {
                        return candidate
                    }
                }
            }

            // Enqueue child nodes
            val childCount = current.childCount
            for (i in 0 until childCount) {
                current.getChild(i)?.let { child ->
                    queue.add(child)
                }
            }
        }

        return null
    }

    private fun extractFromContentDescription(desc: String): String? {
        val lower = desc.lowercase(Locale.ROOT)
        val prefixes = listOf(
            "perfil de ",
            "profile of ",
            "vídeo de ",
            "video of ",
            "video by ",
            "ver perfil de ",
            "view profile of "
        )
        for (prefix in prefixes) {
            if (lower.startsWith(prefix)) {
                val candidate = desc.substring(prefix.length).trim().removePrefix("@")
                val clean = candidate.split(" ").firstOrNull() ?: candidate
                return sanitizeUsername(clean)
            }
        }
        if (desc.startsWith("@")) {
            return sanitizeUsername(desc)
        }
        return null
    }

    private fun sanitizeUsername(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim().removePrefix("@")

        // TikTok usernames: 2-30 chars, alphanumeric with dots and underscores
        if (trimmed.length in 2..30 && trimmed.matches(Regex("^[a-zA-Z0-9._]+$"))) {
            val lower = trimmed.lowercase(Locale.ROOT)
            if (!BLACKLIST_TERMS.contains(lower) && !lower.all { it.isDigit() }) {
                return trimmed
            }
        }
        return null
    }

    override fun onInterrupt() {
        Log.w(TAG, "TikTokAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        ScrollSearchStateBus.setTikTokForeground(false)
    }
}
