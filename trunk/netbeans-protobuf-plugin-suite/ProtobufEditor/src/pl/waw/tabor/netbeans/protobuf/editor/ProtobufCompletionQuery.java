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
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.util.Exceptions;

/**
 * The class prepares completion items for *.proto (Google protobuf files)
 *
 * @author <a href="piotr.tabor@gmail.com" (<a href="http://piotr.tabor.waw.pl">http://piotr.tabor.waw.pl</a>)
 */
public class ProtobufCompletionQuery extends AsyncCompletionQuery {

    /**
     * In this method we have to fill completionResultSet with completionItems.
     * The caret is at the caretOffset in given document.
     *
     * @param completionResultSet
     * @param document
     * @param caretOffset
     */
    @Override
    protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {
        /*Just typed token (prefix of completion item the user expects)*/
        String filter = null;
        /*Offset of the start of the "filter" token*/
        int startOffset = caretOffset - 1;

        try {
            final StyledDocument bDoc = (StyledDocument) document;
            /*Finding the first nonwhite (spaces,tabs) character in the current line*/
            final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
            /*Get business content of the line (without beginning chars) up to the caretPositon*/
            final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
            /*We are looking for last char in the line content that is not white character*/
            final int whiteOffset = indexOfLastWhite(line);
            /*Filter is suffix of the line (from last white char to the end of the line)*/
            filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
            if (whiteOffset > 0) {
                startOffset = lineStartOffset + whiteOffset + 1;
            } else {
                startOffset = lineStartOffset;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        /*Get syntax tree - created by Schlieman*/
        final ASTNode root = getCurrentRootNode(document);
        
        /*What's the logical type of the item that caret is currently nested in. */
        final ParserContext context = getCurrentContext(root, caretOffset);

        /*In this list we will put texts of code completion items*/
        List<String> results=new LinkedList<String>();
        switch (context) {
            case FIELD_TYPE:
                /*Searching whole ASTTree and adding all enum names and message names*/
                /*TODO: We should avoid adding here enum's end messages nested
                in the other context*/
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

        /* For every text in results structure that starts with given
         * prefix (filter), we build completion item.
         */
        for(String s:results)
        {
            if(s.startsWith(filter))
            {
                completionResultSet.addItem(new ProtobufCompletionItem(s, caretOffset, startOffset));
            }
        }
        completionResultSet.finish();
    }

    /**
     * Finds last occurence of whitespace char in the given line. R
     *
     * @param line
     * @return the offset of the whitespace or -1 if not found. 
     */
    static int indexOfLastWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Goes recursivly throught the node (syntax Tree) and finds all occurences
     * of messages and enums. Add those to the resultset.
     *
     * @param resultSet - list, where we will add all found items
     * @param node - subtree that we search through
     */
    private void addNodeToCompletionResultset(List<String> resultSet, ASTItem node) {
        if (node instanceof ASTNode) {
            if (("MessageName".equals(((ASTNode) node).getNT()))) {
                resultSet.add(((ASTNode) node).getAsText() + " [Message]");
            }
            if (("EnumName".equals(((ASTNode) node).getNT()))) {
                resultSet.add(((ASTNode) node).getAsText() + " [Enum]");
            }
        }
        /*For all nested syntax elements call the method recursivly*/
        for (ASTItem n : node.getChildren()) {
            addNodeToCompletionResultset(resultSet, n);
        }
    }

    /**
     * Retrives ASTNode (syntax tree) from the Schliemman parser.
     *
     * @param document
     * @return
     */
    private ASTNode getCurrentRootNode(Document document) {
//        Node[] ns = TopComponent.getRegistry().getActivatedNodes();
//        if (ns.length != 1) {
//            return null;
//        }
//        if (document == null || !(document instanceof NbEditorDocument)) {
//            return null;
//        }
        try {
            return ParserManager.get(document).getAST();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    };

    /**
     * CompletionTypes - for different context (current caret positions)
     * we will prepare different list of proposition
     */
    static private enum ParserContext {
        ROOT,
        CLEAN_MESSAGE_BODY,
        UNKNOWN,
        FIELD_TYPE
    }

    /**
     * In this method we finds first "important" syntax struct that the
     * caret is currently nested in.
     *
     * <p>In this method we have to avoid problems connected to the fact
     * that whitespace (chars that are SKIP marked in Schliemman) fallows (are
     * suffix of) other syntax nodes. So if we are after some node, but there
     * are only whitespaces between caret and the node - we can get information
     * from the parser {@link ASTNode#findPath(int) } that we nested in the node
     * (it is not true - so we have to correct that situation). 
     *
     * @param root - syntax tree of the document
     * @param caretOffset - current position in document
     * @return
     */
    private ParserContext getCurrentContext(ASTNode root, int caretOffset) {
        try{
            /*Get whole stack of items we are nested in*/
            ASTPath path = root.findPath(caretOffset);
            if (path != null) {
                /*From the most nested item to the whole syntax*/
                for (int i = path.size() - 1; i >= 0; i--) {
                    ASTItem item = path.get(i);
                    if (item instanceof ASTNode) {
                        ASTNode node = (ASTNode) item;

                        /**Checks if the last token of the construction is after
                         cursor position - to avoid situation that the node is
                         already finished, but there are whitespaces after it.*/
                        if (isOpenNode(node, caretOffset)) {
                            if ("MessageBody".equals(node.getNT())) {
                                return ParserContext.CLEAN_MESSAGE_BODY;
                            }
                            if ("Message".equals(node.getNT())) {
                                return ParserContext.CLEAN_MESSAGE_BODY;
                            }
                            if ("Proto".equals(node.getNT())) {
                                return ParserContext.ROOT;
                            }
                            if ("S".equals(node.getNT())) {
                                return ParserContext.ROOT;
                            }
                            if ("GroupOrField".equals(node.getNT())) {
                                return ParserContext.FIELD_TYPE;
                            }
                        } else {
                            if ("FieldCont".equals(node.getNT())) {
                                i--;//Skip GroupOrField
                            }
                            if ("MessageBody".equals(node.getNT())) {
                                i--; //Skip Message
                            }
                        }

                    }
                }
            }/*No path no cry*/
        }catch(Exception e)
        {
            Exceptions.printStackTrace(e);
            return ParserContext.UNKNOWN;
        }
        return ParserContext.UNKNOWN;
    }



    /**
     * The node is open if the "closing" child (token) not exsists or
     * is after current caret position.
     *
     * @param node
     * @param caretOffset
     * @return
     */
    private boolean isOpenNode(ASTNode node, int caretOffset) {
        ASTItem child = findClosingChild(node);
        if (child != null) {
            boolean isOpen = child.getEndOffset() > caretOffset;
            return isOpen;
        } else {
            return true;
        }
    }

    /**
     * Searches for last (directly nested) token in the node that is
     * "syntax item closing token" (semicolon, or close_block brace }).
     *
     * @return the token - or NULL if not found. 
     */
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

    /**
     * Finds the line connected to the offset in the documents
     * and finds offset (inside the line) of the first char that
     * isn't whitespace.
     */
    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (Character.isWhitespace(doc.getText(start, 1).charAt(0))) {
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

}