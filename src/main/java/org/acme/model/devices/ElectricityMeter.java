package org.acme.model.devices;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ElectricityMeter extends Device {
    private String serialNumber;
    private String reference;
    private String model;
    private String manufacturer;
    private Date productionDate;
    private String installationLocation;
    private String firmwareVersion;
    private String hardwareVersion;
}
