package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceCountDto {
    public Long deviceCount;
    public Long thlCount;
    public Long energyMeterCount;
    public Long waterMeterCount;
    public Long solarPanelCount ;
    public Long electricityMeterCount;
    public Long clientCount;
}
