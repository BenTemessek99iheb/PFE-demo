package org.acme.dto.kpis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnergyPhases {
    private Long deviceId;

    private Double energy_L1;
    private Double energy_L2;
    private Double energy_L3;
    private Double energy_L4;
    private Double energy_L5;
    private Double energy_L6;
    private Date date;
}
