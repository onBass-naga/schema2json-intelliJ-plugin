package schema2json

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import java.awt.Font
import java.io.File
import java.lang.IllegalArgumentException
import java.net.URLClassLoader
import javax.swing.*
import javax.swing.JTextArea
import javax.swing.JCheckBox


class ExtensionToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        val state = Schema2JsonState.getInstance()

        val parent = toolWindow.component
        val panel = JPanel()

        // GroupLayout
        val layout = GroupLayout(panel)
        panel.layout = layout
        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

        val urlLabel = JLabel("URL")
        panel.add(urlLabel)
        val userLabel = JLabel("User")
        panel.add(userLabel)
        val passwordLabel = JLabel("Password")
        panel.add(passwordLabel)

        val schemaLabel = JLabel("Schema")
        panel.add(schemaLabel)
        val driverLabel = JLabel("Driver path")
        panel.add(driverLabel)
        val outputDirLabel = JLabel("Output directory")
        panel.add(outputDirLabel)
        val postHookCommandLabel = JLabel("Post hook command")
        panel.add(postHookCommandLabel)

        val urlField = JTextField(10)
        panel.add(urlField)
        urlField.text = state.url
        val userField = JTextField(10)
        panel.add(userField)
        userField.text = state.user
        val passwordField = JPasswordField(10)
        panel.add(passwordField)
        passwordField.text = retrieveCredential(state)
        val schemaField = JTextField(10)
        val rememberPasswordCheckbox = JCheckBox("Remember password")
        rememberPasswordCheckbox.isSelected = state.rememberPassword
        rememberPasswordCheckbox.font = Font(
            rememberPasswordCheckbox.font.fontName,
            rememberPasswordCheckbox.font.style,
            10
        )
        panel.add(schemaField)
        schemaField.text = state.schema
        val driverField = JTextField(10)
        panel.add(driverField)
        driverField.text = state.driverPath
        val outputField = JTextField(10)
        panel.add(outputField)
        outputField.text = state.outputDirectory
        val postHookCommandField = JTextField(10)
        panel.add(postHookCommandField)
        postHookCommandField.text = state.postHookCommand

        val exportButton = JButton("Export")
        panel.add(exportButton)
        val messageLabel = JLabel("")
        panel.add(messageLabel)
        val messageArea = JTextArea(5, 10)
        messageArea.lineWrap = true
        messageArea.isVisible = false

        val hGroup = layout.createSequentialGroup()

        hGroup.addGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(urlLabel)
                .addComponent(userLabel)
                .addComponent(passwordLabel)
                .addComponent(schemaLabel)
                .addComponent(driverLabel)
                .addComponent(outputDirLabel)
                .addComponent(postHookCommandLabel)
                .addComponent(exportButton)
        )

        hGroup.addGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(urlField)
                .addComponent(userField)
                .addComponent(passwordField)
                .addComponent(rememberPasswordCheckbox)
                .addComponent(schemaField)
                .addComponent(driverField)
                .addComponent(outputField)
                .addComponent(postHookCommandField)
                .addComponent(messageLabel)
                .addComponent(messageArea)
        )

        layout.setHorizontalGroup(hGroup)

        val vGroup = layout.createSequentialGroup()

        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(urlLabel)
                .addComponent(urlField)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(userLabel)
                .addComponent(userField)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(passwordLabel)
                .addComponent(passwordField)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(rememberPasswordCheckbox)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(schemaLabel)
                .addComponent(schemaField)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(driverLabel)
                .addComponent(driverField)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(outputDirLabel)
                .addComponent(outputField)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(postHookCommandLabel)
                .addComponent(postHookCommandField)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(exportButton)
                .addComponent(messageLabel)
        )
        vGroup.addGroup(
            layout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(messageArea)
        )
        layout.setVerticalGroup(vGroup)

        parent.add(panel)


        // action ----------------------------------------------------------
        fun onSuccess(): () -> Unit = {
            messageLabel.text = """<html><span style="color: green";>Successful</span>"""
            exportButton.isEnabled = true
        }

        fun onFailure(): (Throwable) -> Unit = { e ->
            System.err.println(e.message)
            messageLabel.text = """<html><span style="color: red;">Error<span></html>"""
            messageArea.text = e.message
            messageArea.isVisible = true
            exportButton.isEnabled = true
        }

        exportButton.addActionListener {

            exportButton.isEnabled = false
            messageArea.isVisible = false

            val form = Form(
                urlField.text.trim(),
                userField.text.trim(),
                passwordField.password,
                schemaField.text.trim(),
                driverField.text.trim(),
                outputField.text.trim(),
                postHookCommandField.text.trim(),
                rememberPasswordCheckbox.isSelected
            )

            val errors = validate(form)

            if (errors.isNotEmpty()) {
                onFailure()(IllegalArgumentException(errors.joinToString("\n")))
            } else {
                save(form)
                export(form, onSuccess(), onFailure())
            }
        }
    }

    private fun validate(form: Form): List<String> {

        val errors = mutableListOf<String>()

        if (form.hasRequiredError()) {
            errors.add("[URL/User/Driver path/Output directory] are required.")
        }

        if (form.driverPath.isNotEmpty() && !File(form.driverPath).exists()) {
            errors.add("'Driver path' does not exist. [path: ${form.driverPath}]")
        }

        if (form.outputDirectory.isNotEmpty() && !File(form.outputDirectory).exists()) {
            errors.add("'Output directory' does not exist. [path: ${form.outputDirectory}]")
        }

        return errors
    }

    private fun createCredentialAttributes(key: String): CredentialAttributes {
        return CredentialAttributes(key)
    }

    private fun retrieveCredential(state: Schema2JsonState): String {
        println(state)
        if (!state.rememberPassword) {
            return ""
        }

        val key = "schema2json"
        val credentialAttributes = createCredentialAttributes(key)
        return PasswordSafe.instance.getPassword(credentialAttributes).orEmpty()
    }

    private fun save(form: Form) {

        if (form.rememberPassword) {
            val credentialKey = createCredentialAttributes("schema2json")
            val credentials = Credentials(form.user, String(form.password))
            PasswordSafe.instance.set(credentialKey, credentials)
        }

        val state = Schema2JsonState.getInstance()
        state.url = form.url
        state.user = form.user
        state.schema = form.schema
        state.driverPath = form.driverPath
        state.outputDirectory = form.outputDirectory
        state.postHookCommand = form.postHookCommand
        state.rememberPassword = form.rememberPassword
    }

    private fun export(form: Form, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {

        val file = File(form.driverPath)
        val currentThreadClassLoader = Thread.currentThread().contextClassLoader

        val urlClassLoader = URLClassLoader(
            arrayOf(file.toURI().toURL()),
            currentThreadClassLoader
        )

        val task = ExportTask(form)
        val thread = Thread(task)
        thread.contextClassLoader = urlClassLoader
        thread.start()

        thread.setUncaughtExceptionHandler { _, e ->
            when (e) {
                is InterruptedException -> onSuccess()
                else -> onFailure(e)
            }
        }
    }

}
