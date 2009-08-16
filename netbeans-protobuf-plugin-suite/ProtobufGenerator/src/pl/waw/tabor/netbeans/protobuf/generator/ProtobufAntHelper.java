/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author piotr.tabor@gmail.com
 */
public class ProtobufAntHelper {

  private static final String NBPROJECT_DIR = "nbproject"; //NOI18N
  private static final String PROTOBUF_BUILD_FILE_NAME = "protobuf-build.xml"; //NOI18N
  private static final String PROTOBUF_CONTEXT_CLASS_RES_PATH = "com/google/protobuf/Message.class"; //NOI18N
  private static final String PROTOBUF_LIB_NAME = "protobuf"; //NOI18N
  private static final String PROTOBUF_ANT_XTN_NAME = "protobuf"; //NOI18N

  public void refreshProtobufBuildFile() {
  }

  public static void refreshBuildScript(Project prj) {
    try {
      InputStream is = ProtobufAntHelper.class.getResourceAsStream(PROTOBUF_BUILD_FILE_NAME);
      OutputStream os = new FileOutputStream(getXMLBindingConfigFile(prj));
      FileUtil.copy(is, os);
      is.close();
      os.close();

//            Source xmlSource = new StreamSource(getXMLBindingConfigFile(prj));
//            Source xslSource = null;
//            int projType = getProjectType(prj);
//            if (projType == PROJECT_TYPE_EJB) {
//                xslSource = new StreamSource(
//                        ProjectHelper.class.getClassLoader().getResourceAsStream(
//                        EJB_XSL_RESOURCE));
//            } else if (projType == PROJECT_TYPE_WEB) {
//                xslSource = new StreamSource(
//                        ProjectHelper.class.getClassLoader().getResourceAsStream(
//                        WEB_XSL_RESOURCE));
//            } else {
//                xslSource = new StreamSource(
//                        ProjectHelper.class.getClassLoader().getResourceAsStream(
//                        XSL_RESOURCE));
//            }
//
//            Result result = new StreamResult(getXMLBindingBuildFile(prj));
//            TransformerFactory fact = TransformerFactory.newInstance();
//            try {
//                fact.setAttribute("indent-number", 4); //NOI18N
//            } catch (Exception ex) {
//                //Ignore Xalan does not recognize "indent-number"
//            }
//            Transformer xformer = fact.newTransformer(xslSource);
//            xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
//            xformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
//            xformer.transform(xmlSource, result);
    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  private static File getXMLBindingConfigFile(Project prj) {
    File configFile = null;
    if (prj != null) {
      FileObject fo = prj.getProjectDirectory();
      File projDir = FileUtil.toFile(fo);

      try {
        configFile = new File(projDir, NBPROJECT_DIR + File.separator + PROTOBUF_BUILD_FILE_NAME);
      } catch (Exception ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    return configFile;
  }

  public static AntProjectHelper getAntProjectHelper(Project project) {
    try {
      Method getAntProjectHelperMethod = project.getClass().getMethod(
              "getAntProjectHelper"); //NOI18N
      if (getAntProjectHelperMethod != null) {
        AntProjectHelper helper = (AntProjectHelper) getAntProjectHelperMethod.invoke(project);

        return helper;
      }
    } catch (NoSuchMethodException nme) {
      Exceptions.printStackTrace(nme);
    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }

    return null;
  }

  private static void executeAntTarget(final Project project,
          final boolean addLibs,
          final String antTarget) {
    final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ProtobufAntHelper.class, "MSG_PROTOBUF_PROGRESS")); //NOI18N;
    progressHandle.start();

    Runnable run = new Runnable() {

      public void run() {
        try {
          if (addLibs) {
            addProtobufLibrary(project);
          }

          FileObject buildXml = getFOForProjectBuildFile(project);
          String[] args = new String[]{antTarget};

          if (buildXml != null) {
            ExecutorTask task = ActionUtils.runTarget(buildXml, args, null);
            task.waitFinished();
            if (task.result() != 0) {
              String mes = NbBundle.getMessage(ProtobufAntHelper.class, "MSG_ERROR_COMPILING"); //NOI18N
              NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
              DialogDisplayer.getDefault().notify(desc);
            }
          }
        } catch (IOException ioe) {
          Exceptions.printStackTrace(ioe);
        } catch (Exception e) {
          Exceptions.printStackTrace(e);
        } finally {
          progressHandle.finish();
        }
      }
    };

    RequestProcessor.getDefault().post(run);
  }

  public static FileObject getFOForProjectBuildFile(Project prj) {
    FileObject buildFileFo = null;
    if (prj != null) {
      FileObject fo = prj.getProjectDirectory();
      buildFileFo = fo.getFileObject("build.xml"); //NOI18N
    }
    return buildFileFo;
  }

  private static void addProtobufLibrary(Project prj) {
    SourceGroup[] sgs = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    ClassPath compileClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
    ClassPath bootClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.BOOT);
    ClassPath classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{compileClassPath, bootClassPath});

    FileObject jaxbClass = classPath.findResource(PROTOBUF_CONTEXT_CLASS_RES_PATH);
    if (jaxbClass == null) {
      // Add JAXB jars if not in the classpath
      Library jaxbLib = LibraryManager.getDefault().getLibrary(PROTOBUF_LIB_NAME);
      Sources srcs = ProjectUtils.getSources(prj);
      if (srcs != null) {
        SourceGroup[] srg = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ((srg != null) && (srg.length > 0)) {
          try {
            ProjectClassPathModifier.addLibraries(
                    new Library[]{jaxbLib}, srg[0].getRootFolder(),
                    ClassPath.COMPILE);
          } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
          }
        }
      }
    }
  }

  public static FileObject getFOForBindingBuildFile(Project prj) {
        FileObject buildFileFo = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            try {
                fo.getFileObject(NBPROJECT_DIR + File.separator).refresh();
                buildFileFo = fo.getFileObject(NBPROJECT_DIR + File.separator + PROTOBUF_BUILD_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return buildFileFo;
    }


  public void addExtender(Project project) throws IOException {
    AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);
    if (ext != null && ext.getExtension(PROTOBUF_ANT_XTN_NAME) == null) {
      FileObject jaxbBuildXml = getFOForBindingBuildFile(project);
      AntBuildExtender.Extension jaxbBuild = ext.addExtension(PROTOBUF_ANT_XTN_NAME, jaxbBuildXml);
      jaxbBuild.addDependency("protobuf-code-generation","-pre-pre-compile");
      ProjectManager.getDefault().saveProject(project);
    }
  }
}
