package it.gov.pagopa.mbd.gps.service.model.marcadabollo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Hand-maintained Java model mirroring {@code marcaDaBollo.xsd} (complex type {@code
 * tipoMarcaDaBollo}). This class is committed source code, not regenerated at build time; any
 * changes (including validation annotations) must be applied manually and kept in sync with the
 * XSD.
 *
 * <p>Namespace: {@code http://www.agenziaentrate.gov.it/2014/MarcaDaBollo}
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "tipoMarcaDaBollo",
    propOrder = {"amount", "debtor", "fiscalCode", "documentHash"})
public class TipoMarcaDaBollo implements Serializable {

  private static final long serialVersionUID = 1L;

  /** {@code common:stAmount} - decimal amount with two fractional digits. */
  @XmlElement(name = "amount", required = true)
  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
  private BigDecimal amount;

  @XmlElement(name = "debtor", required = true)
  @NotNull(message = "Debtor information is required")
  @Valid
  private DebtorInfo debtor;

  /** {@code common:stFiscalCodePA} - Creditor Institution fiscal code (11 digits). */
  @XmlElement(name = "fiscalCode", required = true)
  @NotBlank(message = "Creditor Institution fiscal code must be not empty")
  @Pattern(
      regexp = "^[0-9]{11}$",
      message = "Creditor Institution fiscal code must be an 11-digit number")
  private String fiscalCode;

  /** {@code mbd:signature} - base64-encoded document digest (44 characters). */
  @XmlElement(name = "documentHash", required = true)
  @XmlSchemaType(name = "base64Binary")
  @NotNull(message = "Document hash is required")
  @Size(min = 32, max = 32, message = "Document hash must be a valid 32-byte SHA-256 digest")
  private byte[] documentHash;


  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public DebtorInfo getDebtor() {
    return debtor;
  }

  public void setDebtor(DebtorInfo debtor) {
    this.debtor = debtor;
  }

  public String getFiscalCode() {
    return fiscalCode;
  }

  public void setFiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
  }

  public byte[] getDocumentHash() {
    return documentHash;
  }

  public void setDocumentHash(byte[] documentHash) {
    this.documentHash = documentHash;
  }
}
