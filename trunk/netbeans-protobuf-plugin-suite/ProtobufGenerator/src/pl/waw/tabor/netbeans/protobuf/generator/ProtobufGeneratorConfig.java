/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.waw.tabor.netbeans.protobuf.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.waw.tabor.netbeans.protobuf.generator.jaxb.ProtobufConfig;

/**
 *
 * @author ptab
 */
public class ProtobufGeneratorConfig {
  private static JAXBContext context;
 
  private boolean java_gen = false;
  private boolean python_gen = false;
  private boolean cpp_gen = false;
  private String projectName;

  public boolean isCpp_gen() {
    return cpp_gen;
  }

  public void setCpp_gen(boolean cpp_gen) {
    this.cpp_gen = cpp_gen;
  }

  public boolean isJava_gen() {
    return java_gen;
  }

  public void setJava_gen(boolean java_gen) {
    this.java_gen = java_gen;
  }

  public boolean isPython_gen() {
    return python_gen;
  }

  public void setPython_gen(boolean python_gen) {
    this.python_gen = python_gen;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }


 public synchronized  JAXBContext getContext() throws JAXBException{
    if (context==null){
      context=JAXBContext.newInstance(ProtobufConfig.class);
    }
    return context;
 }



  public void readFrom(InputStream is) throws  ParserConfigurationException, IOException, SAXException
  {
      DocumentBuilder dbf=DocumentBuilderFactory.newInstance().newDocumentBuilder();

      setCpp_gen(false);
      setJava_gen(false);
      setPython_gen(false);

   //   XMLReader reader=XMLReaderFactory.createXMLReader();
      Document doc=dbf.parse(is);
      NodeList nl=doc.getDocumentElement().getElementsByTagName("generator");
      for(int i=0; i<nl.getLength(); i++){
         if ( nl.item(i) instanceof Element){
           Element e=(Element)nl.item(i);
           if("JAVA".equalsIgnoreCase(e.getAttribute("type"))){
             setJava_gen(xmlText2bool(e.getAttribute("on")));
           }
           if("CPP".equalsIgnoreCase(e.getAttribute("type"))){
             setCpp_gen(xmlText2bool(e.getAttribute("on")));
           }
           if("PYTHON".equalsIgnoreCase(e.getAttribute("type"))){
             setPython_gen(xmlText2bool(e.getAttribute("on")));
           }
         }
      }

      NodeList nl2=doc.getDocumentElement().getElementsByTagName("projectName");
      if(nl2.getLength()==1){
        setProjectName(((Element)(nl2.item(0))).getTextContent());
      }else{
        setProjectName(null);
      }
  }


//
//      ProtobufConfig pc=(ProtobufConfig) getContext().createUnmarshaller().unmarshal(is);
//      setProjectName(pc.getProjectName());
//      for(GeneratorType g:pc.getGenerator()){
//        switch(g.getType()){
//          case JAVA:
//            setJava_gen(g.isOn());
//            break;
//          case PYTHON:
//            setPython_gen(g.isOn());
//            break;
//          case CPP:
//            setCpp_gen(g.isOn());
//            break;
//        }
//      }
 // }
//<?xml version="1.0" encoding="UTF-8"?>
//<protobuf-config
//  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
//  xmlns='http://tabor.waw.pl/netbeans/protobuf/generator/jaxb'
//   xsi:schemaLocation='http://tabor.waw.pl/netbeans/protobuf/generator/jaxb file:/home/ptab/NetBeansProjects/JavaApplication5/nbproject/ProtobufConfig.xsd'>
//  <generator type="java" on="true"/>
//  <generator type="cpp" on="true"/>
//  <generator type="python" on="true"/>
//</protobuf-config>
  public void writeTo(OutputStream os) throws JAXBException, IOException, ParserConfigurationException{
    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document document=dbf.newDocumentBuilder().newDocument();


    Element root=document.createElementNS("http://tabor.waw.pl/netbeans/protobuf/generator/jaxb", "protobuf-config");

    root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://tabor.waw.pl/netbeans/protobuf/generator/jaxb ProtobufConfig.xsd");
    root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    root.setAttribute("xmlns", "http://tabor.waw.pl/netbeans/protobuf/generator/jaxb");
    document.appendChild(root);

    Element e=document.createElement("generator");
    e.setAttribute("type", "cpp");
    e.setAttribute("on", Boolean.toString(isCpp_gen()));
    root.appendChild(e);

    e=document.createElement("generator");
    e.setAttribute("type", "java");
    e.setAttribute("on", Boolean.toString(isJava_gen()));
    root.appendChild(e);

    e=document.createElement("generator");
    e.setAttribute("type", "python");
    e.setAttribute("on", Boolean.toString(isPython_gen()));
    root.appendChild(e);

    if(projectName!=null){
      e=document.createElement("projectName");
      e.setTextContent(projectName);
      root.appendChild(e);
    }


    OutputFormat format = new OutputFormat(document);
    XMLSerializer output = new XMLSerializer(os, format);
    output.serialize(document);
//
//
//      ProtobufConfig pc=new ProtobufConfig();
//      pc.setProjectName(getProjectName());
//
//      if (isCpp_gen()){
//        GeneratorType gt=new GeneratorType();
//        gt.setOn(true);
//        gt.setType(DestinationLanguage.CPP);
//        pc.getGenerator().add(gt);
//      }
//      if (isJava_gen()){
//        GeneratorType gt=new GeneratorType();
//        gt.setOn(true);
//        gt.setType(DestinationLanguage.JAVA);
//        pc.getGenerator().add(gt);
//      }
//      if (isPython_gen()){
//        GeneratorType gt=new GeneratorType();
//        gt.setOn(true);
//        gt.setType(DestinationLanguage.PYTHON);
//        pc.getGenerator().add(gt);
//      }
//      getContext().createMarshaller().marshal(pc, os);

  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ProtobufGeneratorConfig other = (ProtobufGeneratorConfig) obj;
    if (this.java_gen != other.java_gen) {
      return false;
    }
    if (this.python_gen != other.python_gen) {
      return false;
    }
    if (this.cpp_gen != other.cpp_gen) {
      return false;
    }
    if ((this.projectName == null) ? (other.projectName != null) : !this.projectName.equals(other.projectName)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 61 * hash + (this.java_gen ? 1 : 0);
    hash = 61 * hash + (this.python_gen ? 1 : 0);
    hash = 61 * hash + (this.cpp_gen ? 1 : 0);
    hash = 61 * hash + (this.projectName != null ? this.projectName.hashCode() : 0);
    return hash;
  }

  private boolean xmlText2bool(String attribute) {
    return ("true".equalsIgnoreCase(attribute))||("yes".equalsIgnoreCase(attribute));
  }
  
}
