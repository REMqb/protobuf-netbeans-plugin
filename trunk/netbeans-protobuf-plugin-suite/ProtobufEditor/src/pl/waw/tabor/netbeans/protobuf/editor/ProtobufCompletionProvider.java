/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.editor;

import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author ptab
 */
public class ProtobufCompletionProvider implements CompletionProvider {

    public CompletionTask createTask(int queryType, JTextComponent jTextComponent) {
        return new AsyncCompletionTask(new ProtobufCompletionQuery(), jTextComponent);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
}
