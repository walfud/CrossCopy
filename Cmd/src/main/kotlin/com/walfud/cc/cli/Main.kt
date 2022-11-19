import com.walfud.cc.clientshare.*
import com.walfud.cc.projectshare.model.*
import com.walfud.extention.isUuid
import com.walfud.extention.toSimpleString
import kotlinx.cli.*
import java.io.File
import java.time.LocalDateTime

const val EXEC_NAME = "ccx"
const val CONFIG_KEY = "config"
const val RESET_KEY = "reset"
const val TOKEN_KEY = "token"
const val LS_KEY = "ls"
const val UPLOAD_KEY = "upload"
const val UPLOAD_FILE_KEY = "f"
const val DOWNLOAD_KEY = "download"
const val DOWNLOAD_OUTPUT_KEY = "o"

fun main(args: Array<String>) {
    val home = System.getenv("HOME")
    setupConfigDir(home)

    val parser = ArgParser(EXEC_NAME, strictSubcommandOptionsOrder = true)
    parser.subcommands(object : Subcommand(CONFIG_KEY, "$EXEC_NAME $CONFIG_KEY --help") {
        val configArgList by argument(
            ArgType.String,
            description = "$EXEC_NAME $CONFIG_KEY $TOKEN_KEY=ba32723b-adf1-4809-996d-b1ee8511b86e"
        ).vararg()

        override fun execute() {
            configArgList.forEach { configArg ->
                if (configArg.startsWith(RESET_KEY)) {
                    handleConfigReset()
                } else if (configArg.startsWith(TOKEN_KEY)) {
                    handleConfigToken(configArg)
                } else {
                    println("unknown command($configArg)")
                }
            }
        }

        /**
         * cc config reset
         *
         * reset user.
         */
        private fun handleConfigReset() {
            configFile.delete()
        }

        /**
         * cc config token=xxxxx
         */
        private fun handleConfigToken(tokenArg: String) {
            val kv = tokenArg.split('=')
            if (kv.size != 2) {
                throw RuntimeException("`token`: wrong argument count(${kv})")
            }
            val token = kv[1].trim()
            if (!token.isUuid()) {
                throw RuntimeException("`token`: not an uuid($token)")
            }

            // saved in `$HOME/.cc.json`
            val ccConfig = getConfigFromLocal()
            val newCcConfig = CcConfig(
                CONFIG_VERSION,
                token,
                ccConfig?.createTime ?: LocalDateTime.now(),
                ccConfig?.updateTime ?: LocalDateTime.now(),
            )
            saveConfigToLocal(newCcConfig)
        }
    })
    parser.subcommands(object : Subcommand(LS_KEY, "$EXEC_NAME $LS_KEY") {
        override fun execute() {
            handleLs()
        }

        /**
         * cc ls
         */
        private fun handleLs() {
            initializeConfigIfNeededSync()
            val things = getThingsFromServerSync()
            things.sortedBy {
                it.updateTime
            }
                .reversed()
                .forEachIndexed { indexed, thing ->
                    formatPrint(thing)
                    if (indexed != things.size - 1) {
                        println("-----------------------------------")
                    }
                }
        }

        /**
         * 2022-11-09 23:43:12(FILE):mylogo
         * -----------------------------------
         * 2022-11-09 22:59:58(TEXT):some text
         */
        private fun formatPrint(thing: Thing) {
            val typeName = when (thing.type) {
                THING_TYPE_TEXT -> "TEXT"
                THING_TYPE_FILE -> "FILE"
                else -> "unknown type(${thing.type})"
            }
            println("$typeName:${thing.content}")
        }
    })
    parser.subcommands(object : Subcommand(UPLOAD_KEY, "$EXEC_NAME $UPLOAD_KEY") {
        val uploadArg by argument(
            ArgType.String,
            description = "title in server"
        ).optional()
        val uploadFileOption by option(
            ArgType.String,
            shortName = UPLOAD_FILE_KEY,
            description = "local file"
        )

        override fun execute() {
            if (uploadFileOption == null) {
                if (uploadArg == null) {
                    println(helpMessage)
                    return
                }

                handleUploadText(uploadArg!!)
            } else {
                handleUploadFile(uploadArg, File(uploadFileOption!!))
            }
        }

        /**
         * cc upload https://cc.walfud.com
         */
        private fun handleUploadText(text: String) {
            initializeConfigIfNeededSync()
            val thing = uploadTextToServerSync(text)
            println("upload text success. $thing")

        }

        /**
         * cc upload -f ./logo.png [mylogo]
         */
        private fun handleUploadFile(name: String?, file: File) {
            initializeConfigIfNeededSync()
            val beginTime = System.currentTimeMillis()
            var currProgress = 0L
            val thing = uploadFileToServerSync(name ?: file.absolutePath, file) { bytesSentTotal, contentLength ->
                val progress = bytesSentTotal * 100 / contentLength
                if (currProgress < progress) {
                    currProgress = progress

                    val elapse = System.currentTimeMillis() - beginTime
                    println("progress($progress%), elapse(${elapse / 1000}s)")
                }
            }
            println("upload file success. $thing")
        }
    })
    parser.subcommands(object : Subcommand(DOWNLOAD_KEY, "$EXEC_NAME $DOWNLOAD_KEY") {
        val downloadArg by argument(
            ArgType.String,
            description = "server filename"
        )
        val downloadOption by option(
            ArgType.String,
            shortName = DOWNLOAD_OUTPUT_KEY,
            description = "local file or directory to write into"
        )

        override fun execute() {
            val filename = downloadArg.split(File.separator).last()
            var localFile = if (downloadOption == null) {
                File(".", filename)
            } else {
                var outputFile = if (downloadOption!!.startsWith("~")) {
                    val home = System.getenv("HOME")
                    File(home, downloadOption!!.substring(1))
                } else {
                    File(downloadOption!!)
                }
                if (outputFile.isDirectory) {
                    outputFile = File(outputFile, filename)
                }
                outputFile
            }

            // mkdirs
            localFile.parentFile.mkdirs()

            handleDownload(downloadArg, localFile)
        }

        /**
         * cc download "/home/cc/logo.png" -o ./logo.png
         */
        private fun handleDownload(serverFilename: String, localFile: File) {
            initializeConfigIfNeededSync()
            val beginTime = System.currentTimeMillis()
            var currProgress = 0L
            downloadFileFromServerSync(serverFilename, localFile) { bytesSentTotal, contentLength ->
                val progress = bytesSentTotal * 100 / contentLength
                if (currProgress < progress) {
                    currProgress = progress

                    val elapse = System.currentTimeMillis() - beginTime
                    println("progress($progress%), elapse(${elapse / 1000}s)")
                }
            }
            println("download file success. $serverFilename -> ${localFile.absolutePath}")
        }
    })
    parser.parse(args)
}