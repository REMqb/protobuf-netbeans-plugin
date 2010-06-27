/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author ptab
 */
public class ProtobufHelper {

    public void jaxbTest(){
      
    }

    /**
     *
     * Find a file (one of files the protoc was runned on)
     *
     * @param fileName
     * @return DataObject connected to the fileName or NULL if not found.
     */
    public static DataObject findDataObjectForFile(String fileName,Node[] nodes) {
        for(Node node:nodes)
        {
            DataObject d = node.getCookie(DataObject.class);
            FileObject fileObject=d.getPrimaryFile();
            File file = FileUtil.toFile(fileObject);
            if (file.toString().endsWith(fileName))
                return d;
        }
        return null;
    }
}

