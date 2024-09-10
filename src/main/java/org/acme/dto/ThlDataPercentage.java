package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThlDataPercentage {
    public String deviceName;
    public Date date;
    public Double percentageTemperature;
    public Double percentageHumidity;
    public Double percentageLight;
}
