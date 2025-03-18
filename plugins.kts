

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import liveplugin.PluginUtil
import liveplugin.PluginUtil.registerAction

class InsertGetLangAction(private val withEcho: Boolean) : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.project ?: return
        val editor: Editor = PluginUtil.currentEditorIn(project) ?: return
        val document: Document = editor.document

        val selectionModel = editor.selectionModel
        val caretModel = editor.caretModel
        val selectedText = selectionModel.selectedText ?: ""

        val textToInsert = if (selectedText.isNotEmpty()) {
            if (withEcho) "<?=getLang('$selectedText');?>" else "getLang('$selectedText');"
        } else {
            if (withEcho) "<?=getLang('');?>" else "getLang('');"
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val insertOffset = if (selectedText.isNotEmpty()) {
                selectionModel.selectionStart
            } else {
                caretModel.offset
            }

            if (selectedText.isNotEmpty()) {
                document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, textToInsert)
            } else {
                document.insertString(insertOffset, textToInsert)
            }

            // İmleci tırnakların içine veya seçili metnin son tırnağından önce yerleştir
            val cursorOffset = insertOffset + textToInsert.indexOfLast { it == '\'' }
            caretModel.moveToOffset(cursorOffset)
        }
    }
}

// ALT + 1 → <?=getLang('');?>  (Seçili metni tırnak içine alır ve imleç tırnak içine gelir)
registerAction("Insert GetLang With Echo", "alt 1", InsertGetLangAction(true))

// ALT + 2 → getLang('');  (Seçili metni tırnak içine alır ve imleç tırnak içine gelir)
registerAction("Insert GetLang", "alt 2", InsertGetLangAction(false))
