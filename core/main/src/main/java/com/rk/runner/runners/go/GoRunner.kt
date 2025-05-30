package com.rk.runner.runners.go

import android.content.Context
import android.graphics.drawable.Drawable
import com.rk.launchInternalTerminal
import com.rk.libcommons.TerminalCommand
import com.rk.libcommons.child
import com.rk.libcommons.localBinDir
import com.rk.runBashScript
import com.rk.runner.RunnerImpl
import com.rk.settings.Settings
import java.io.File

class GoRunner(val file: File, val isTermuxFile: Boolean = false) : RunnerImpl() {

    override fun run(context: Context) {
        val node = localBinDir().child("go")
        if (node.exists().not()) {
            node.writeText(context.assets.open("terminal/go.sh").bufferedReader()
                .use { it.readText() })
        }
        val runtime = if (isTermuxFile) {
            "Termux"
        } else {
            Settings.terminal_runtime
        }
        when (runtime) {
            "Alpine" -> {
                launchInternalTerminal(
                    context = context, TerminalCommand(
                        shell = "/bin/sh",
                        args = arrayOf(node.absolutePath, file.absolutePath),
                        id = "go",
                        workingDir = file.parentFile!!.absolutePath
                    )
                )
            }

            "Termux" -> {
                runBashScript(
                    context, script = """
                        required_packages="go"
                        missing_packages=""

                        # Check for missing packages
                        for pkg in ${'$'}required_packages; do
                            if ! dpkg -l | grep -q "^ii  ${'$'}pkg"; then
                                missing_packages="${'$'}missing_packages ${'$'}pkg"
                            fi
                        done

                        # Install missing packages if any
                        if [ -n "${'$'}missing_packages" ]; then
                            echo -e "\e[34;1m[*]\e[37m Installing missing packages: ${'$'}missing_packages\e[0m"
                            pkg install -y ${'$'}missing_packages
                        fi

                        go run "${file.absolutePath}"
                        echo -e "\n\nProcess completed. Press Enter to go back to Xed-Editor."
                        read
                """.trimIndent(), workingDir = file.parentFile!!.absolutePath
                )
            }
        }
    }

    override fun getName(): String {
        return "Go"
    }

    override fun getDescription(): String {
        return "Go"
    }

    override fun getIcon(context: Context): Drawable? {
        return null
    }

    override fun isRunning(): Boolean {
        return false
    }

    override fun stop() {

    }
}