/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.openide.cookies.LineCookie;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;

/**
 *
 * @author ptab
 */
class ProtobufAnnotation extends Annotation {

    private static List<Annotation> annotations =
            new ArrayList<Annotation>();
    private static String[] annoType = {
        "pl-waw-tabor-netbeans-protobuf-generator-tidyerrorannotation"
//       ,"org-yourorghere-nbtidyintegration-tidywarningannotation"
    };
    private String reason;
    private int column;
    private int severity = 0;

    public static ProtobufAnnotation create(
            String severity, int column, String reason) {
        ProtobufAnnotation annotation =
                new ProtobufAnnotation(severity, column, reason);
        annotations.add(annotation);
        return annotation;
    }

    public static void clear() {
        for (Annotation annotation : annotations) {
            annotation.detach();
        }
    }

    public static void remove(ProtobufAnnotation annotation) {
        annotations.remove(annotation);
    }

    /**
     * Create a new instance of TidyErrorAnnotation
     */
    private ProtobufAnnotation(String severity,
            int column, String reason) {
        this.severity = severity.contains("Err") ? 0 : 1;
        this.reason = reason;
        this.column = column;
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

    /** Create an annotation for a line from a match string */
    public static void createAnnotation(
            final LineCookie lc, int lineNumber,int columnNumber,String severity, String reason)
            throws IndexOutOfBoundsException, NumberFormatException {

        final Line line = lc.getLineSet().getOriginal(lineNumber);
        final ProtobufAnnotation annotation =
                ProtobufAnnotation.create(severity, columnNumber, reason);
        annotation.attach(line);
        line.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent ev) {
                String type = ev.getPropertyName();
                if ((type == null) ||
                        type.equals(Annotatable.PROP_TEXT)) {
// User edited the line, assume error should be cleared
                    line.removePropertyChangeListener(this);
                    annotation.detach();
                    ProtobufAnnotation.remove(annotation);
                }
            }
        });
    }
}
