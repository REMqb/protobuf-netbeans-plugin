/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
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
  private static final String PROTOBUF_CONFIG_FILE_NAME = "protobuf-build.cfg.xml"; //NOI18N
  private static final String PROTOBUF_CONTEXT_CLASS_RES_PATH = "com/google/protobuf/Message.class"; //NOI18N
  private static final String PROTOBUF_LIB_NAME = "protobuf"; //NOI18N
  private static final String PROTOBUF_ANT_XTN_NAME = "protobuf"; //NOI18N


  
  public static void refreshBuildScript(Project prj) {
    try {
      refreshBuildScript(new StreamSource(getProtobufConfigFile(prj)), new StreamResult(getProtobufBuildFile(prj)));
    } catch (TransformerConfigurationException ex) {
      Exceptions.printStackTrace(ex);
    } catch (TransformerException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

    public static void refreshBuildScript(/*Project prj,*/StreamSource xmlSource, StreamResult result) throws TransformerConfigurationException, TransformerException {
              Source xslSource = new StreamSource(ProtobufAntHelper.class.getResourceAsStream("resources/protobuf-build.xsl"));
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

//            Result result = new StreamResult(getProtobufBuildFile(prj));
            TransformerFactory fact = TransformerFactory.newInstance();
            try {
                fact.setAttribute("indent-number", 4); //NOI18N
            } catch (Exception ex) {
                //Ignore Xalan does not recognize "indent-number"
            }
            Transformer xformer = fact.newTransformer(xslSource);
            xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
            xformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
            xformer.transform(xmlSource, result);
  }


  private static File getProtobufBuildFile(Project prj) {
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

  public static File getProtobufConfigFile(Project prj) {
    File configFile = null;
    if (prj != null) {
      FileObject fo = prj.getProjectDirectory();
      File projDir = FileUtil.toFile(fo);

      try {
        configFile = new File(projDir, NBPROJECT_DIR + File.separator + PROTOBUF_CONFIG_FILE_NAME);
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

  private static String getProperty(Project prj/*, String filePath*/,
            String name) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        return aph.getStandardPropertyEvaluator().getProperty(name);
        //aph.g
//        EditableProperties ep = aph.getProperties(filePath);
//        String str = null;
//        String value = ep.getProperty(name);
//        if (value != null) {
//            PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
//            str = pe.evaluate(value);
//        }
//        return "abc";
    }

  public static String getJavaGenDestinationDirectory(Project prj)
  {
      return getProperty(prj,"build.generated.sources.dir")+"/protobuf-java";
  }

  private static void executeAntTarget(final Project project,
          final boolean addLibs,
          final String antTarget) {
    final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ProtobufAntHelper.class, "MSG_PROTOBUF_PROGRESS")); //NOI18N;
    progressHandle.start();


    Runnable run = new Runnable() {

      @Override
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
            System.out.println("prj!=null");
            FileObject fo = prj.getProjectDirectory();
            try {
                fo.getFileObject(NBPROJECT_DIR + File.separator).refresh();
                buildFileFo = fo.getFileObject(NBPROJECT_DIR + File.separator + PROTOBUF_BUILD_FILE_NAME);
                System.out.println("NBPROJECT_DIR + File.separator + PROTOBUF_BUILD_FILE_NAME:"+NBPROJECT_DIR + File.separator + PROTOBUF_BUILD_FILE_NAME);
                System.out.println("buildFileFo:"+buildFileFo);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return buildFileFo;
    }


  public static void addExtender(Project project) throws IOException {

    System.out.println("Destination generation directory: "+getJavaGenDestinationDirectory(project));

    AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);

    removeExtender(project);

    if (ext != null && ext.getExtension(PROTOBUF_ANT_XTN_NAME) == null) {
      //System.out.println("Registering extension");
      FileObject jaxbBuildXml = getFOForBindingBuildFile(project);
      AntBuildExtender.Extension jaxbBuild = ext.addExtension(PROTOBUF_ANT_XTN_NAME, jaxbBuildXml);
      jaxbBuild.addDependency("-pre-pre-compile","protobuf-code-generation");      
    }
  }

  public static void removeExtender(Project project){
    AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);

    if (ext != null && ext.getExtension(PROTOBUF_ANT_XTN_NAME) != null) {
      System.out.println("Removing extension");
      ext.removeExtension(PROTOBUF_ANT_XTN_NAME);
    }
  }

  public static void proposeRegistrationOfProtobufLibrary() {
    //throw new UnsupportedOperationException("Not yet implemented");
  }
}
