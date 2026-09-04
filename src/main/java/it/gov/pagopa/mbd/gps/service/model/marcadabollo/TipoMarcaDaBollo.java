package it.gov.pagopa.mbd.gps.service.model.marcadabollo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Java model generated from {@code marcaDaBollo.xsd} (complex type {@code tipoMarcaDaBollo}).
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
  private BigDecimal amount;

  @XmlElement(name = "debtor", required = true)
  private DebtorInfo debtor;

  /** {@code common:stFiscalCodePA} - Creditor Institution fiscal code (11 digits). */
  @XmlElement(name = "fiscalCode", required = true)
  private String fiscalCode;

  /** {@code mbd:signature} - base64-encoded document digest (44 characters). */
  @XmlElement(name = "documentHash", required = true)
  @XmlSchemaType(name = "base64Binary")
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
