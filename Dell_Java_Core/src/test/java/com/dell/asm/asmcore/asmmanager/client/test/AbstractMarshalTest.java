/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.test;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.junit.Test;

public abstract class AbstractMarshalTest<T>
{
 final Class<T> klazz;
 final JAXBContext jaxbContext;

 // Run with -DDUMP_OUTPUT=true to print sample XML / JSON templates
 static final boolean DUMP_OUTPUT = true;//Boolean.valueOf(System.getProperty("DUMP_OUTPUT", "false"));

 protected AbstractMarshalTest(Class<T> klazz)
 {
  try
   {
    this.klazz = klazz;
    this.jaxbContext = JAXBContext.newInstance(klazz);
   }
  catch (JAXBException e)
   { throw new RuntimeException(e); }
 }

 abstract T buildNamedDTO(String name);

 @Test
 public void testMarshalDiscoveryRequest() throws JAXBException
 {
  Marshaller marshaller = jaxbContext.createMarshaller();
  marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
  marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

  T dto = buildNamedDTO(UUID.randomUUID().toString());
  StringWriter stringWriter = new StringWriter();
  marshaller.marshal(dto, stringWriter);
  if (DUMP_OUTPUT)
   {
    System.out.println(stringWriter.toString());
   }
 }

 @Test
 public void testUnmarshalDiscoveryRequest() throws JAXBException, IOException
 {
  T dto = buildNamedDTO(UUID.randomUUID().toString());
  Marshaller marshaller = jaxbContext.createMarshaller();
  StringWriter stringWriter = new StringWriter();
  marshaller.marshal(dto, stringWriter);
  stringWriter.close();

  Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
  StringReader reader = new StringReader(stringWriter.toString());
  Object unmarshal = unmarshaller.unmarshal(reader);
 // assertEquals(dto, unmarshal);
 }

 @Test
 public void testMarshalDiscoveryRequestToJSON() throws JAXBException, XMLStreamException
 {
  T dto = buildNamedDTO(UUID.randomUUID().toString());

  StringWriter stringWriter = new StringWriter();

  MappedNamespaceConvention con = new MappedNamespaceConvention();
  AbstractXMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(con, stringWriter);


  Marshaller marshaller = jaxbContext.createMarshaller();
  marshaller.marshal(dto, xmlStreamWriter);
  xmlStreamWriter.close();

  if (DUMP_OUTPUT)
   {
    System.out.println(stringWriter.toString());
   }
 }
}
