package org.acme.model.devices;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class THL extends Device {
  //  private EdeviceType deviceType= EdeviceType.THL;
    private String serialNumber;
    private String reference;
    private String model;
    private String manufacturer;
    private Date productionDate;
    private String installationLocation;
    private String firmwareVersion;
    private String hardwareVersion;
}
