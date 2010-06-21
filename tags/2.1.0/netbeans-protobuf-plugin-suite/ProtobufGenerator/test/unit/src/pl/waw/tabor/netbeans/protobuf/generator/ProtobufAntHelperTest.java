/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ptab
 */
public class ProtobufAntHelperTest {

  public ProtobufAntHelperTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

      /** @param filePath the name of the file to open. Not sure if it can accept URLs or just filenames. Path handling could be better, and buffer sizes are hardcoded
    */
    private static String readFileAsString(InputStream is)
    throws java.io.IOException{
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is)/*new FileReader(filePath)*/);
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }


  @Test
  public void refreshBuildScriptTest() throws TransformerException, IOException
  {
      ByteArrayOutputStream baos=new ByteArrayOutputStream();
      InputStream inputStream=ProtobufAntHelperTest.class.getResourceAsStream("resources/protobuf-build.cfg.xml");
      assertNotNull(inputStream);
      ProtobufAntHelper.refreshBuildScript(
        new StreamSource(inputStream),
        new StreamResult(baos)
      );
      String res=new String(baos.toByteArray());
      String expected=readFileAsString(ProtobufAntHelperTest.class.getResourceAsStream("resources/protobuf-build.xml"));
      assertEquals(expected, res);
  }

}
