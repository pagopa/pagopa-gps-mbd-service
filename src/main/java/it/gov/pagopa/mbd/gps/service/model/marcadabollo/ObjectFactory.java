package it.gov.pagopa.mbd.gps.service.model.marcadabollo;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * Factory for creating instances of the {@code marcaDaBollo.xsd} generated model, following the
 * JAXB {@code ObjectFactory} convention.
 */
@XmlRegistry
public class ObjectFactory {

  private static final String NAMESPACE = "http://www.agenziaentrate.gov.it/2014/MarcaDaBollo";

  private static final QName MARCA_DA_BOLLO_QNAME = new QName(NAMESPACE, "marcaDaBollo");

  public ObjectFactory() {
    // default constructor required by JAXB
  }

  public TipoMarcaDaBollo createTipoMarcaDaBollo() {
    return new TipoMarcaDaBollo();
  }

  public DebtorInfo createDebtorInfo() {
    return new DebtorInfo();
  }

  @XmlElementDecl(namespace = NAMESPACE, name = "marcaDaBollo")
  public JAXBElement<TipoMarcaDaBollo> createMarcaDaBollo(TipoMarcaDaBollo value) {
    return new JAXBElement<>(MARCA_DA_BOLLO_QNAME, TipoMarcaDaBollo.class, null, value);
  }
}
