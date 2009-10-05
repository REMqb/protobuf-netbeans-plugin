<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:s='http://tabor.waw.pl/netbeans/protobuf/generator/jaxb'
                xmlns:exsl="http://exslt.org/common"
                extension-element-prefixes="exsl"
                version="1.0">
  <xsl:output method="xml" indent="yes" xmlns:xalan="http://xml.apache.org/xslt"  xalan:indent-amount="4" />

  <xsl:template match="/">
    <project name="JavaApplication5_protobuf" default="default" basedir=".">

      <target name="protobuf-environment">
        <xsl:element name="echo">
          <xsl:attribute name="message">user.properties.file:${user.properties.file}</xsl:attribute>
        </xsl:element>
        <xsl:element name="dirname">
          <xsl:attribute name="property">user.properties.dir</xsl:attribute>
          <xsl:attribute name="file">${user.properties.file}</xsl:attribute>
        </xsl:element>
        <xsl:element name="echo">
          <xsl:attribute name="message">user.properties.dir:${user.properties.dir}</xsl:attribute>
        </xsl:element>
        <xsl:element name="property">
          <xsl:attribute name="file">${user.properties.dir}/config/Preferences/pl/waw/tabor/netbeans/protobuf/generator.properties</xsl:attribute>
        </xsl:element>
        <property name="protobuf.executable" value="protoc"/><!-- If undefined in the file above, or the file does not exist-->
        <xsl:element name="property">
          <xsl:attribute name="name">build.generated.sources.dir.protobuf.java</xsl:attribute>
          <xsl:attribute name="value">${build.generated.sources.dir}/protobuf-java</xsl:attribute>
        </xsl:element>
        <xsl:element name="property">
          <xsl:attribute name="name">build.generated.sources.dir.protobuf.cpp</xsl:attribute>
          <xsl:attribute name="value">${build.generated.sources.dir}/protobuf-cpp</xsl:attribute>
        </xsl:element>
        <xsl:element name="property">
          <xsl:attribute name="name">build.generated.sources.dir.protobuf.python</xsl:attribute>
          <xsl:attribute name="value">${build.generated.sources.dir}/protobuf-python</xsl:attribute>
        </xsl:element>
      </target>

      <target name="protobuf-code-generation" depends="-do-init,-init-macrodef-javac,protobuf-environment">
        <xsl:element name="fileset">
          <xsl:attribute name="dir">${src.dir}</xsl:attribute>
          <xsl:attribute name="id">proto.classpath</xsl:attribute>
          <xsl:element name="include">
            <xsl:attribute name="name">**/*.proto</xsl:attribute>
          </xsl:element>
        </xsl:element>
        <pathconvert property="protofiles" pathsep=" " refid="proto.classpath" />

        <xsl:for-each select="/s:protobuf-config/s:generator[@on='true']">
          <xsl:element name="mkdir">
            <xsl:attribute name="dir">${build.generated.sources.dir.protobuf.<xsl:value-of select='@type'/>}</xsl:attribute>
          </xsl:element>
        </xsl:for-each>

        <xsl:variable name="args">
          <xsl:element name="arg">
            <xsl:attribute name="line">-I ${basedir}</xsl:attribute>
          </xsl:element>

          <xsl:element name="arg">
            <xsl:attribute name="line">${protofiles}</xsl:attribute>
          </xsl:element>

            
          <xsl:for-each select="/s:protobuf-config/s:generator[@on='true']">
            <xsl:element name="arg"><xsl:attribute name="line">--<xsl:value-of select="@type"/>_out=${build.generated.sources.dir.protobuf.<xsl:value-of select="@type"/>}</xsl:attribute></xsl:element>
          </xsl:for-each>

        </xsl:variable>

        <xsl:element name="echo">
          <xsl:attribute name="message">${protobuf.executable} <xsl:for-each select="exsl:node-set($args)/arg/@line" ><xsl:value-of select="concat(' ',.)"/></xsl:for-each>
          </xsl:attribute>
        </xsl:element>

        <xsl:element name="exec">
          <xsl:attribute name="executable">${protobuf.executable}</xsl:attribute>
          <xsl:attribute name="searchpath">true</xsl:attribute>
          <xsl:copy-of select="$args"/>
        </xsl:element>
                        
      </target>
   </project>
  </xsl:template>
</xsl:stylesheet>



