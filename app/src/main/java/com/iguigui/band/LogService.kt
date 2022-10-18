package com.iguigui.band

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.Nullable
import com.orhanobut.logger.*
import java.io.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class LogService :Service() {

    private val externalFilesDir by lazy { getExternalFilesDir(null) }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        initLogger()
        Logger.d("LogService onCreate")
    }


    private fun initLogger() {
        val formatStrategyForAndroid = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
            .methodCount(5)         // (Optional) How many method line to show. Default 2
            .tag("wsy")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategyForAndroid) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        externalFilesDir?.let { MyDiskLogAdapter(it.absolutePath) }
            ?.let { Logger.addLogAdapter(it) }
    }

}




internal class MyDiskLogAdapter(path: String) : LogAdapter {
    private val formatStrategy: MyFormatStrateg = MyFormatStrateg.Builder().build(path)
    override fun isLoggable(priority: Int, @Nullable tag: String?): Boolean {
        return true
    }

    override fun log(priority: Int, @Nullable tag: String?, message: String) {
        formatStrategy.log(priority, tag, message)
    }

}


class MyFormatStrateg(builder: Builder) : FormatStrategy {


    private val NEW_LINE = System.getProperty("line.separator")
    private val NEW_LINE_REPLACEMENT = " <br> "
    private val SEPARATOR = ","


    private var date: Date
    private var dateFormat: SimpleDateFormat
    private var logStrategy: LogStrategy

    private var tag: String? = null

    init {
        date = builder.date
        dateFormat = builder.dateFormat
        logStrategy = builder.logStrategy
        tag = builder.tag
    }

    fun newBuilder(): Builder {
        return Builder()
    }


    override fun log(priority: Int, onceOnlyTag: String?, message: String) {
        var message = message
        val tag = onceOnlyTag?.let { formatTag(it) }
        date.time = System.currentTimeMillis()
        val builder = StringBuilder()

        // machine-readable date/time
        builder.append(java.lang.Long.toString(date!!.time))

        // human-readable date/time
        builder.append(SEPARATOR)
        builder.append(dateFormat!!.format(date))

        // level
        builder.append(SEPARATOR)
        builder.append(MyUtil.logLevel(priority))

        // tag
        builder.append(SEPARATOR)
        builder.append(tag)

        // message
        if (message.contains(NEW_LINE)) {
            // a new line would break the CSV format, so we replace it here
            message = message.replace(NEW_LINE.toRegex(), NEW_LINE_REPLACEMENT)
        }
        builder.append(SEPARATOR)
        builder.append(message)

        // new line
        builder.append(NEW_LINE)
        logStrategy!!.log(priority, tag, builder.toString())
    }

    @Nullable
    private fun formatTag(@Nullable tag: String): String? {
        return if (!MyUtil.isEmpty(tag) && !MyUtil.equals(this.tag, tag)) {
            this.tag + "-" + tag
        } else this.tag
    }

    class Builder() {
        lateinit var date: Date
        lateinit var dateFormat: SimpleDateFormat
        lateinit var logStrategy: LogStrategy
        var tag = "PRETTY_LOGGER"

        fun tag(@Nullable tag: String): Builder {
            this.tag = tag
            return this
        }

        fun build(path: String): MyFormatStrateg {
            date = Date()
            dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)
            val folder = path + File.separatorChar + "logger"
            val ht = HandlerThread("AndroidFileLogger.$folder")
            ht.start()
            val handler: Handler = MyLogStrategy.WriteHandler(ht.looper, folder, MAX_BYTES)
            logStrategy = MyLogStrategy(handler)
            return MyFormatStrateg(this)
        }

        companion object {
            private const val MAX_BYTES = 1024 * 1024 // 500K averages to a 4000 lines per file
        }
    }

}


internal class MyLogStrategy(private val handler: Handler) : LogStrategy {
    override fun log(level: Int, tag: String?, message: String) {

        // do nothing on the calling thread, simply pass the tag/msg to the background thread
        handler.sendMessage(handler.obtainMessage(level, message))
    }

    internal class WriteHandler(
        looper: Looper,
        private val folder: String,
        private val maxFileSize: Int
    ) :
        Handler(looper) {
        override fun handleMessage(msg: Message) {
            val content = msg.obj as String
            var fileWriter: FileWriter? = null
            val logFile = getLogFile(folder, "Logs")
            try {
                fileWriter = FileWriter(logFile, true)
                writeLog(fileWriter, content)
                fileWriter.flush()
                fileWriter.close()
            } catch (e: IOException) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush()
                        fileWriter.close()
                    } catch (e1: IOException) { /* fail silently */
                    }
                }
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        @Throws(IOException::class)
        private fun writeLog(fileWriter: FileWriter, content: String) {
            fileWriter.append(content)
        }

        private fun getLogFile(folderName: String, fileName: String): File? {
            val folder = File(folderName)
            if (!folder.exists()) {
                //TODO: What if folder is not created, what happens then?
                folder.mkdirs()
            }
            val files = folder.list()
            val filecount = files.size
            var newFile: File? = null
            var existingFile: File? = null
            newFile = File(folder, String.format("%s_%s.log", fileName, 0))
            while (newFile!!.exists()) {
                existingFile = newFile
                newFile = File(folder, String.format("%s_%s.log", fileName, filecount))
            }
            if (existingFile != null) {
                if (existingFile.length() >= maxFileSize) {
                    if (filecount >= 5) {
                        for (i in 0 until filecount) {
                            val localfile = File(folder.path + File.separator + files[i])
                            if (localfile.exists()) {
                                localfile.delete()
                            }
                        }
                    } else {
                        existingFile.renameTo(newFile)
                    }
                    newFile = File(folder, String.format("%s_%s.log", fileName, 0))
                    return newFile
                }
                return existingFile
            }
            return newFile
        }
    }
}


internal object MyUtil {
    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.length == 0
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     *
     * *Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.*
     *
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     *
     *
     * NOTE: Logic slightly change due to strict policy on CI -
     * "Inner assignments should be avoided"
     */
    fun equals(a: CharSequence?, b: CharSequence?): Boolean {
        if (a === b) return true
        if (a != null && b != null) {
            val length = a.length
            if (length == b.length) {
                return if (a is String && b is String) {
                    a == b
                } else {
                    for (i in 0 until length) {
                        if (a[i] != b[i]) return false
                    }
                    true
                }
            }
        }
        return false
    }

    /**
     * Copied from "android.util.Log.getStackTraceString()" in order to avoid usage of Android stack
     * in unit tests.
     *
     * @return Stack trace in form of String
     */
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun logLevel(value: Int): String {
        return when (value) {
            Log.VERBOSE -> "VERBOSE"
            Log.DEBUG -> "DEBUG"
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    fun toString(`object`: Any?): String {
        if (`object` == null) {
            return "null"
        }
        if (!`object`.javaClass.isArray) {
            return `object`.toString()
        }
        if (`object` is BooleanArray) {
            return Arrays.toString(`object` as BooleanArray?)
        }
        if (`object` is ByteArray) {
            return Arrays.toString(`object` as ByteArray?)
        }
        if (`object` is CharArray) {
            return Arrays.toString(`object` as CharArray?)
        }
        if (`object` is ShortArray) {
            return Arrays.toString(`object` as ShortArray?)
        }
        if (`object` is IntArray) {
            return Arrays.toString(`object` as IntArray?)
        }
        if (`object` is LongArray) {
            return Arrays.toString(`object` as LongArray?)
        }
        if (`object` is FloatArray) {
            return Arrays.toString(`object` as FloatArray?)
        }
        if (`object` is DoubleArray) {
            return Arrays.toString(`object` as DoubleArray?)
        }
        return if (`object` is Array<*>) {
            Arrays.deepToString(`object` as Array<Any?>?)
        } else "Couldn't find a correct type for the object"
    }

    fun <T> checkNotNull(obj: T?): T {
        if (obj == null) {
            throw NullPointerException()
        }
        return obj
    }
}