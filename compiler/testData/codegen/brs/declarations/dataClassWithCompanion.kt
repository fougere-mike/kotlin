// Data class with companion object test
data class Response(val code: Int, val message: String) {
    companion object {
        fun fromCode(code: Int): Response {
            return Response(code, "Message for code $code")
        }

        val DEFAULT_CODE = 200
    }
}

fun createFromCompanion(): Response {
    return Response.fromCode(404)
}

fun getDefaultCode(): Int {
    return Response.DEFAULT_CODE
}
