package org.example.project

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.io.IOException
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class ClientManager: Client.ResultHandler {
    companion object {
        private val logInit = AtomicBoolean(false)
    }

    private lateinit var client: Client

    fun init() {
        while (!logInit.load()) {
            if (logInit.compareAndSet(expectedValue = false, newValue = true)) {
                Client.setLogMessageHandler(0, {_, _->})
                try {
                    Client.execute(TdApi.SetLogVerbosityLevel(0))
                    Client.execute(TdApi.SetLogStream(TdApi.LogStreamFile(
                        (FileKit.filesDir / "tdlib.log").absolutePath(), // 文件路径
                        1 shl 27, // 文件大小
                        false // 是否重定向stderr
                    )))
                } catch (e: Client.ExecutionException) {
                    throw IOException("Cannot write to ${FileKit.filesDir}")
                }
            }
        }

        client = Client.create(this, null, null)
    }

    override fun onResult(obj: TdApi.Object) {
        obj.invokeWhenMatch<TdApi.UpdateAuthorizationState> { state ->
            println("更新用户状态 $obj")
            onAuthorizationStateUpdated(state.authorizationState)
        }
    }

    private fun onAuthorizationStateUpdated(authorizationState: TdApi.AuthorizationState) {
        authorizationState.invokeWhenMatch<TdApi.AuthorizationStateWaitTdlibParameters> {
            println("等待tdlib参数 $authorizationState")

            val request = TdApi.SetTdlibParameters().apply {
                databaseDirectory = FileKit.databasesDir.absolutePath()
                useMessageDatabase = true
                useSecretChats = true
                apiId = 94575
                apiHash = "a3406de8d171bb422bb6ddf3bbd800e2" // todo change to my api hash
                systemLanguageCode = "en"
                deviceModel = "Desktop"
                applicationVersion = "1.0"
            }
            client.send(request, null)
        }
        authorizationState.invokeWhenMatch<TdApi.AuthorizationStateWaitPhoneNumber> {
            println("等待电话号码")

        }
    }

    private inline fun <reified T: TdApi.Object> TdApi.Object.invokeWhenMatch(block: (T) -> Unit) {
        if (this is T) {
            block(this)
        }
    }
}