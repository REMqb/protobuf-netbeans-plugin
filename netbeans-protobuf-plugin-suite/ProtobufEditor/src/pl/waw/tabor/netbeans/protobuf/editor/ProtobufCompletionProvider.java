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
 * Links to {@link ProtobufCompletionQuery}, that prepares completion items
 * for *.proto files.
 *
 * @author <a href="piotr.tabor@gmail.com" (<a href="http://piotr.tabor.waw.pl">http://piotr.tabor.waw.pl</a>)
 */
public class ProtobufCompletionProvider implements CompletionProvider {

    /**
     * Create task (procedure
     *
     * @param queryType - 
     * @param jTextComponent
     * @return
     */
    public CompletionTask createTask(int queryType, JTextComponent jTextComponent) {
        return new AsyncCompletionTask(new ProtobufCompletionQuery(), jTextComponent);
    }

    /**
     * Determines whether text typed in a document should automatically pop up
     * the code completion window
     *
     * @returns Any combination of the COMPLETION_QUERY_TYPE, 
     * COMPLETION_ALL_QUERY_TYPE, DOCUMENTATION_QUERY_TYPE,
     * and TOOLTIP_QUERY_TYPE values, or zero if no query
     * should be automatically invoked.
     *
     */
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        /*We don't want completion window to appear automaticaly (without
         keyboard shortcut)*/
        return 0;
    }
}
