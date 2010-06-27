/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.waw.tabor.netbeans.protobuf.generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * This class formats output from the external tool (protoc compiler) that
 * shows in the "Output" window. It parses each line and if the line contains
 * filename, line and column it provided hiperlink to that position.
 *
 * @author <a href="piotr.tabor@gmail.com" (<a href="http://piotr.tabor.waw.pl">http://piotr.tabor.waw.pl</a>)
 */
class ProtobufOutputListener implements OutputListener{
    /**
     * Parsers dependening on protoc output formatt
     */
    private static final String PATTERN_STRING = "(.*):([0-9]*):([0-9]*):(.*)";
    public static final Pattern PATTERN=Pattern.compile(PATTERN_STRING);

    /**files the protoc was runned on */
    private Node[] nodes;

    public ProtobufOutputListener(Node[] activatedNodes) {
        this.nodes=activatedNodes;
    }

    /**
     * User selected (clicked) that line. We have to open
     * that file and move cursor to given position.
     *
     * @param event
     */
    public void outputLineAction(OutputEvent event) {
        String lineString = event.getLine();
        Matcher matcher = PATTERN.matcher(lineString);
        if(matcher.matches()){
            String file  =matcher.group(1);
            int lineNumber=Integer.parseInt(matcher.group(2));
            int colNumber=Integer.parseInt(matcher.group(3));
            DataObject dataObject=ProtobufHelper.findDataObjectForFile(file,nodes);
            if (dataObject!=null)
            {
                LineCookie lc = dataObject.getCookie(LineCookie.class);
                Line l = lc.getLineSet().getOriginal(lineNumber-1);
                l.show(Line.ShowOpenType.REUSE_NEW,Line.ShowVisibilityType.FOCUS,colNumber-1);
            }
        }
    }

    public void outputLineCleared(OutputEvent arg0) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void outputLineSelected(OutputEvent arg0) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

}
