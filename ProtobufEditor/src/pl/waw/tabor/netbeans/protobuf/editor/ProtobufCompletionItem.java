/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 *
 * @author <a href="piotr.tabor@gmail.com" (<a href="http://piotr.tabor.waw.pl">http://piotr.tabor.waw.pl</a>)
 */
class ProtobufCompletionItem implements CompletionItem {

    /*Text of the hint*/
    private String text;

    //TODO: Provide more icons
    private static ImageIcon fieldIcon =
            new ImageIcon(ImageUtilities.loadImage("/org/netbeans/modules/languages/resources/variable.gif"));
    private static Color fieldColor = Color.decode("0x0000B2");
    
    /**
     * Current position of the cursor (caret) as offset from beginning of the resource (file).
     * <p>We need it to know which text to replace if user selects the completion item</p>
     */
    private int caretOffset;

    /**
     * Position of start of the current token (as offset from beginning of the resource (file))
     * <p>We need it to know which text to replace if user selects the completion item</p>
     */
    private int dotOffset;


    /**
     * * <p>We need it to know which text to replace if user selects the completion item</p>
     *
     * @param  text - text of the completion item
     * @param  caretOffset - offset of the end of the already typed text (prefix of completion item)
     * @param  dotOffset - offset of the start of  the already typed text (prefix of completion item)
     */
    public ProtobufCompletionItem(String text, int caretOffset,int dotOffset) {
        this.text = text;
        this.caretOffset = caretOffset;
        this.dotOffset=dotOffset;
    }

    /**
     * User want to apply the completion item. We replace the current text (from
     * dotOffset to caretOffset) with the selected text.
     *
     * @param jTextComponent
     */
    public void defaultAction(JTextComponent jTextComponent) {
        try {
            StyledDocument doc = (StyledDocument) jTextComponent.getDocument();
            doc.remove(dotOffset, caretOffset-dotOffset);
            /*The [Message] and [Enum] suffix are only information during selection of items from the list*/
            doc.insertString(dotOffset, text.replaceAll(" [Message]", "").replaceAll(" [Enum]",""), null);
            Completion.get().hideAll();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(text, null, g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(fieldIcon, text, null, g, defaultFont,
                (selected ? Color.white : fieldColor), width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    /**
     * We want to show items of type [Message] first and the
     * [Enum]s. After that we will show other proposition (for example
     * atomic types).
     * @return
     */
    public int getSortPriority() {
        if (text.contains("[Message]"))
            return 0;
        if (text.contains("[Enum]"))
            return 1;
        return 2;
    }

    /**
     * Inside prorities {@link #getSortPriority() } we sort lexically
     * @return
     */
    public CharSequence getSortText() {
        return text;
    }

    public CharSequence getInsertPrefix() {
        return text;
    }
}
