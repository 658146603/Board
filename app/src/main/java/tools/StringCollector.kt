package tools

object StringCollector {

    private val LOCAL_USER = "http://192.168.31.97:8080/board/user"
    private val LOCAL_MSG = "http://192.168.31.97:8080/board/msg"
    private val SERVER_USER = "http://101.132.122.143:8080/board/user"
    private val SERVER_MSG = "http://101.132.122.143:8080/board/msg"

    private val severMode: String
        get() = "LOCAL"

    val userServer: String
        get() = if (severMode == "SERVER") {
            SERVER_USER
        } else {
            LOCAL_USER
        }

    val msgServer: String
        get() = if (severMode == "SERVER") {
            SERVER_MSG
        } else {
            LOCAL_MSG
        }

}
