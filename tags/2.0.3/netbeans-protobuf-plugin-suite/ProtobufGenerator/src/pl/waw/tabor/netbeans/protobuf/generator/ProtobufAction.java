/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 * Action that is run when user selects "Regenerate file(s) from this
 * protobuf definition(s)". 
 * @author ptab
 */
public final class ProtobufAction extends CookieAction {
    static RequestProcessor processor=null;

    /**
     * We got list of selected nodes (files - {@link DataObject})
     * @param activatedNodes
     */
    protected void performAction(Node[] activatedNodes) {
        getProcessor().post(new ProtobufGeneratorRunnable(activatedNodes,""));
    }

    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    public String getName() {
        return NbBundle.getMessage(ProtobufAction.class, "CTL_ProtobufGenerateAction");
    }

    /**
     * This action work on DataObjects
     * @return
     */
    protected Class[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    @Override
    protected String iconResource() {
        return "pl/waw/tabor/netbeans/protobuf/generator/resources/g.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private RequestProcessor getProcessor() {
        if(processor==null)
        {
            processor=new RequestProcessor("ProtobufCodeGenerator1",1,true);
        }
        return processor;
    }

}

