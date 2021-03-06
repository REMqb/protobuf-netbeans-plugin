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
import org.openide.util.NbPreferences;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * This class takes care on realization of "Regenerate code from selected
 * protoc definition(s)". It runs protoc compiler on selected files and parses
 * the output. 
 *
 * @author <a href="piotr.tabor@gmail.com" (<a href="http://piotr.tabor.waw.pl">http://piotr.tabor.waw.pl</a>)
 */
public class ProtobufGeneratorRunnable implements Runnable {
    public final static String PROTOC_PATH_KEY="protobuf.executable";
    private Node[] nodes;
    private String additionalArgs;

    /**
     *
     * @param activatedNodes - selected (by user) nodes (files) in the navigator
     * @param args - additional arguments to forward to protoc compiler.
     */
    public ProtobufGeneratorRunnable(Node[] activatedNodes, String args) {
        nodes = activatedNodes;
        additionalArgs = args;
    }

    /**
     * Runs protoc compiler on all selected nodes. 
     */
    public void run() {
        final String protocPath=getProtocPath();
        final InputOutput io = IOProvider .getDefault().getIO(NbBundle.
            getMessage(getClass(), "ProtocResult"), false);
            io.select(); // Tree tab is selected
         final OutputWriter writer = io.getOut();

         try{
        if (new File(protocPath).exists()) {
            try {
                /*Iterate over selected nodes*/
                for (int i = 0; i < nodes.length; i++) {
                    DataObject dataObject = nodes[i].getCookie(DataObject.class);
                    processDataObject(writer,protocPath,additionalArgs, dataObject);
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

    /**
     * Runs protoc compiler on single *.proto file.
     *
     * @param writer
     * @param protocExecutable
     * @param protocParams
     * @param dataObject
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    void processDataObject(final OutputWriter writer,final String protocExecutable,final String protocParams,final DataObject dataObject) throws InterruptedException,IOException{
        /*Ensure the file is saved. Save if not*/
        forceSave(dataObject);
        FileObject fileObject = dataObject.getPrimaryFile();
        File file = FileUtil.toFile(fileObject);


        final FileObject java_out=getJavaDestinationDirectory(dataObject,writer);                

        if (java_out!=null)
        { /*We found destination directory*/

            StringBuilder args=new StringBuilder(protocParams +"--proto_path=\""+getProjectPath(fileObject)+"\" ");
            Project p=getProject(dataObject.getPrimaryFile());
            if (isPythonProject(p)){
              args.append(" --python_out \""+FileUtil.toFile(java_out).toString()+"\"");
            }
            if (isCppProject(p)){
              args.append(" --cpp_out \""+FileUtil.toFile(java_out).toString()+"\"");
            }
            if (isJavaProject(p)
                ||((!isPythonProject(p))&&(!isCppProject(p))&&(!isJavaProject(p)))){
              args.append(" --java_out \""+FileUtil.toFile(java_out).toString()+"\"");
            }
            args.append(" \"" + file.getAbsolutePath() +"\"");

            writer.println("running: "+protocExecutable+" "+args.toString());
            NbProcessDescriptor protocProcessDesc =
                new NbProcessDescriptor( protocExecutable, args.toString());
            Process process = protocProcessDesc.exec();

            /*Removing all old annotations - we will got fresh errors.*/
            ProtobufAnnotation.removeAllAnnotationsForFile(file.getAbsolutePath());
            /* Parse the errorStream and create fresh annotations*/
            readOutput(writer, process.getErrorStream(), nodes,file.getAbsolutePath());
            /* Wait until the process finishes.*/
            process.waitFor();

            writer.println("Exit: "+process.exitValue());

            /*Reload content of the destination directory*/
            getProject(fileObject).getProjectDirectory().refresh();
            java_out.refresh();
        }else{
            writer.println("No Java sorce directory (destination directory)");
        }
        writer.flush();
    }

    /**
     * Reads the path to protoc compilet from the configuration
     * 
     * @return
     */
    public String getProtocPath() {
        String defaultValue = NbBundle.getMessage(
                ProtobufAction.class,
                "ProtocolBuffersPanel_ProtocPathDefault");
        return NbPreferences.forModule(ProtobufGeneratorRunnable.class).get(ProtobufGeneratorRunnable.PROTOC_PATH_KEY, defaultValue);
        //return NbPreferences.root().get(ProtobufGeneratorRunnable.PROTOC_PATH_KEY, defaultValue);
    }

    /** Save the DataObject if it has been modified */
    void forceSave(DataObject dataObject) throws IOException {
        if (dataObject.isModified()) {
            SaveCookie cookie =
                    dataObject.getCookie(SaveCookie.class);
            if (cookie != null) {
                cookie.save();
            }
        }
    }

    /**
     * This method reads the inputStream (errStream) and creates error
     * annotations {@link ProtobufAnnotation} based on messages from the stream. It
     * also forwards the messages to the writer.
     *
     * @param writer
     * @param errStream
     * @param nodes
     * @param fileName
     */
    static void readOutput(OutputWriter writer, InputStream errStream, Node[] nodes,String fileName) {
        ProtobufOutputListener listener = new ProtobufOutputListener(nodes);
        try {
            BufferedReader error = new BufferedReader(new InputStreamReader(errStream));
            String errString = null;
            while ((errString = error.readLine()) != null) {//for each line
                Matcher matcher = ProtobufOutputListener.PATTERN.matcher(errString);
                if (matcher.matches()) { //If the line matches pattern of error message
                    //Create annotation.
                    DataObject dataObject=ProtobufHelper.findDataObjectForFile(matcher.group(1),nodes);
                    if (dataObject!=null)
                    {
                        LineCookie lc = dataObject.getCookie(LineCookie.class);
                        ProtobufAnnotation.createAnnotation(lc,
                                Integer.parseInt(matcher.group(2)) - 1,
                                Integer.parseInt(matcher.group(3)),
                                "Error",
                                matcher.group(4),fileName);

                        writer.println(errString, listener);
                    }
                } else {
                    writer.println(errString);
                }
            }
        } catch (Exception e) {
            writer.println("Could not read process output " + e);
        }
    }

    /**
     * Returns FileObject that represents directory that is destination for
     * generated files.
     *
     * <p> We finds first directoy that contains JAVA_SOURCES {@link JavaProjectConstants#SOURCES_TYPE_JAVA}. We
     * also prefer this directory not to be test directory.
     * </p>
     *
     * TODO: This file should be selectable by the user (configurable per project),
     * or created automatically. 
     *
     * @param dataObject
     * @param writer
     * @return
     */
   private FileObject getJavaSourceDirForNode(DataObject dataObject, OutputWriter writer) {
        Project p = getProject(dataObject.getPrimaryFile());
        FileObject res = null;
        if (p != null) {
            Sources sources = ProjectUtils.getSources(p);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

            for (SourceGroup g : groups) {
                if (!g.getName().equals("${test.src.dir}")) {
                    res = g.getRootFolder();
                }
            }
        }
        return res;
    }

   /**
    * Returns project that given file belongs to.
    * @param file
    * @return
    */
    private Project getProject(FileObject file) {
        Project p = FileOwnerQuery.getOwner(file);
        return p;
    }

    
    private String getProjectPath(FileObject fileObject) {
        Project p=getProject( fileObject);
        return FileUtil.toFile(p.getProjectDirectory()).getAbsolutePath();
    }

  private FileObject getJavaDestinationDirectory(DataObject dataObject, OutputWriter writer) {
    Project p = getProject(dataObject.getPrimaryFile());
    String s=ProtobufAntHelper.getJavaGenDestinationDirectory(p);
    if (s!=null){
      File f=new File(FileUtil.toFile(p.getProjectDirectory()),s);
      f.mkdirs();
      FileObject res=FileUtil.toFileObject(FileUtil.normalizeFile(f));
      return res;
    }else{
      FileObject javaSrcDir=getJavaSourceDirForNode(dataObject, writer);
      if(javaSrcDir!=null){
          return javaSrcDir;
      }else{
          //No path - so we will use the same directory as dataObject.file
          return dataObject.getPrimaryFile().getParent();
      }
    }
  }

  private boolean isJavaProject(Project p) {
    return (p.getClass().toString().toUpperCase().contains("JAVA"));
  }

  private boolean isPythonProject(Project p) {
    return (p.getClass().toString().toUpperCase().contains("PYTHON"));
  }

  private boolean isCppProject(Project p) {
    return (p.getClass().toString().toUpperCase().contains("MAKEPROJECT"));
  }
}
