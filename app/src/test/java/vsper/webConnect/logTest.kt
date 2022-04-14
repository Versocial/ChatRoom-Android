package vsper.webConnect

import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


/*what*/class logTest {
    private val USER_AGENT = "vsper"
    private val CONTENT_TYPE = "multipart/form-data"
    private val postUrl = URL("http://82.156.202.134:8081/log/list/-1")


    fun testUploadLog() {
        val file = File("src/test/java/vsper/webConnect/log")
        uploadFile(postUrl, "logFile", file)
    }

    /**
     * 模拟文件post上传
     * @param urlStr（接口地址）
     * @param formName（接口file接收名）
     * @param fileName（需要上传文件的本地路径）
     * @return文件上传到接口返回的结果
     */
    fun uploadFile(url: URL, formName: String, file: File): String? {
        var baseResult: String? = null
        try {
            val newLine = "\r\n"
            val boundaryPrefix = "--"
            val BOUNDARY = "========7d4a6d158c9" // 模拟数据分隔线
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST" // 设置为POST请求
            conn.doOutput = true
            conn.doInput = true
            conn.setRequestProperty("User-Agent", USER_AGENT) // 设置请求头参数
            conn.setRequestProperty("Charsert", "UTF-8")
            conn.setRequestProperty("Content-Type", "$CONTENT_TYPE;boundary=$BOUNDARY")
            val out: OutputStream = conn.outputStream
            val sb = StringBuilder()
            sb.append(boundaryPrefix)
            sb.append(BOUNDARY)
            sb.append(boundaryPrefix)
            sb.append(newLine)
            sb.append("Content-Disposition: form-data;name=\"$formName\";filename=\"test\"$newLine")
            sb.append("Content-Type:application/octet-stream")
            sb.append(newLine)
            sb.append(newLine)
            out.write(sb.toString().toByteArray()) // 将参数头的数据写入到输出流中
            val `in` = DataInputStream(FileInputStream(file)) // 数据输入流,用于读取文件数据
            val bufferOut = ByteArray(1024)
            var bytes = 0
            while (`in`.read(bufferOut).also { bytes = it } != -1) { // 每次读1KB数据,并且将文件数据写入到输出流中
                out.write(bufferOut, 0, bytes)
            }
            out.write(newLine.toByteArray())
            `in`.close()
            val end_data = (newLine + boundaryPrefix + BOUNDARY
                    + boundaryPrefix + newLine).toByteArray()
            out.write(end_data)
            out.flush()
            out.close()
            val reader = BufferedReader(
                InputStreamReader(
                    conn.inputStream
                )
            )
            var line: String? = null
            val strs = StringBuffer("")
            while (reader.readLine().also { line = it } != null) {
                strs.append(line)
            }
            baseResult = strs.toString()
        } catch (e: Exception) {
            baseResult = e.message
        }
        return baseResult
    }


    fun upload2() {
        val client = OkHttpClient().newBuilder()
            .build()
        val mediaType: MediaType? = MediaType.parse("multipart/form-data")
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "logfile", "testlog1",
                RequestBody.create(
                    MediaType.parse("application/octet-stream"),
                    File("src/test/java/vsper/webConnect/log")
                )
            )
            .build()
        val request: Request = Request.Builder()
            .url("http://82.156.202.134:8081/log/list/-1")
            .method("POST", body)
            .addHeader("User-Agent", "vsper")
            .addHeader("Content-Type", "multipart/form-data")
            .build()
        val response = client.newCall(request).execute()

    }

}