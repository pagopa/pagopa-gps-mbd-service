@jakarta.xml.bind.annotation.XmlSchema(
    namespace = "http://www.agenziaentrate.gov.it/2014/MarcaDaBollo",
    elementFormDefault = jakarta.xml.bind.annotation.XmlNsForm.QUALIFIED,
    xmlns = {
      @jakarta.xml.bind.annotation.XmlNs(
          prefix = "mdb",
          namespaceURI = "http://www.agenziaentrate.gov.it/2014/MarcaDaBollo"),
      @jakarta.xml.bind.annotation.XmlNs(
          prefix = "ds",
          namespaceURI = "http://www.w3.org/2000/09/xmldsig#"),
      @jakarta.xml.bind.annotation.XmlNs(
          prefix = "common",
          namespaceURI = "http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/"),
      @jakarta.xml.bind.annotation.XmlNs(
          prefix = "tns",
          namespaceURI = "http://pagopa-api.pagopa.gov.it/pa/paForNode.xsd")
    })
package it.gov.pagopa.mbd.gps.service.model.marcadabollo;
