package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.model.devices.EdeviceType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceConsumption_cout_carbone {
    private String deviceName;
    private EdeviceType deviceType;
    private String deviceZone;
    private String deviceEspace;
    private double consumption;
    private double cout;
    private double carboneFootprint;
}
