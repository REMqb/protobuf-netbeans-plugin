/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author ptab
 */
public class ProtobufCompletionQuery extends AsyncCompletionQuery {

    private void addNodeToCompletionResultset(List<String> resultSet, ASTItem node) {
        if (node instanceof ASTNode) {
            if (("MessageName".equals(((ASTNode) node).getNT()))) {
                resultSet.add(((ASTNode) node).getAsText() + " [Message]");
            }
            if (("EnumName".equals(((ASTNode) node).getNT()))) {
                resultSet.add(((ASTNode) node).getAsText() + " [Enum]");
            }
        }
        for (ASTItem n : node.getChildren()) {
            addNodeToCompletionResultset(resultSet, n);
        }
    }

    private ASTNode getCurrentRootNode(Document document) {
        Node[] ns = TopComponent.getRegistry().getActivatedNodes();
        if (ns.length != 1) {
            return null;
        }
        if (document == null || !(document instanceof NbEditorDocument)) {
            return null;
        }
        try {
            return ParserManager.get(document).getAST();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    static public enum ParserContexts {

        ROOT,
        CLEAN_MESSAGE_BODY,
        UNKNOWN,
        FIELD_TYPE
    }

    protected ParserContexts getCurrentContext(ASTNode root, int caretOffset) {
        try{
        ASTPath path = root.findPath(caretOffset);
        if (path != null) {
            for (int i = path.size() - 1; i >= 0; i--) {
                ASTItem item = path.get(i);
//                String desc = String.format("%3d", i/*,item.toString()*/);
                if (item instanceof ASTNode) {
                    ASTNode node = (ASTNode) item;
//                    desc += "(" + node.getAsText() + ") [" + node.getNT() + "]";

                    if (isOpenNode(node, caretOffset)) {
//                         desc="[OPEN]"+desc;
//                         System.out.println(desc+" "+item.toString());

                        if ("MessageBody".equals(node.getNT())) {
                            return ParserContexts.CLEAN_MESSAGE_BODY;
                        }
                        if ("Message".equals(node.getNT())) {
                            return ParserContexts.CLEAN_MESSAGE_BODY;
                        }
                        if ("Proto".equals(node.getNT())) {
                            return ParserContexts.ROOT;
                        }
                        if ("S".equals(node.getNT())) {
                            return ParserContexts.ROOT;
                        }
                        if ("GroupOrField".equals(node.getNT())) {
                            return ParserContexts.FIELD_TYPE;
                        }
                    } else {
//                         System.out.println("[CLOSED]"+desc+" "+item.toString());
                        if ("FieldCont".equals(node.getNT())) {
                            i--;//Skip GroupOrField
                        }
                        if ("MessageBody".equals(node.getNT())) {
                            i--; //Skip Message
                        }
                    }

                }
            }
        }
        }catch(Exception e)
        {
            Exceptions.printStackTrace(e);
            return ParserContexts.UNKNOWN;
        }
        return ParserContexts.UNKNOWN;
    }

    @Override
    protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {
        String filter = null;
        int startOffset = caretOffset - 1;
        try {
            final StyledDocument bDoc = (StyledDocument) document;
            final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
            final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
            final int whiteOffset = indexOfWhite(line);
            filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
            if (whiteOffset > 0) {
                startOffset = lineStartOffset + whiteOffset + 1;
            } else {
                startOffset = lineStartOffset;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }


        ASTNode root = getCurrentRootNode(document);
        ParserContexts context = getCurrentContext(root, caretOffset);
        List<String> results=new LinkedList<String>();
        switch (context) {
            case FIELD_TYPE:
                addNodeToCompletionResultset(results, root);
                results.addAll(Arrays.asList(new String[]{"string","double",
                    "float","int32","int64","uint32","uint64","string",
                    "sint32","sint64","fixed32","fixed64","sfixed32","sfixed64",
                    "bool","string","bytes"}));
                break;
            case ROOT:
                 results.addAll(Arrays.asList(new String[]{"package","option",
                    "message","enum","service"}));

                break;
            case CLEAN_MESSAGE_BODY:
                results.addAll(Arrays.asList(new String[]{"option",
                    "message","enum","required","optional","repeated"}));
                break;
        }

        for(String s:results)
        {
            if(s.startsWith(filter))
            {
                completionResultSet.addItem(new ProtobufCompletionItem(s, caretOffset, startOffset));
            }
        }
        completionResultSet.finish();
    }

    private boolean isOpenNode(ASTNode node, int caretOffset) {
        ASTItem child = findClosingChild(node);
        if (child != null) {
            boolean isOpen = child.getEndOffset() > caretOffset;
//            System.out.println("Closing end: "+child.getEndOffset()+" caret:"+caretOffset+" isOpen:"+isOpen);
            return isOpen;
        } else {
            return true;
        }
    }

    private ASTItem findClosingChild(ASTNode node) {
        List<ASTItem> items = new LinkedList<ASTItem>(node.getChildren());
        Collections.reverse(items);
        for (ASTItem item : items) {
            if (item instanceof ASTToken) {
                ASTToken token = (ASTToken) item;
                if ("close_block".equals(token.getTypeName()) ||
                        "semicolon".equals(token.getTypeName())) {
                    return token;
                }
            }
        }
        return null;
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1) +
                        ") on doc of length: " + doc.getLength(), start).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }
}