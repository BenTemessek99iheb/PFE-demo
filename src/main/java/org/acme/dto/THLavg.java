package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class THLavg {
    public double avgTemperature;
    public double avgHumidity;
    public double avgLuminosity;
    public int count;
}
