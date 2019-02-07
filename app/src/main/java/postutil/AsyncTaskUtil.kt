package postutil

import android.os.Handler

class AsyncTaskUtil {

    object AsyncNetUtils {
        operator fun get(url: String, callback: Callback) {
            val handler = Handler()
            Thread(Runnable {
                val response = NetUtils.get(url)
                handler.post { callback.onResponse(response) }
            }).start()
        }

        fun post(url: String, content: String, callback: Callback) {
            val handler = Handler()
            Thread(Runnable {
                val response = NetUtils.post(url, content)
                handler.post { callback.onResponse(response) }
            }).start()
        }

        interface Callback {
            fun onResponse(response: String?)
        }
    }
}
