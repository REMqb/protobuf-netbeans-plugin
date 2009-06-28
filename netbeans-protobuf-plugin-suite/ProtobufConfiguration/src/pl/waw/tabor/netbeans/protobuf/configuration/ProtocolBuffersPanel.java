/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.configuration;

import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.openide.util.NbBundle;
import pl.waw.tabor.netbeans.protobuf.generator.ProtobufAction;
import pl.waw.tabor.netbeans.protobuf.generator.ProtobufGeneratorRunnable;

final class ProtocolBuffersPanel extends javax.swing.JPanel {

    private final ProtocolBuffersOptionsPanelController controller;

    ProtocolBuffersPanel(ProtocolBuffersOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
    // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        protocPathLabal = new javax.swing.JLabel();
        protocPathTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(protocPathLabal, org.openide.util.NbBundle.getMessage(ProtocolBuffersPanel.class, "ProtocolBuffersPanel.protocPathLabal.text")); // NOI18N

        protocPathTextField.setText(org.openide.util.NbBundle.getMessage(ProtocolBuffersPanel.class, "ProtocolBuffersPanel.protocPathTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ProtocolBuffersPanel.class, "ProtocolBuffersPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(protocPathLabal)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(protocPathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(protocPathLabal)
                    .add(protocPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String filename = protocPathTextField.getText();
        JFileChooser chooser = new JFileChooser(new File(filename));
        int result = chooser.showOpenDialog(this);
        switch (result) {
            case JFileChooser.APPROVE_OPTION:
                File selectedFile = chooser.getSelectedFile();
                protocPathTextField.setText(
                        selectedFile.getAbsolutePath());
                controller.changed();
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
}//GEN-LAST:event_browseButtonActionPerformed

    void load() {
        String defaultValue = NbBundle.getMessage(
                ProtobufAction.class,
                "ProtocolBuffersPanel_ProtocPathDefault");
        protocPathTextField.setText(Preferences.userNodeForPackage(
                ProtobufGeneratorRunnable.class).get(
                ProtobufGeneratorRunnable.PROTOC_PATH_KEY, defaultValue));
    }
    
    void store() {
        Preferences.userNodeForPackage(ProtobufGeneratorRunnable.class).
        put(ProtobufGeneratorRunnable.PROTOC_PATH_KEY,  protocPathTextField.getText());
    }


    boolean valid() {
        File f = new File(protocPathTextField.getText());
        return f.exists();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel protocPathLabal;
    private javax.swing.JTextField protocPathTextField;
    // End of variables declaration//GEN-END:variables
}
