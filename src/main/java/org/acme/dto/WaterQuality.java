package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaterQuality {
    private String deviceName;
    private double consommationEau;
    private double fluxDirecte;
    private double fluxInverse;
    private double uptime;
    private double tamp;
    private double carbonFootprint;
}
