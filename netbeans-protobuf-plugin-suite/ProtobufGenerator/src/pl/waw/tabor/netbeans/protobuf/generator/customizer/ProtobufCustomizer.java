package pl.waw.tabor.netbeans.protobuf.generator.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;
import pl.waw.tabor.netbeans.protobuf.generator.ProtobufAntHelper;
import pl.waw.tabor.netbeans.protobuf.generator.ProtobufGeneratorConfig;

/**
 *
 * @author ptab
 */
public class ProtobufCustomizer implements ProjectCustomizer.CompositeCategoryProvider {

  private static final Logger logger = Logger.getLogger(ProtobufCustomizer.class.getName());
  private static final String PROTOBUF = "protobuf";
  private static final ProtobufCustomizerPanel panel = new ProtobufCustomizerPanel();

  public static ProtobufCustomizer create() {
    return new ProtobufCustomizer();
  }

  public Category createCategory(Lookup context) {
    ProjectCustomizer.Category category = ProjectCustomizer.Category.create(
            PROTOBUF,
            NbBundle.getMessage(ProtobufCustomizer.class, "LBL_Protobuf_Compiling"),
            null,
            (ProjectCustomizer.Category[]) null);
    category.setStoreListener(new StoreActionListener(context, panel));
    return category;
  }

  public JComponent createComponent(Category category, Lookup context) {
    Project project = context.lookup(Project.class);
    File f = ProtobufAntHelper.getProtobufConfigFile(project);
    ProtobufGeneratorConfig pgc = new ProtobufGeneratorConfig();
    if (f.exists()) {
      try {
        pgc.readFrom(new FileInputStream(f));
      } catch (FileNotFoundException ex) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(ex));
      } catch (SAXException ex) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(ex));
      } catch (ParserConfigurationException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    } else {
      pgc.setProjectName(project.toString());
    }

    panel.setConfig(pgc);
    return panel;
  }

  private static final class StoreActionListener implements ActionListener {

    private final Lookup context;
    private final ProtobufCustomizerPanel panel;

    public StoreActionListener(Lookup context, ProtobufCustomizerPanel panel) {
      this.context = context;
      this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(ProtobufAntHelper.getProtobufConfigFile(context.lookup(Project.class)));
        ProtobufGeneratorConfig config=panel.getConfig();
        config.writeTo(fos);
        fos.close();

        Project project=context.lookup(Project.class);
        ProtobufAntHelper.refreshBuildScript(project);

        if(config.isCpp_gen()||config.isJava_gen()||config.isPython_gen()){
          ProtobufAntHelper.addExtender(project);
        }else{
          ProtobufAntHelper.removeExtender(project);
        }

        if(config.isJava_gen()){
          ProtobufAntHelper.proposeRegistrationOfProtobufLibrary();
        }

         ProjectManager.getDefault().saveProject(project);
        //DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message("actionPerformed - panel"));
//            Project project = context.lookup(Project.class);
//            if (project != null) {
//                GroovyProjectExtender extender = project.getLookup().lookup(GroovyProjectExtender.class);
//                if (extender != null) {
//                    GroovyCustomizerPanel panel = extender.getPanel();
//                    if (panel != null) {
//                        if (panel.isEnablingCheckboxSelected() && !extender.isGroovyEnabled()) {
//                            extender.enableGroovy();
//                        } else if (!panel.isEnablingCheckboxSelected() && extender.isGroovyEnabled()) {
//                            extender.disableGroovy();
//                        }
//                    }
//                }
//            }
      } catch (ParserConfigurationException ex) {
        Exceptions.printStackTrace(ex);
      } catch (FileNotFoundException ex) {
        Exceptions.printStackTrace(ex);
      } catch (JAXBException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      } finally {
        try {
          fos.close();
        } catch (IOException ex) {
          Exceptions.printStackTrace(ex);
        }
      }
    }
  }
}
