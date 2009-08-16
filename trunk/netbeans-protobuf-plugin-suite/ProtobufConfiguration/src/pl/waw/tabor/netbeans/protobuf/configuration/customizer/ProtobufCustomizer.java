package pl.waw.tabor.netbeans.protobuf.configuration.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author ptab
 */
public class ProtobufCustomizer implements ProjectCustomizer.CompositeCategoryProvider {
  private static final String PROTOBUF="protobuf";

  public static ProtobufCustomizer create(){
    return new ProtobufCustomizer();
  }

  public Category createCategory(Lookup context) {
    ProjectCustomizer.Category category = ProjectCustomizer.Category.create(
                PROTOBUF,
                NbBundle.getMessage(ProtobufCustomizer.class, "LBL_Protobuf_Compiling"),
                null,
                (ProjectCustomizer.Category[]) null
                );
        category.setStoreListener(new StoreActionListener(context));
        return category;
  }

  public JComponent createComponent(Category category, Lookup context) {
    return new ProtobufCustomizerPanel();
  }


  private static final class StoreActionListener implements ActionListener {

        private final Lookup context;

        public StoreActionListener(Lookup context) {
            this.context = context;
        }

        public void actionPerformed(ActionEvent e) {
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
        }

    }


}
