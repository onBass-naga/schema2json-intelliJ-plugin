package schema2json

data class Form(
    val url: String,
    val user: String,
    val password: CharArray,
    val schema: String,
    val driverPath: String,
    val outputDirectory: String,
    val postHookCommand: String,
    val rememberPassword: Boolean
) {
    fun hasRequiredError(): Boolean {
        return url.isNullOrBlank() || user.isNullOrBlank() || driverPath.isNullOrBlank() || outputDirectory.isNullOrBlank()
    }

    fun driverClassName(): String =
        when {
            url.contains("jdbc:postgresql:") -> "org.postgresql.Driver"
            url.contains("jdbc:mysql:") -> "com.mysql.jdbc.Driver"
            url.contains("jdbc:mariadb:") -> "org.mariadb.jdbc.Driver"
            url.contains("jdbc:h2:") -> "org.h2.Driver"
            else -> throw IllegalArgumentException("This jdbc driver is not supported.")
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Form

        if (url != other.url) return false
        if (user != other.user) return false
        if (!password.contentEquals(other.password)) return false
        if (schema != other.schema) return false
        if (driverPath != other.driverPath) return false
        if (outputDirectory != other.outputDirectory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + password.contentHashCode()
        result = 31 * result + schema.hashCode()
        result = 31 * result + driverPath.hashCode()
        result = 31 * result + outputDirectory.hashCode()
        return result
    }
}
