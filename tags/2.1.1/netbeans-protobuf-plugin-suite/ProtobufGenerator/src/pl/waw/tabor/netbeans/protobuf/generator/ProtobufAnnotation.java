/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openide.cookies.LineCookie;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;

/**
 * Represents single "error" mark in the *.proto editor connected to errors 
 * reported by protoc compilert.
 *
 * @author <a href="piotr.tabor@gmail.com" (<a href="http://piotr.tabor.waw.pl">http://piotr.tabor.waw.pl</a>)
 */
class ProtobufAnnotation extends Annotation {

    /**
     * Map from file's path to all annotation that are connected to the file
     */
    private static final Map<String,List<ProtobufAnnotation>> annotations = new HashMap<String, List<ProtobufAnnotation>>();

    /**
     * We support only "error" annotations. 
     */
    private static final String[] annoType = {
        "pl-waw-tabor-netbeans-protobuf-generator-resources-tidyerrorannotation"
    };

    /*Description and position of the problem*/
    private String reason;
    private int column;
    private int severity = 0;
    private String fileName;

    /**
     * Creates the annotation and puts it in global struct of all annotations {@link #annotations}.
     *
     * Does not link the annotation with the line.
     *
     * @param severity
     * @param column
     * @param reason
     * @param fileName
     * @return
     */
    private static ProtobufAnnotation create(String severity, int column, String reason,String fileName) {
        ProtobufAnnotation annotation =
                new ProtobufAnnotation(severity, column, reason,fileName);
        List<ProtobufAnnotation> list=annotations.get(fileName);
        if(list==null)
        {
            list=new LinkedList<ProtobufAnnotation>();
            annotations.put(fileName,list);
        }
        list.add(annotation);
        return annotation;
    }

    /* Removes the annotation from the global struct of all annotations {@link #annotations}
     *
     * Doesn't deattach the annotation from the line.
     */
    private static void remove(ProtobufAnnotation annotation) {
        List<ProtobufAnnotation> list=annotations.get(annotation.fileName);
        list.remove(annotation);
    }

    /**
     * Create a new instance of TidyErrorAnnotation
     */
    private ProtobufAnnotation(String severity,
            int column, String reason,String fileName) {
        this.severity = severity.contains("Err") ? 0 : 1;
        this.reason = reason;
        this.column = column;
        this.fileName=fileName;
    }

    /**
     * Define the Tidy Annotation type
     *
     * @return Constant String "TidyErrorAnnotation"
     */
    public String getAnnotationType() {
        return annoType[severity];
    }

    /** Provide the Tidy error message as a description.
     * @return Annotation Reason */
    public String getShortDescription() {
        return reason + " (" + "Column: " + column + ")";
    }

    /** Create an annotation for a line in the given file.
     *
     * <p> Attaches the annotation to the line and sets listener on the line
     * in case of modification</p>
     */
    public static void createAnnotation(
            final LineCookie lc, int lineNumber,int columnNumber,String severity, String reason,String fileName)
            throws IndexOutOfBoundsException, NumberFormatException {

        final Line line = lc.getLineSet().getOriginal(lineNumber);
        final ProtobufAnnotation annotation =
                ProtobufAnnotation.create(severity, columnNumber, reason,fileName);
        annotation.attach(line);
        line.addPropertyChangeListener(new PropertyChangeListener() {
            /*If user changes the line - we want to remove the annotation.
             * User probably just fixed the line.
             */
            public void propertyChange(PropertyChangeEvent ev) {
                String type = ev.getPropertyName();
                  if ((type == null) ||
                         type.equals(Annotatable.PROP_TEXT)) {
                    line.removePropertyChangeListener(this);
                    annotation.detach();
                    ProtobufAnnotation.remove(annotation);
                 }
             }
          });
    }

    public static void removeAllAnnotationsForFile(String fileName)
    {
        List<ProtobufAnnotation> list=annotations.get(fileName);
        if(list!=null)
        {
            for(Annotation a:list)
            {
                a.detach();
            }
            while(list.size()>0)
               ProtobufAnnotation.remove(list.get(0));
        }
    }
}
