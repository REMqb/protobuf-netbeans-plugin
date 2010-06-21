/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author ptab
 */
public class ProtobufGeneratorConfigTest {

  @Test
  public void readDefaultFileTest() throws JAXBException, SAXException, IOException, ParserConfigurationException
  {
    ProtobufGeneratorConfig pgc=new ProtobufGeneratorConfig();
    pgc.readFrom(ProtobufGeneratorConfigTest.class.getResourceAsStream("resources/protobuf-build.cfg.xml"));
    Assert.assertTrue(pgc.isCpp_gen());
    Assert.assertTrue(pgc.isJava_gen());
    Assert.assertTrue(pgc.isPython_gen());
    Assert.assertEquals(null,pgc.getProjectName());
  }

  @Test
  public void writeReadTest()  throws JAXBException, SAXException, IOException, ParserConfigurationException
  {
    ProtobufGeneratorConfig pgc=new ProtobufGeneratorConfig();
    pgc.setCpp_gen(true);
    pgc.setJava_gen(false);
    pgc.setPython_gen(true);
    pgc.setProjectName("writeReadTest");

    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    pgc.writeTo(baos);
    baos.close();

    System.out.println("\nbaos:"+new String(baos.toByteArray()));

    ProtobufGeneratorConfig pgc2=new ProtobufGeneratorConfig();
    pgc2.readFrom(new ByteArrayInputStream(baos.toByteArray()));

    Assert.assertTrue(pgc2.isCpp_gen());
    Assert.assertFalse(pgc2.isJava_gen());
    Assert.assertTrue(pgc2.isPython_gen());
    Assert.assertTrue(pgc.equals(pgc2));
  }

    @Test
  public void writeReadTest2()  throws JAXBException, SAXException, IOException, ParserConfigurationException
  {
    ProtobufGeneratorConfig pgc=new ProtobufGeneratorConfig();
    pgc.setCpp_gen(false);
    pgc.setJava_gen(true);
    pgc.setPython_gen(false);
    pgc.setProjectName("writeReadTest2");

    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    pgc.writeTo(baos);
    baos.close();

    System.out.println("\nbaos:"+new String(baos.toByteArray()));

    ProtobufGeneratorConfig pgc2=new ProtobufGeneratorConfig();
    pgc2.readFrom(new ByteArrayInputStream(baos.toByteArray()));

    Assert.assertFalse(pgc2.isCpp_gen());
    Assert.assertTrue(pgc2.isJava_gen());
    Assert.assertFalse(pgc2.isPython_gen());
    Assert.assertTrue(pgc.equals(pgc2));
  }

}