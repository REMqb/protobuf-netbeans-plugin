/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.OutputWriter;

/**
 *
 * @author ptab
 */
class ProtobufGeneratorRunnable implements Runnable{

    public ProtobufGeneratorRunnable(Node[] activatedNodes, String string) {
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//                    get(ProtobufGenerateAction.PROTOC_PATH_KEY, defaultValue);
//    }

    /** Save the DataObject if it has been modified */
    void forceSave(DataObject dataObject) throws IOException {
        if (dataObject.isModified()) {
            SaveCookie cookie =
                    (SaveCookie) dataObject.getCookie(SaveCookie.class);
            if (cookie != null) {
                cookie.save();
            }
        }
    }

    static void readOutput(OutputWriter writer, InputStream errStream, Node[] nodes) {
        ProtobufOutputListener listener = new ProtobufOutputListener(nodes);
        try {
            BufferedReader error = new BufferedReader(new InputStreamReader(errStream));
            String errString = null;
            while ((errString = error.readLine()) != null) {
                Matcher matcher = ProtobufOutputListener.PATTERN.matcher(errString);
                if (matcher.matches()) {
                    LineCookie lc = (LineCookie) listener.findDataObjectForFile(matcher.group(1)).getCookie(LineCookie.class);
                    ProtobufAnnotation.createAnnotation(lc,
                            Integer.parseInt(matcher.group(2)) - 1,
                            Integer.parseInt(matcher.group(3)),
                            "Error",
                            matcher.group(4));
                    writer.println(errString, listener);
                } else {
                    writer.println(errString);
                }
            }
        } catch (Exception e) {
            writer.println("Could not read process output " + e);
        }
    }

    private FileObject getJavaSourceDirForNode(Node node,OutputWriter writer) {
        DataObject dataObject = (DataObject) node.getCookie(DataObject.class);

        Project p=getProject(dataObject.getPrimaryFile());
        FileObject res=null;
        if(p!=null)
        {
            Sources sources = ProjectUtils.getSources(p);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

            for(SourceGroup g:groups)
            {
                writer.println("Source group name: "+g.getName());
                if (!g.getName().equals("${test.src.dir}"))
                    res=g.getRootFolder();
            }

        }
        return res;
    }

    private Project getProject(FileObject file){
        Project p = FileOwnerQuery.getOwner(file);
        return p;
    }
}
