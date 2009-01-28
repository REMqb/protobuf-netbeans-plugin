/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SaveCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author ptab
 */
public class ProtobufGeneratorRunnable implements Runnable {
    public final static String PROTOC_PATH_KEY="PROTOC_PATH_KEY";
    private Node[] nodes;
    private String additionalArgs;

    public ProtobufGeneratorRunnable(Node[] activatedNodes, String args) {
        nodes = activatedNodes;
        additionalArgs = args;
    }

    public void run() {
        final String protocPath=getProtocPath();
        final InputOutput io = IOProvider .getDefault().getIO(NbBundle.
            getMessage(getClass(), "ProtocResult"), false);
            io.select(); // Tree tab is selected
         final OutputWriter writer = io.getOut();

         try{
        if (new File(protocPath).exists()) {
            try {
                for (int i = 0; i < nodes.length; i++) {
                    DataObject dataObject = (DataObject) nodes[i].getCookie(DataObject.class);
                    processDataObject(writer,protocPath,"--proto_path=\"/\" "+additionalArgs, dataObject);
                }
            } catch (InterruptedException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            writer.println("Executable not found at " + protocPath + ".");
        }
         }catch(Exception ex)
         {
             ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);

         }finally{
             writer.close();
         }
    }

    void processDataObject(final OutputWriter writer,final String protocExecutable,final String protocParams,final DataObject dataObject) throws InterruptedException,IOException{
        forceSave(dataObject);
        FileObject fileObject = dataObject.getPrimaryFile();
        File file = FileUtil.toFile(fileObject);

        final FileObject java_out=getJavaSourceDirForNode(dataObject, writer);
        if (java_out!=null)
        {
            String args=protocParams +" --java_out \""+FileUtil.toFile(java_out).toString()+"\" \"" + file.getAbsolutePath() +"\"";
            writer.println("running: "+protocExecutable+" "+args);
            NbProcessDescriptor protocProcessDesc =
                new NbProcessDescriptor( protocExecutable, args);
            Process process = protocProcessDesc.exec();
            ProtobufAnnotation.removeAllAnnotationsForFile(file.getAbsolutePath());
            readOutput(writer, process.getErrorStream(), nodes,file.getAbsolutePath());
            process.waitFor();
            writer.println("Exit: "+process.exitValue());
            if(process.exitValue()==0)
            {
                
            }
            java_out.refresh();
        }else{
            writer.println("No Java sorce directory (destination directory)");
        }
        writer.flush();
    }

    public String getProtocPath() {
//        return "/usr/bin/protoc";
    //   get(ProtobufAction.PROTOC_PATH_KEY, defaultValue);
        String defaultValue = NbBundle.getMessage(
                ProtobufAction.class,
                "ProtocolBuffersPanel_ProtocPathDefault");
        return Preferences.userNodeForPackage(ProtobufGeneratorRunnable.class).get(ProtobufGeneratorRunnable.PROTOC_PATH_KEY, defaultValue);
    }

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

    static void readOutput(OutputWriter writer, InputStream errStream, Node[] nodes,String fileName) {
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
                            matcher.group(4),fileName);
                    writer.println(errString, listener);
                } else {
                    writer.println(errString);
                }
            }
        } catch (Exception e) {
            writer.println("Could not read process output " + e);
        }
    }

    private FileObject getJavaSourceDirForNode(DataObject dataObject, OutputWriter writer) {

        Project p = getProject(dataObject.getPrimaryFile());
        FileObject res = null;
        if (p != null) {
            Sources sources = ProjectUtils.getSources(p);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

            for (SourceGroup g : groups) {
                //writer.println("Source group name: " + g.getName());
                if (!g.getName().equals("${test.src.dir}")) {
                    res = g.getRootFolder();
                }
            }

        }
        return res;
    }

    private Project getProject(FileObject file) {
        Project p = FileOwnerQuery.getOwner(file);
        return p;
    }
}
