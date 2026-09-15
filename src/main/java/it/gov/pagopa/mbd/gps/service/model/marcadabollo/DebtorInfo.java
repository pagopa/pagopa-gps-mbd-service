package it.gov.pagopa.mbd.gps.service.model.marcadabollo;

import it.gov.pagopa.mbd.gps.service.annotation.ValidEntityUniqueIdentifier;
import it.gov.pagopa.mbd.gps.service.model.partner.CtEntityUniqueIdentifier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    propOrder = {"uniqueIdentifier", "fullName", "email"})
@ValidEntityUniqueIdentifier
public class DebtorInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  /** {@code tns:ctEntityUniqueIdentifier} from {@code paForNode.xsd}. */
  @XmlElement(name = "uniqueIdentifier", required = true)
  @NotNull(message = "Debtor unique identifier is required")
  private CtEntityUniqueIdentifier uniqueIdentifier;

  /** {@code common:stText70} - debtor full name. */
  @XmlElement(name = "fullName", required = true)
  @NotBlank(message = "Debtor full name must be not empty")
  private String fullName;

  /** {@code common:stEMail} - debtor e-mail. */
  @XmlElement(name = "email", required = true)
  @NotBlank(message = "Email must be not empty")
  @Pattern(
      regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
      message = "Invalid debtor email format")
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
