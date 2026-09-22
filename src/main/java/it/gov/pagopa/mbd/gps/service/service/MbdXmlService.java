package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.model.marcadabollo.TipoMarcaDaBollo;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

@Service
public class MbdXmlService {

  private static final String ENTITY_UID_TYPE_ELEMENT = "entityUniqueIdentifierType";
  private static final String ENTITY_UID_VALUE_ELEMENT = "entityUniqueIdentifierValue";
  private static final Schema MARCA_DA_BOLLO_SCHEMA = loadMarcaDaBolloSchema();

  private final Jaxb2Marshaller partnerJaxb2Marshaller;
  private final Jaxb2Marshaller marcaDaBolloJaxb2Marshaller;

  public MbdXmlService(
      @Qualifier("partnerJaxb2Marshaller") Jaxb2Marshaller partnerJaxb2Marshaller,
      @Qualifier("marcaDaBolloJaxb2Marshaller") Jaxb2Marshaller marcaDaBolloJaxb2Marshaller) {
    this.partnerJaxb2Marshaller = partnerJaxb2Marshaller;
    this.marcaDaBolloJaxb2Marshaller = marcaDaBolloJaxb2Marshaller;
  }

  /** UnmarshalsMarcaDaBollo payload using marcaDaBollo Marshaller and Schema validation. */
  @SuppressWarnings("unchecked")
  public TipoMarcaDaBollo unmarshalMarcaDaBollo(byte[] xmlData)
      throws XMLStreamException, JAXBException {
    XMLStreamReader reader = createMarcaDaBolloReader(xmlData);
    try {
      var unmarshaller = marcaDaBolloJaxb2Marshaller.getJaxbContext().createUnmarshaller();
      unmarshaller.setSchema(MARCA_DA_BOLLO_SCHEMA);
      JAXBElement<TipoMarcaDaBollo> element =
          unmarshaller.unmarshal(reader, TipoMarcaDaBollo.class);
      return element.getValue();
    } finally {
      reader.close();
    }
  }

  /** Serializza oggetti Partner/Nodo (Request e Response SOAP/XML) */
  public String marshal(Object object) {
    StringWriter writer = new StringWriter();
    partnerJaxb2Marshaller.marshal(object, new StreamResult(writer));
    return writer.toString();
  }

  private XMLStreamReader createMarcaDaBolloReader(byte[] datiSpecificiServizio)
      throws XMLStreamException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    XMLStreamReader baseReader =
        inputFactory.createXMLStreamReader(new ByteArrayInputStream(datiSpecificiServizio));
    return new StreamReaderDelegate(baseReader) {
      @Override
      public String getNamespaceURI() {
        String localName = getLocalName();
        if (ENTITY_UID_TYPE_ELEMENT.equals(localName)
            || ENTITY_UID_VALUE_ELEMENT.equals(localName)) {
          return "";
        }
        return super.getNamespaceURI();
      }
    };
  }

  @SuppressWarnings("java:S2755")
  private static Schema loadMarcaDaBolloSchema() {
    try {
      ClassLoader classLoader = MbdXmlService.class.getClassLoader();
      URL paForNodeUrl = classLoader.getResource("wsdl/xsd/paForNode.xsd");
      URL marcaDaBolloUrl = classLoader.getResource("xsd-common/marcaDaBollo.xsd");

      if (paForNodeUrl == null || marcaDaBolloUrl == null) {
        throw new IllegalArgumentException("XSD files not found in classpath");
      }

      Source[] schemaSources =
          new Source[] {
            new StreamSource(paForNodeUrl.openStream(), paForNodeUrl.toExternalForm()),
            new StreamSource(marcaDaBolloUrl.openStream(), marcaDaBolloUrl.toExternalForm())
          };

      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file, jar:file");
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);

      return schemaFactory.newSchema(schemaSources);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load marcaDaBollo.xsd schema", e);
    }
  }
}
