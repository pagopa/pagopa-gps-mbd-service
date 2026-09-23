package it.gov.pagopa.mbd.gps.service.model.enumeration;

import lombok.Getter;

@Getter
public enum AppErrorCode {
  PPA_SINTASSI_EXTRAXSD("PAA_SINTASSI_EXTRAXSD"),
  PAA_ID_DOMINIO_ERRATO("PAA_ID_DOMINIO_ERRATO"),
  PAA_SYSTEM_ERROR("PAA_SYSTEM_ERROR");

  private final String code;

  AppErrorCode(String code) {
    this.code = code;
  }
}
