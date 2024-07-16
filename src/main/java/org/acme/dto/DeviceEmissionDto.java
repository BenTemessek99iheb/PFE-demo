package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceEmissionDto {
    private String deviceName;
    private double allowableConsumption;
    private double totalConsumption;
    private double carbonEmission;

}
