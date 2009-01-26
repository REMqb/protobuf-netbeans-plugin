/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author ptab
 */
class ProtobufOutputListener implements OutputListener{
    private static final String PATTERN_STRING = "(.*):([0-9]*):([0-9]*):(.*)";
    public static final Pattern PATTERN=Pattern.compile(PATTERN_STRING);
    private Node[] nodes;

    public ProtobufOutputListener(Node[] activatedNodes) {
        this.nodes=activatedNodes;
    }

    public void outputLineAction(OutputEvent event) {
        String lineString = event.getLine();
        Matcher matcher = PATTERN.matcher(lineString);
        if(matcher.matches()){
            String file  =matcher.group(1);
            int lineNumber=Integer.parseInt(matcher.group(2));
            int colNumber=Integer.parseInt(matcher.group(3));
            DataObject dataObject=findDataObjectForFile(file);
            if (dataObject!=null)
            {
                LineCookie lc= (LineCookie) dataObject.getCookie(LineCookie.class);
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

    public DataObject findDataObjectForFile(String fileName) {
        for(Node node:nodes)
        {
            DataObject d=(DataObject)node.getCookie(DataObject.class);
            FileObject fileObject=d.getPrimaryFile();
            File file = FileUtil.toFile(fileObject);
            if (file.toString().endsWith(fileName))
                return d;
        }
        return null;
    }
}
