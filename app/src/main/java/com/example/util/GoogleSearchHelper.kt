package com.example.util

import android.util.Log
import com.example.model.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object GoogleSearchHelper {
    private const val TAG = "GoogleSearchHelper"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    fun buildSearchUrl(
        engine: com.example.model.SearchEngine,
        query: String,
        qualityParam: String = "",
        isSafeSearchDisabled: Boolean = com.example.data.ScrollSearchStateBus.isSafeSearchDisabled.value,
        searchType: com.example.model.SearchType = com.example.data.ScrollSearchStateBus.selectedSearchType.value,
        isHighQualityFilter: Boolean = com.example.data.ScrollSearchStateBus.isHighQualityFilterEnabled.value
    ): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val effectiveQuality = if (qualityParam.isNotBlank()) {
            qualityParam
        } else if (isHighQualityFilter) {
            "&tbs=isz:l"
        } else {
            ""
        }
        return when (engine) {
            com.example.model.SearchEngine.GOOGLE -> {
                val safeParam = if (isSafeSearchDisabled) "&safe=off" else "&safe=active"
                when (searchType) {
                    com.example.model.SearchType.IMAGES ->
                        "https://www.google.com/search?q=$encoded&tbm=isch&hl=pt-BR&gl=br$safeParam$effectiveQuality"
                    com.example.model.SearchType.VIDEOS ->
                        "https://www.google.com/search?q=$encoded&tbm=vid&hl=pt-BR&gl=br$safeParam"
                    com.example.model.SearchType.ALL_WEB ->
                        "https://www.google.com/search?q=$encoded&hl=pt-BR&gl=br$safeParam"
                    com.example.model.SearchType.NEWS ->
                        "https://www.google.com/search?q=$encoded&tbm=nws&hl=pt-BR&gl=br$safeParam"
                }
            }
            com.example.model.SearchEngine.BING -> {
                val safeParam = if (isSafeSearchDisabled) "&adlt=off" else "&adlt=strict"
                val hqParam = if (isHighQualityFilter) "&qft=+filterui:imagesize-large" else ""
                when (searchType) {
                    com.example.model.SearchType.IMAGES ->
                        "https://www.bing.com/images/search?q=$encoded&form=HDRSC2$safeParam$hqParam"
                    com.example.model.SearchType.VIDEOS ->
                        "https://www.bing.com/videos/search?q=$encoded$safeParam"
                    com.example.model.SearchType.ALL_WEB ->
                        "https://www.bing.com/search?q=$encoded$safeParam"
                    com.example.model.SearchType.NEWS ->
                        "https://www.bing.com/news/search?q=$encoded$safeParam"
                }
            }
            com.example.model.SearchEngine.DUCKDUCKGO -> {
                val safeParam = if (isSafeSearchDisabled) "&kp=-2" else "&kp=1"
                val hqParam = if (isHighQualityFilter) "&iaf=size:Large" else ""
                when (searchType) {
                    com.example.model.SearchType.IMAGES ->
                        "https://duckduckgo.com/?q=$encoded&iax=images&ia=images$safeParam$hqParam"
                    com.example.model.SearchType.VIDEOS ->
                        "https://duckduckgo.com/?q=$encoded&iax=videos&ia=videos$safeParam"
                    com.example.model.SearchType.ALL_WEB ->
                        "https://duckduckgo.com/?q=$encoded$safeParam"
                    com.example.model.SearchType.NEWS ->
                        "https://duckduckgo.com/?q=$encoded&iar=news&ia=news$safeParam"
                }
            }
            com.example.model.SearchEngine.YANDEX -> {
                val safeParam = if (isSafeSearchDisabled) "&family=0" else "&family=1"
                val hqParam = if (isHighQualityFilter) "&isize=wallpaper" else ""
                when (searchType) {
                    com.example.model.SearchType.IMAGES ->
                        "https://yandex.com/images/search?text=$encoded$safeParam$hqParam"
                    com.example.model.SearchType.VIDEOS ->
                        "https://yandex.com/video/search?text=$encoded$safeParam"
                    com.example.model.SearchType.ALL_WEB ->
                        "https://yandex.com/search/?text=$encoded$safeParam"
                    com.example.model.SearchType.NEWS ->
                        "https://yandex.com/news/search?text=$encoded$safeParam"
                }
            }
            com.example.model.SearchEngine.YAHOO -> {
                val safeParam = if (isSafeSearchDisabled) "&vm=p" else "&vm=r"
                val hqParam = if (isHighQualityFilter) "&imgsz=large" else ""
                when (searchType) {
                    com.example.model.SearchType.IMAGES ->
                        "https://images.search.yahoo.com/search/images?p=$encoded$safeParam$hqParam"
                    com.example.model.SearchType.VIDEOS ->
                        "https://video.search.yahoo.com/search/video?p=$encoded$safeParam"
                    com.example.model.SearchType.ALL_WEB ->
                        "https://search.yahoo.com/search?p=$encoded$safeParam"
                    com.example.model.SearchType.NEWS ->
                        "https://news.search.yahoo.com/search?p=$encoded$safeParam"
                }
            }
        }
    }

    fun buildGoogleImagesUrl(query: String, qualityParam: String = ""): String {
        return buildSearchUrl(com.example.model.SearchEngine.GOOGLE, query, qualityParam)
    }

    fun buildAlternativeSearchUrl(query: String): String {
        return buildSearchUrl(com.example.model.SearchEngine.DUCKDUCKGO, query)
    }

    fun getCleanViewCssInjectionScript(): String {
        return """
            (function() {
                var style = document.getElementById('scrollsearch-custom-style');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'scrollsearch-custom-style';
                    document.head.appendChild(style);
                }
                style.innerHTML = `
                    header, #header, #searchform, .mNsI7b, .F9640c, .OqTqVd, .yg07nd,
                    .k27Ahb, footer, #footer, .NZmxZe, #swml, .fbar, #sfooter,
                    div[role="navigation"], div[role="banner"], form[role="search"],
                    div[aria-modal="true"], #lb, .VDgVie, form[action*="consent"] {
                        display: none !important;
                    }
                    body {
                        background-color: #12121E !important;
                        color: #FFFFFF !important;
                        padding-top: 0px !important;
                        margin: 0px !important;
                        -webkit-overflow-scrolling: touch !important;
                        touch-action: pan-y !important;
                        overflow-y: auto !important;
                    }
                    img {
                        border-radius: 8px !important;
                        content-visibility: auto !important;
                        contain-intrinsic-size: 200px 200px !important;
                        transform: translateZ(0) !important;
                        -webkit-transform: translateZ(0) !important;
                    }
                `;

                // Auto-accept Google Consent prompt if present to unblock search results
                try {
                    var consentBtn = document.querySelector('button#L2AGLb, button[id*="L2AGLb"], form[action*="consent"] button, button[aria-label*="Aceitar"], button[aria-label*="Accept"], div[aria-label*="Aceitar tudo"]');
                    if (consentBtn) { consentBtn.click(); }
                } catch(e) {}

                // Set SafeSearch disabled cookie on DuckDuckGo and Bing
                try {
                    if (window.location.hostname.includes('duckduckgo')) {
                        document.cookie = "kp=-2; path=/; domain=.duckduckgo.com";
                    } else if (window.location.hostname.includes('bing')) {
                        document.cookie = "SRCHHPGUSR=ADLT=OFF; path=/; domain=.bing.com";
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    fun getPurePhotosCssInjectionScript(): String {
        return """
            (function() {
                var style = document.getElementById('scrollsearch-custom-style');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'scrollsearch-custom-style';
                    document.head.appendChild(style);
                }
                style.innerHTML = `
                    header, #header, #searchform, .mNsI7b, .F9640c, .OqTqVd, .yg07nd,
                    .k27Ahb, footer, #footer, .NZmxZe, #swml, .fbar, #sfooter,
                    div[role="navigation"], div[role="banner"], form[role="search"],
                    #sfc, .hdtb-mbot, .t6P1ce, .NZmxZe, .Q3Abe, .f7Wdxd, .j31l1e,
                    div[jsname="GZIdke"], div[jscontroller="Csnxkd"],
                    div.crJ18e, div.L99Ud, div.e6i7be, #taw, .appbar, #botstuff,
                    .W4DClc, .g3P7Id, #rhs, div[data-sug],
                    div[aria-modal="true"], #lb, .VDgVie, form[action*="consent"], div[role="dialog"] {
                        display: none !important;
                        height: 0px !important;
                        visibility: hidden !important;
                    }
                    html, body {
                        background-color: #0A0A12 !important;
                        color: #FFFFFF !important;
                        padding: 0px !important;
                        margin: 0px !important;
                        overflow-x: hidden !important;
                        overflow-y: auto !important;
                        -webkit-overflow-scrolling: touch !important;
                        touch-action: pan-y !important;
                    }
                    img {
                        border-radius: 12px !important;
                        content-visibility: auto !important;
                        contain-intrinsic-size: 200px 250px !important;
                        transform: translateZ(0) !important;
                        -webkit-transform: translateZ(0) !important;
                    }
                    .isv-r, .tile--img, .img_cont, .iusc, .grid-item {
                        border-radius: 12px !important;
                        margin: 4px !important;
                        contain: layout paint !important;
                        transform: translateZ(0) !important;
                    }
                `;

                // Auto-accept Google Consent prompt if present to unblock search results
                try {
                    var consentBtn = document.querySelector('button#L2AGLb, button[id*="L2AGLb"], form[action*="consent"] button, button[aria-label*="Aceitar"], button[aria-label*="Accept"], div[aria-label*="Aceitar tudo"]');
                    if (consentBtn) { consentBtn.click(); }
                } catch(e) {}

                // Set SafeSearch disabled cookie on DuckDuckGo and Bing
                try {
                    if (window.location.hostname.includes('duckduckgo')) {
                        document.cookie = "kp=-2; path=/; domain=.duckduckgo.com";
                    } else if (window.location.hostname.includes('bing')) {
                        document.cookie = "SRCHHPGUSR=ADLT=OFF; path=/; domain=.bing.com";
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Mode F7: Exactly ONE image per display screen
     */
    fun getF7SingleImageCssInjectionScript(): String {
        return """
            (function() {
                var style = document.getElementById('scrollsearch-custom-style');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'scrollsearch-custom-style';
                    document.head.appendChild(style);
                }
                style.innerHTML = `
                    header, #header, #searchform, .mNsI7b, .F9640c, .OqTqVd, .yg07nd,
                    .k27Ahb, footer, #footer, .NZmxZe, #swml, .fbar, #sfooter,
                    div[role="navigation"], div[role="banner"], form[role="search"],
                    #sfc, .hdtb-mbot, .t6P1ce, .NZmxZe, .Q3Abe, .f7Wdxd, .j31l1e,
                    div[jsname="GZIdke"], div[jscontroller="Csnxkd"],
                    div.crJ18e, div.L99Ud, div.e6i7be, #taw, .appbar, #botstuff,
                    .W4DClc, .g3P7Id, #rhs, div[data-sug],
                    div[aria-modal="true"], #lb, .VDgVie, form[action*="consent"], div[role="dialog"] {
                        display: none !important;
                        height: 0px !important;
                        visibility: hidden !important;
                    }
                    html, body {
                        background-color: #0A0A12 !important;
                        color: #FFFFFF !important;
                        padding: 0px !important;
                        margin: 0px !important;
                        overflow-x: hidden !important;
                        -webkit-overflow-scrolling: touch !important;
                        scroll-behavior: smooth !important;
                    }
                    /* Force parent image containers to a single vertical column */
                    #islrg, .islrc, .z75nLe, div[jsname="GZIdke"], .results--images, .zmg, .dg_u, .b_focusTextExtra, .serp__results, .layout-images, .b_canvas, #b_content, #b_results {
                        display: block !important;
                        column-count: 1 !important;
                        -webkit-column-count: 1 !important;
                        grid-template-columns: 100vw !important;
                        width: 100vw !important;
                        max-width: 100vw !important;
                        padding: 0 !important;
                        margin: 0 !important;
                    }
                    /* Mode F7: Large image card filling the display cleanly with fluid scrolling */
                    div[data-ri], .isv-r, .grid-item, .img_cont, .serp__item, div[jsname="r5xl4"], .tile--img, .iusc, .iuscp {
                        width: 100vw !important;
                        max-width: 100vw !important;
                        min-width: 100vw !important;
                        height: 85vh !important;
                        min-height: 85vh !important;
                        max-height: 85vh !important;
                        margin: 0px 0px 16px 0px !important;
                        padding: 6px !important;
                        box-sizing: border-box !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: center !important;
                        position: relative !important;
                        float: none !important;
                        clear: both !important;
                    }
                    div[data-ri] img, .isv-r img, .grid-item img, .img_cont img, .tile--img img, .mimg, .iusc img {
                        width: 100% !important;
                        height: 100% !important;
                        max-width: 100vw !important;
                        max-height: 82vh !important;
                        object-fit: contain !important;
                        border-radius: 16px !important;
                    }
                `;

                // Auto-accept Google Consent prompt if present to unblock search results
                try {
                    var consentBtn = document.querySelector('button#L2AGLb, button[id*="L2AGLb"], form[action*="consent"] button, button[aria-label*="Aceitar"], button[aria-label*="Accept"], div[aria-label*="Aceitar tudo"]');
                    if (consentBtn) { consentBtn.click(); }
                } catch(e) {}

                // Set SafeSearch disabled cookie on DuckDuckGo and Bing
                try {
                    if (window.location.hostname.includes('duckduckgo')) {
                        document.cookie = "kp=-2; path=/; domain=.duckduckgo.com";
                    } else if (window.location.hostname.includes('bing')) {
                        document.cookie = "SRCHHPGUSR=ADLT=OFF; path=/; domain=.bing.com";
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    fun getImageOverlayButtonsScript(): String {
        return """
            (function() {
                try {
                    var style = document.getElementById('scrollsearch-buttons-style');
                    if (!style) {
                        style = document.createElement('style');
                        style.id = 'scrollsearch-buttons-style';
                        style.innerHTML = `
                            .scrollsearch-btn-container {
                                position: absolute !important;
                                top: 6px !important;
                                right: 6px !important;
                                display: flex !important;
                                gap: 7px !important;
                                z-index: 2147483647 !important;
                                pointer-events: auto !important;
                                -webkit-user-select: none !important;
                                user-select: none !important;
                            }
                            .scrollsearch-btn {
                                width: 36px !important;
                                height: 36px !important;
                                min-width: 36px !important;
                                min-height: 36px !important;
                                border-radius: 50% !important;
                                background: rgba(10, 10, 22, 0.90) !important;
                                border: 1.5px solid rgba(255, 255, 255, 0.40) !important;
                                display: inline-flex !important;
                                align-items: center !important;
                                justify-content: center !important;
                                cursor: pointer !important;
                                box-shadow: 0 4px 10px rgba(0,0,0,0.65) !important;
                                padding: 0 !important;
                                outline: none !important;
                                touch-action: manipulation !important;
                                -webkit-tap-highlight-color: transparent !important;
                                transition: transform 0.1s ease !important;
                            }
                            .scrollsearch-btn:active {
                                transform: scale(0.90) !important;
                                background: rgba(30, 30, 50, 0.98) !important;
                            }
                            .scrollsearch-btn svg {
                                width: 19px !important;
                                height: 19px !important;
                                pointer-events: none !important;
                            }
                            .scrollsearch-btn.download svg {
                                fill: #FF3366 !important;
                            }
                            .scrollsearch-btn.fullscreen svg {
                                fill: #00E5FF !important;
                            }
                        `;
                        document.head.appendChild(style);
                    }

                    function captureImageAsBase64(img) {
                        try {
                            var src = img.src || img.getAttribute('data-src') || img.getAttribute('data-iurl');
                            if (src && src.startsWith('data:image')) {
                                return src;
                            }
                            var canvas = document.createElement('canvas');
                            canvas.width = img.naturalWidth || img.width || 300;
                            canvas.height = img.naturalHeight || img.height || 300;
                            var ctx = canvas.getContext('2d');
                            ctx.drawImage(img, 0, 0);
                            return canvas.toDataURL('image/jpeg', 0.95);
                        } catch(e) {
                            return null;
                        }
                    }

                    function getValidPhotoImages() {
                        var all = document.querySelectorAll('img');
                        var valid = [];
                        for (var i = 0; i < all.length; i++) {
                            var img = all[i];
                            var src = img.src || img.getAttribute('data-src') || img.getAttribute('data-iurl') || '';
                            if (!src || src.includes('googlelogo') || src.includes('cleardot') || src.includes('arrow_') || src.includes('quantum') || src.includes('favicon')) continue;
                            var w = img.naturalWidth || img.width || 0;
                            var h = img.naturalHeight || img.height || 0;
                            if (w >= 40 || h >= 40 || src.includes('gstatic.com') || src.includes('bing.com') || src.startsWith('data:image')) {
                                valid.push(img);
                            }
                        }
                        return valid;
                    }

                    function collectAllUrls(validImgs) {
                        var urls = [];
                        var seen = {};
                        for (var k = 0; k < validImgs.length; k++) {
                            var u = validImgs[k].src || validImgs[k].getAttribute('data-src') || validImgs[k].getAttribute('data-iurl') || '';
                            if (u && (u.startsWith('http') || u.startsWith('data:image')) && !seen[u]) {
                                seen[u] = true;
                                urls.push(u);
                            }
                        }
                        return urls;
                    }

                    function attachButtons() {
                        var validImgs = getValidPhotoImages();
                        for (var i = 0; i < validImgs.length; i++) {
                            var img = validImgs[i];
                            img.dataset.scrollsearchIndex = i.toString();
                            if (img.dataset.scrollsearchReady) continue;
                            img.dataset.scrollsearchReady = 'true';

                            var src = img.src || img.getAttribute('data-src') || img.getAttribute('data-iurl');
                            var parent = img.parentElement;
                            if (!parent) continue;

                            if (parent.style.position !== 'relative' && parent.style.position !== 'absolute') {
                                parent.style.position = 'relative';
                            }

                            var container = document.createElement('div');
                            container.className = 'scrollsearch-btn-container';

                            // 1. Fullscreen Zoom button
                            var fsBtn = document.createElement('button');
                            fsBtn.className = 'scrollsearch-btn fullscreen';
                            fsBtn.title = 'Visualizar Tela Cheia & Zoom';
                            fsBtn.setAttribute('aria-label', 'Visualizar Tela Cheia & Zoom');
                            fsBtn.innerHTML = '<svg viewBox="0 0 24 24"><path d="M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z"/></svg>';

                            (function(currentImg, fallbackSrc, btn) {
                                var handleOpen = function(e) {
                                    if (e) {
                                        e.stopPropagation();
                                        e.preventDefault();
                                    }
                                    var currentList = getValidPhotoImages();
                                    var idx = currentList.indexOf(currentImg);
                                    if (idx < 0) {
                                        idx = parseInt(currentImg.dataset.scrollsearchIndex, 10);
                                        if (isNaN(idx)) idx = 0;
                                    }

                                    var rect = currentImg.getBoundingClientRect();
                                    var dpr = window.devicePixelRatio || 1.0;
                                    var base64 = captureImageAsBase64(currentImg);
                                    var s = (base64 && base64.startsWith('data:image')) ? base64 : (currentImg.src || currentImg.getAttribute('data-src') || currentImg.getAttribute('data-iurl') || fallbackSrc);
                                    
                                    var allUrls = collectAllUrls(currentList);
                                    if (idx >= 0 && idx < allUrls.length && base64 && base64.startsWith('data:image')) {
                                        allUrls[idx] = base64;
                                    }
                                    var allUrlsJson = JSON.stringify(allUrls);

                                    if (window.ScrollSearchBridge && window.ScrollSearchBridge.onCaptureAndOpenFullscreenWithIndex) {
                                        window.ScrollSearchBridge.onCaptureAndOpenFullscreenWithIndex(rect.left, rect.top, rect.width, rect.height, dpr, s, allUrlsJson, idx);
                                    } else if (window.ScrollSearchBridge && window.ScrollSearchBridge.onCaptureAndOpenFullscreen) {
                                        window.ScrollSearchBridge.onCaptureAndOpenFullscreen(rect.left, rect.top, rect.width, rect.height, dpr, s, allUrlsJson);
                                    } else if (window.ScrollSearchBridge && window.ScrollSearchBridge.onOpenFullscreen) {
                                        window.ScrollSearchBridge.onOpenFullscreen(s, allUrlsJson);
                                    }
                                };
                                btn.addEventListener('click', handleOpen, true);
                                btn.addEventListener('touchend', handleOpen, true);
                            })(img, src, fsBtn);

                            // 2. Download button
                            var dlBtn = document.createElement('button');
                            dlBtn.className = 'scrollsearch-btn download';
                            dlBtn.title = 'Baixar Foto';
                            dlBtn.setAttribute('aria-label', 'Baixar Foto');
                            dlBtn.innerHTML = '<svg viewBox="0 0 24 24"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z"/></svg>';

                            (function(currentImg, fallbackSrc, btn) {
                                var handleDownload = function(e) {
                                    if (e) {
                                        e.stopPropagation();
                                        e.preventDefault();
                                    }
                                    var rect = currentImg.getBoundingClientRect();
                                    var dpr = window.devicePixelRatio || 1.0;
                                    var base64 = captureImageAsBase64(currentImg);
                                    var s = (base64 && base64.startsWith('data:image')) ? base64 : (currentImg.src || currentImg.getAttribute('data-src') || currentImg.getAttribute('data-iurl') || fallbackSrc);

                                    // Direct Base64 capture from the already rendered DOM/canvas (zero 403)
                                    if (base64 && base64.startsWith('data:image')) {
                                        if (window.ScrollSearchBridge && window.ScrollSearchBridge.onDownloadBase64) {
                                            window.ScrollSearchBridge.onDownloadBase64(base64, 'ScrollSearch_Photo');
                                            return;
                                        }
                                    }
                                    if (window.ScrollSearchBridge && window.ScrollSearchBridge.onCaptureAndDownload) {
                                        window.ScrollSearchBridge.onCaptureAndDownload(rect.left, rect.top, rect.width, rect.height, dpr, s, 'ScrollSearch_Photo');
                                        return;
                                    }
                                    if (window.ScrollSearchBridge && window.ScrollSearchBridge.onDownloadImageWithReferrer) {
                                        window.ScrollSearchBridge.onDownloadImageWithReferrer(s, window.location.href || 'https://www.google.com/', 'ScrollSearch_Photo');
                                    } else if (window.ScrollSearchBridge && window.ScrollSearchBridge.onDownloadImage) {
                                        window.ScrollSearchBridge.onDownloadImage(s);
                                    }
                                };
                                btn.addEventListener('click', handleDownload, true);
                                btn.addEventListener('touchend', handleDownload, true);
                            })(img, src, dlBtn);

                            container.appendChild(fsBtn);
                            container.appendChild(dlBtn);
                            parent.appendChild(container);
                        }
                    }

                    var isAttaching = false;
                    function safeAttachButtons() {
                        if (isAttaching) return;
                        isAttaching = true;
                        try {
                            attachButtons();
                        } finally {
                            setTimeout(function() { isAttaching = false; }, 60);
                        }
                    }

                    safeAttachButtons();

                    if (!window.__scrollsearchObserverInstalled) {
                        window.__scrollsearchObserverInstalled = true;
                        var isQueued = false;
                        var throttledAttach = function() {
                            if (isQueued || isAttaching) return;
                            isQueued = true;
                            window.requestAnimationFrame(function() {
                                safeAttachButtons();
                                isQueued = false;
                            });
                        };

                        var observer = new MutationObserver(function(mutations) {
                            if (isAttaching) return;
                            var shouldTrigger = false;
                            for (var m = 0; m < mutations.length; m++) {
                                var target = mutations[m].target;
                                if (target && !(target.classList && target.classList.contains('scrollsearch-btn-container'))) {
                                    shouldTrigger = true;
                                    break;
                                }
                            }
                            if (shouldTrigger) throttledAttach();
                        });
                        observer.observe(document.body, { childList: true, subtree: true });

                        var scrollDebounce = null;
                        window.addEventListener('scroll', function() {
                            if (scrollDebounce) clearTimeout(scrollDebounce);
                            // Only run after scroll is completely finished to keep 60fps scrolling
                            scrollDebounce = setTimeout(safeAttachButtons, 350);
                        }, { passive: true });
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    fun getImageExtractorScript(): String {
        return """
            (function() {
                try {
                    var urls = [];
                    var seen = {};
                    var imgs = document.querySelectorAll('img');
                    for (var i = 0; i < imgs.length; i++) {
                        var el = imgs[i];
                        var src = el.src || el.getAttribute('data-src') || el.getAttribute('data-iurl') || el.getAttribute('src');
                        if (src && !seen[src]) {
                            if (!src.includes('googlelogo') && !src.includes('cleardot') && !src.includes('arrow_') && !src.includes('quantum') && !src.includes('favicon')) {
                                if (src.includes('gstatic.com') || src.startsWith('data:image') || src.startsWith('http') || (el.naturalWidth && el.naturalWidth > 40) || (el.width && el.width > 40)) {
                                    seen[src] = true;
                                    urls.push(src);
                                }
                            }
                        }
                    }
                    var extraElements = document.querySelectorAll('[data-src], [data-iurl]');
                    for (var j = 0; j < extraElements.length; j++) {
                        var elem = extraElements[j];
                        var dSrc = elem.getAttribute('data-src') || elem.getAttribute('data-iurl');
                        if (dSrc && !seen[dSrc] && (dSrc.startsWith('http') || dSrc.startsWith('data:image'))) {
                            seen[dSrc] = true;
                            urls.push(dSrc);
                        }
                    }
                    var jsonStr = JSON.stringify(urls);
                    if (window.ScrollSearchBridge && urls.length > 0) {
                        window.ScrollSearchBridge.onReceiveImages(jsonStr);
                    }
                    return jsonStr;
                } catch(e) {
                    return "[]";
                }
            })();
        """.trimIndent()
    }

    /**
     * Fetches high-resolution photos using multiple sources:
     * 1. Google Images search extraction
     * 2. Wikipedia Media API (verified, high-resolution creator/celebrity photos)
     * 3. Curated HD fallback photography
     */
    suspend fun fetchCleanPhotos(query: String): List<PhotoItem> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<PhotoItem>()
        val seenUrls = mutableSetOf<String>()

        // Strategy 1: Wikipedia high resolution API for verified creator/celebrity images
        try {
            val creatorName = query.split(" ").firstOrNull()?.removePrefix("@") ?: query
            val wikiUrl = "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=${URLEncoder.encode(creatorName, "UTF-8")}&gsrlimit=8&prop=pageimages&piprop=original|thumbnail&pithumbsize=1000&format=json"

            val req = Request.Builder()
                .url(wikiUrl)
                .header("User-Agent", "ScrollSearchApp/1.0 (Android; Contact: support@example.com)")
                .build()

            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val jsonStr = resp.body?.string().orEmpty()
                val json = JSONObject(jsonStr)
                val pages = json.optJSONObject("query")?.optJSONObject("pages")
                if (pages != null) {
                    val keys = pages.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val pageObj = pages.getJSONObject(key)
                        val pageTitle = pageObj.optString("title")
                        val origObj = pageObj.optJSONObject("original")
                        val thumbObj = pageObj.optJSONObject("thumbnail")
                        val imgUrl = origObj?.optString("source") ?: thumbObj?.optString("source")
                        if (!imgUrl.isNullOrBlank() && seenUrls.add(imgUrl)) {
                            resultList.add(
                                PhotoItem(
                                    id = "wiki_${key}",
                                    url = imgUrl,
                                    title = "$pageTitle",
                                    sourceDomain = "Wikipédia HD",
                                    width = 800,
                                    height = 1000
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Wiki API extraction: ${e.message}")
        }

        // Strategy 2: Google Images HTML extraction
        try {
            val url = buildGoogleImagesUrl(query)
            val request = Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                )
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Referer", "https://www.google.com/")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string().orEmpty()

            val tbnPattern = Pattern.compile("(https://encrypted-tbn[0-9]\\.gstatic\\.com/images\\?q=tbn:[^\\\\\"'\\s&<]+)")
            val tbnMatcher = tbnPattern.matcher(html)
            var count = 0
            while (tbnMatcher.find() && count < 20) {
                val rawGroup = tbnMatcher.group(1) ?: continue
                val imgUrl = rawGroup.replace("\\u003d", "=").replace("\\u0026", "&")
                if (imgUrl.isNotEmpty() && seenUrls.add(imgUrl)) {
                    resultList.add(
                        PhotoItem(
                            id = "photo_${query.hashCode()}_$count",
                            url = imgUrl,
                            title = "$query #${count + 1}",
                            sourceDomain = "Google Imagens",
                            width = 600,
                            height = 800
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Google HTML photo extraction: ${e.message}")
        }

        // Strategy 3: Guaranteed high-resolution photography fallback
        if (resultList.isEmpty()) {
            val curatedHighRes = listOf(
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=85",
                "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=800&q=85"
            )

            curatedHighRes.forEachIndexed { index, photoUrl ->
                resultList.add(
                    PhotoItem(
                        id = "hq_${query.hashCode()}_$index",
                        url = photoUrl,
                        title = "$query - Foto #${index + 1}",
                        sourceDomain = "ScrollSearch HD",
                        width = 800,
                        height = 1000
                    )
                )
            }
        }

        resultList
    }

    const val DEFAULT_SEARCH_ENGINE_ID = "937552ab3e8894f76"

    fun getGoogleCustomSearchApiKey(): String {
        val key = try {
            com.example.BuildConfig.GOOGLE_CUSTOM_SEARCH_API_KEY
        } catch (_: Throwable) {
            ""
        }
        return if (key.isBlank() || key.contains("your_google_custom_search_api_key")) "" else key
    }

    fun getGoogleSearchEngineId(): String {
        val configured = try {
            com.example.BuildConfig.GOOGLE_SEARCH_ENGINE_ID
        } catch (_: Throwable) {
            ""
        }
        return if (configured.isNotBlank()) configured else DEFAULT_SEARCH_ENGINE_ID
    }

    /**
     * Searches images using Google Custom Search JSON API (Premium Plan).
     * Endpoint: https://www.googleapis.com/customsearch/v1
     * Parameters: key, cx, q, searchType=image, num=10
     */
    suspend fun fetchGoogleCustomSearchPhotos(
        query: String,
        apiKey: String = getGoogleCustomSearchApiKey(),
        cx: String = getGoogleSearchEngineId(),
        num: Int = 10
    ): List<PhotoItem> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().ifBlank { "tiktok creator" }
        val effectiveKey = apiKey.trim()
        val effectiveCx = cx.trim().ifBlank { DEFAULT_SEARCH_ENGINE_ID }

        if (effectiveKey.isBlank()) {
            Log.w(TAG, "Google Custom Search API Key is not configured in secrets.")
            return@withContext emptyList()
        }

        val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
        val effectiveNum = num.coerceIn(1, 10)
        val url = "https://www.googleapis.com/customsearch/v1?key=$effectiveKey&cx=$effectiveCx&q=$encodedQuery&searchType=image&num=$effectiveNum"

        val resultList = mutableListOf<PhotoItem>()
        try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "TikTokLens/1.0 (Android)")
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Google Custom Search API request failed [${response.code}]: $bodyString")
                return@withContext emptyList()
            }

            val json = JSONObject(bodyString)
            val items = json.optJSONArray("items") ?: return@withContext emptyList()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val title = item.optString("title", "Imagem")
                val link = item.optString("link") // Direct original high-resolution image URL
                val displayLink = item.optString("displayLink", "Google Custom Search")
                val imageObj = item.optJSONObject("image")
                val width = imageObj?.optInt("width", 800) ?: 800
                val height = imageObj?.optInt("height", 800) ?: 800

                if (link.isNotBlank() && (link.startsWith("http://") || link.startsWith("https://"))) {
                    resultList.add(
                        PhotoItem(
                            id = "gcs_${cleanQuery.hashCode()}_$i",
                            url = link,
                            title = title,
                            sourceDomain = displayLink,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Google Custom Search API execution: ${e.message}", e)
        }
        resultList
    }
}
