package schema2json

import com.areab.schema2json.SchemaToJsonExecutor
import com.areab.schema2json.Settings
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class ExportTask(private val form: Form) : Runnable {

    override fun run() {
        val settings = Settings()
        settings.setOutputDirectory(form.outputDirectory)

        settings.driverClassName = form.driverClassName()
        settings.url = form.url
        settings.schema = form.url
        settings.username = form.user
        settings.password = String(form.password)
        SchemaToJsonExecutor().execute(settings)

        if (form.postHookCommand.isNotEmpty()) {

            fun String.runCommand(workingDir: File) {
                ProcessBuilder(*split(" ").toTypedArray())
                    .directory(workingDir)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()
                    .waitFor(90, TimeUnit.SECONDS)
            }

            val tempDir = Files.createTempDirectory("schema2json_temp_")
            form.postHookCommand.runCommand(tempDir.toFile())
        }

        throw InterruptedException()
    }
}