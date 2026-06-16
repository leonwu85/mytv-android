package top.yogiczy.mytv.ui.utils

import android.content.Context
import android.widget.Toast
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.http.body.JSONObjectBody
import com.koushikdutta.async.http.body.MultipartFormDataBody
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import top.yogiczy.mytv.AppGlobal
import top.yogiczy.mytv.R
import top.yogiczy.mytv.data.repositories.epg.EpgRepository
import top.yogiczy.mytv.data.repositories.iptv.IptvRepository
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.utils.ApkInstaller
import top.yogiczy.mytv.utils.Loggable
import top.yogiczy.mytv.utils.Logger
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.net.Inet4Address
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.URL

object HttpServer : Loggable() {
    private val SERVER_PORTS = listOf(8080, 10481, 18080)

    @Volatile
    private var activeServerPort = SERVER_PORTS.first()

    @Volatile
    private var server: AsyncHttpServer? = null

    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var isStarting = false

    private val uploadedApkFile = File(AppGlobal.cacheDir, "uploaded_apk.apk").apply {
        deleteOnExit()
    }

    private var showToast: (String) -> Unit = { }

    val serverUrl: String
        get() = "http://${SP.httpServerAdvertiseIp.ifBlank { getLocalIpAddress() }}:${activeServerPort}"

    fun start(context: Context, showToast: (String) -> Unit) {
        HttpServer.showToast = showToast
        synchronized(this) {
            if (serverSocket != null || isStarting) return
            isStarting = true
        }

        CoroutineScope(Dispatchers.IO).launch {
            var socket: ServerSocket? = null
            try {
                val boundServer = bindFirstAvailableServerSocket()
                socket = boundServer.first
                activeServerPort = boundServer.second

                synchronized(this@HttpServer) {
                    serverSocket = socket
                    isStarting = false
                }

                val startedUrl = serverUrl
                log.i("HTTP server started: $startedUrl")
                launch(Dispatchers.Main) {
                    HttpServer.showToast("配置服务已启动: $startedUrl")
                }

                launch {
                    val selfCheckPassed = checkLocalPing()
                    launch(Dispatchers.Main) {
                        HttpServer.showToast(
                            "配置服务本机自检: ${if (selfCheckPassed) "通过" else "失败"}"
                        )
                    }
                }

                while (!socket.isClosed) {
                    val client = socket.accept()
                    launch {
                        handleHttpClient(client, context)
                    }
                }
            } catch (ex: Exception) {
                synchronized(this@HttpServer) {
                    if (serverSocket === socket) serverSocket = null
                    isStarting = false
                }
                socket?.close()
                log.e("HTTP server start failed: ${ex.message}", ex)
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "设置服务启动失败: ${ex.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun bindFirstAvailableServerSocket(): Pair<ServerSocket, Int> {
        var lastException: Exception? = null
        SERVER_PORTS.forEach { port ->
            val socket = ServerSocket()
            try {
                socket.reuseAddress = true
                socket.bind(InetSocketAddress(InetAddress.getByName("0.0.0.0"), port))
                return socket to port
            } catch (ex: Exception) {
                lastException = ex
                runCatching { socket.close() }
                log.e("HTTP server port $port unavailable: ${ex.message}", ex)
            }
        }

        throw lastException ?: IllegalStateException("No available HTTP server port")
    }

    private fun checkLocalPing(): Boolean {
        return try {
            val connection =
                URL("http://127.0.0.1:${activeServerPort}/api/ping").openConnection() as HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader().use { it.readText() == "ok" }
        } catch (ex: Exception) {
            log.e("HTTP server self-check failed: ${ex.message}", ex)
            false
        }
    }

    private fun handleHttpClient(client: Socket, context: Context) {
        client.use { socket ->
            socket.soTimeout = 10_000
            val input = BufferedInputStream(socket.getInputStream())
            val output = socket.getOutputStream()

            val requestLine = readHttpLine(input)
            if (requestLine.isNullOrBlank()) return

            val requestParts = requestLine.split(" ")
            if (requestParts.size < 2) {
                sendRawResponse(output, 400, "text/plain", "Bad Request".encodeToByteArray())
                return
            }

            val method = requestParts[0].uppercase()
            val path = requestParts[1].substringBefore("?")
            val headers = mutableMapOf<String, String>()

            while (true) {
                val line = readHttpLine(input) ?: return
                if (line.isEmpty()) break
                val separatorIndex = line.indexOf(':')
                if (separatorIndex > 0) {
                    headers[line.substring(0, separatorIndex).trim().lowercase()] =
                        line.substring(separatorIndex + 1).trim()
                }
            }

            val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
            val body = readRequestBody(input, contentLength)
            handleRawHttpRequest(context, method, path, body, output)
        }
    }

    private fun readHttpLine(input: BufferedInputStream): String? {
        val line = ByteArrayOutputStream()
        while (true) {
            val byte = input.read()
            if (byte == -1) return if (line.size() == 0) null else line.toString("UTF-8")
            if (byte == '\n'.code) return line.toString("UTF-8")
            if (byte != '\r'.code) line.write(byte)
        }
    }

    private fun readRequestBody(input: BufferedInputStream, contentLength: Int): ByteArray {
        if (contentLength <= 0) return ByteArray(0)

        val body = ByteArray(contentLength)
        var offset = 0
        while (offset < contentLength) {
            val read = input.read(body, offset, contentLength - offset)
            if (read == -1) break
            offset += read
        }

        return if (offset == contentLength) body else body.copyOf(offset)
    }

    private fun handleRawHttpRequest(
        context: Context,
        method: String,
        path: String,
        body: ByteArray,
        output: OutputStream,
    ) {
        when {
            method == "OPTIONS" -> sendRawResponse(output, 204, "text/plain", ByteArray(0))
            method == "HEAD" && path == "/" -> sendRawResponse(output, 200, "text/html", ByteArray(0))
            method == "GET" && path == "/" ->
                sendRawResource(output, context, "text/html", R.raw.index)

            method == "GET" && path == "/index_css.css" ->
                sendRawResource(output, context, "text/css", R.raw.index_css)

            method == "GET" && path == "/index_js.js" ->
                sendRawResource(output, context, "text/javascript", R.raw.index_js)

            method == "GET" && path == "/api/ping" ->
                sendRawResponse(output, 200, "text/plain", "ok".encodeToByteArray())

            method == "GET" && path == "/api/settings" ->
                sendRawResponse(output, 200, "application/json", settingsJson().encodeToByteArray())

            method == "POST" && path == "/api/settings" -> {
                updateSettings(body.decodeToString())
                sendRawResponse(output, 200, "text/plain", "success".encodeToByteArray())
            }

            method == "POST" && path == "/api/upload/apk" ->
                sendRawResponse(output, 501, "text/plain", "upload unsupported".encodeToByteArray())

            else -> sendRawResponse(output, 404, "text/plain", "Not Found".encodeToByteArray())
        }
    }

    private fun sendRawResource(
        output: OutputStream,
        context: Context,
        contentType: String,
        id: Int,
    ) {
        sendRawResponse(output, 200, contentType, context.resources.openRawResource(id).readBytes())
    }

    private fun sendRawResponse(
        output: OutputStream,
        code: Int,
        contentType: String,
        body: ByteArray,
    ) {
        val headers = buildString {
            append("HTTP/1.1 $code ${statusText(code)}\r\n")
            append("Content-Type: $contentType; charset=utf-8\r\n")
            append("Content-Length: ${body.size}\r\n")
            append("Connection: close\r\n")
            append("Access-Control-Allow-Methods: POST, GET, HEAD, DELETE, PUT, OPTIONS\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Access-Control-Allow-Headers: Origin, Content-Type, X-Auth-Token\r\n")
            append("\r\n")
        }
        output.write(headers.encodeToByteArray())
        output.write(body)
        output.flush()
    }

    private fun statusText(code: Int): String {
        return when (code) {
            200 -> "OK"
            204 -> "No Content"
            400 -> "Bad Request"
            404 -> "Not Found"
            501 -> "Not Implemented"
            else -> "OK"
        }
    }

    private fun settingsJson(): String {
        return Json.encodeToString(
            AllSettings(
                appTitle = Constants.APP_TITLE,
                appRepo = Constants.APP_REPO,
                iptvSourceUrl = SP.iptvSourceUrl,
                epgXmlUrl = SP.epgXmlUrl,
                videoPlayerUserAgent = SP.videoPlayerUserAgent,
                logHistory = Logger.history,
            )
        )
    }

    private fun updateSettings(body: String) {
        val json = JSONObject(body)
        val iptvSourceUrl = json.optString("iptvSourceUrl", SP.iptvSourceUrl)
        val epgXmlUrl = json.optString("epgXmlUrl", SP.epgXmlUrl)
        val videoPlayerUserAgent =
            json.optString("videoPlayerUserAgent", SP.videoPlayerUserAgent)

        if (SP.iptvSourceUrl != iptvSourceUrl) {
            SP.iptvSourceUrl = iptvSourceUrl
            IptvRepository().clearCache()
        }

        if (SP.epgXmlUrl != epgXmlUrl) {
            SP.epgXmlUrl = epgXmlUrl
            EpgRepository().clearCache()
        }

        SP.videoPlayerUserAgent = videoPlayerUserAgent
    }

    private fun registerRoutes(server: AsyncHttpServer, context: Context) {
        server.addAction("OPTIONS", ".*") { _, response ->
            wrapResponse(response).code(204).end()
        }

        server.addAction("HEAD", "/") { _, response ->
            wrapResponse(response).end()
        }

        server.get("/") { _, response ->
            handleRawResource(response, context, "text/html", R.raw.index)
        }
        server.get("/index_css.css") { _, response ->
            handleRawResource(response, context, "text/css", R.raw.index_css)
        }
        server.get("/index_js.js") { _, response ->
            handleRawResource(response, context, "text/javascript", R.raw.index_js)
        }

        server.get("/api/ping") { _, response ->
            wrapResponse(response).send("ok")
        }

        server.get("/api/settings") { _, response ->
            handleGetSettings(response)
        }

        server.post("/api/settings") { request, response ->
            handleSetSettings(request, response)
        }

        server.post("/api/upload/apk") { request, response ->
            handleUploadApk(request, response, context)
        }
    }

    private fun wrapResponse(response: AsyncHttpServerResponse) = response.apply {
        headers.set(
            "Access-Control-Allow-Methods", "POST, GET, HEAD, DELETE, PUT, OPTIONS"
        )
        headers.set("Access-Control-Allow-Origin", "*")
        headers.set(
            "Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token"
        )
    }

    private fun handleRawResource(
        response: AsyncHttpServerResponse,
        context: Context,
        contentType: String,
        id: Int,
    ) {
        wrapResponse(response).apply {
            setContentType(contentType)
            send(context.resources.openRawResource(id).readBytes().decodeToString())
        }
    }

    private fun handleGetSettings(response: AsyncHttpServerResponse) {
        wrapResponse(response).apply {
            setContentType("application/json")
            send(
                Json.encodeToString(
                    AllSettings(
                        appTitle = Constants.APP_TITLE,
                        appRepo = Constants.APP_REPO,
                        iptvSourceUrl = SP.iptvSourceUrl,
                        epgXmlUrl = SP.epgXmlUrl,
                        videoPlayerUserAgent = SP.videoPlayerUserAgent,
                        logHistory = Logger.history,
                    )
                )
            )
        }
    }

    private fun handleSetSettings(
        request: AsyncHttpServerRequest,
        response: AsyncHttpServerResponse,
    ) {
        val body = request.getBody<JSONObjectBody>().get()
        val iptvSourceUrl = body.get("iptvSourceUrl").toString()
        val epgXmlUrl = body.get("epgXmlUrl").toString()
        val videoPlayerUserAgent = body.get("videoPlayerUserAgent").toString()

        if (SP.iptvSourceUrl != iptvSourceUrl) {
            SP.iptvSourceUrl = iptvSourceUrl
            IptvRepository().clearCache()
        }

        if (SP.epgXmlUrl != epgXmlUrl) {
            SP.epgXmlUrl = epgXmlUrl
            EpgRepository().clearCache()
        }

        SP.videoPlayerUserAgent = videoPlayerUserAgent

        wrapResponse(response).send("success")
    }

    private fun handleUploadApk(
        request: AsyncHttpServerRequest,
        response: AsyncHttpServerResponse,
        context: Context,
    ) {
        val body = request.getBody<MultipartFormDataBody>()

        val os = uploadedApkFile.outputStream()
        val contentLength = request.headers["Content-Length"]?.toLong() ?: 1
        var hasReceived = 0L

        body.setMultipartCallback { part ->
            if (part.isFile) {
                body.setDataCallback { _, bb ->
                    val byteArray = bb.allByteArray
                    hasReceived += byteArray.size
                    showToast("正在接收文件: ${(hasReceived * 100f / contentLength).toInt()}%")
                    os.write(byteArray)
                }
            }
        }

        body.setEndCallback {
            showToast("文件接收完成")
            body.dataEmitter.close()
            os.flush()
            os.close()
            ApkInstaller.installApk(context, uploadedApkFile.path)
        }

        wrapResponse(response).send("success")
    }

    private fun getLocalIpAddress(): String {
        val defaultIp = "127.0.0.1"

        try {
            val candidates = mutableListOf<LocalIpCandidate>()
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (!networkInterface.isUp ||
                    networkInterface.isLoopback ||
                    networkInterface.isVirtual ||
                    networkInterface.isPointToPoint
                ) {
                    continue
                }

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (inetAddress !is Inet4Address ||
                        inetAddress.isLoopbackAddress ||
                        inetAddress.isLinkLocalAddress ||
                        inetAddress.isMulticastAddress ||
                        inetAddress.hostAddress == "0.0.0.0"
                    ) {
                        continue
                    }

                    candidates += LocalIpCandidate(
                        address = inetAddress,
                        interfaceName = networkInterface.name,
                        interfaceIndex = networkInterface.index,
                    )
                }
            }

            return candidates.sortedWith(
                compareByDescending<LocalIpCandidate> { it.address.isSiteLocalAddress }
                    .thenByDescending { it.interfaceName.isPreferredLanInterfaceName() }
                    .thenBy { it.interfaceIndex },
            ).firstOrNull()?.address?.hostAddress ?: defaultIp
        } catch (ex: SocketException) {
            log.e("IP Address: ${ex.message}", ex)
            return defaultIp
        }
    }

    private data class LocalIpCandidate(
        val address: Inet4Address,
        val interfaceName: String,
        val interfaceIndex: Int,
    )

    private fun String.isPreferredLanInterfaceName(): Boolean {
        val name = lowercase()
        return name.startsWith("wlan") ||
            name.startsWith("wifi") ||
            name.startsWith("eth") ||
            name.startsWith("en")
    }
}

@Serializable
private data class AllSettings(
    val appTitle: String,
    val appRepo: String,
    val iptvSourceUrl: String,
    val epgXmlUrl: String,
    val videoPlayerUserAgent: String,

    val logHistory: List<Logger.HistoryItem>,
)
