package it.gov.pagopa.mbd.gps.service.model.marcadabollo;

import it.gov.pagopa.mbd.gps.service.model.partner.CtEntityUniqueIdentifier;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Java model generated from {@code marcaDaBollo.xsd} (complex type {@code debtorInfo}).
 *
 * <p>Holds the debtor personal data.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "debtorInfo",
    propOrder = {"uniqueIdentifier", "fullName", "province", "email"})
public class DebtorInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  /** {@code tns:ctEntityUniqueIdentifier} from {@code paForNode.xsd}. */
  @XmlElement(name = "uniqueIdentifier", required = true)
  private CtEntityUniqueIdentifier uniqueIdentifier;

  /** {@code common:stText70} - debtor full name. */
  @XmlElement(name = "fullName", required = true)
  private String fullName;

  /** {@code common:stNazioneProvincia} - two-letter province code. */
  @XmlElement(name = "province", required = true)
  private String province;

  /** {@code common:stEMail} - optional debtor e-mail. */
  @XmlElement(name = "email")
  private String email;

  public CtEntityUniqueIdentifier getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  public void setUniqueIdentifier(CtEntityUniqueIdentifier uniqueIdentifier) {
    this.uniqueIdentifier = uniqueIdentifier;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
