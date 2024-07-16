package org.acme.dto.kpis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PowerFactorPhases {
    private Long deviceId;
    private Double power_factor_L1;
    private Double power_factor_L2;
    private Double power_factor_L3;
    private Double power_factor_L4;
    private Double power_factor_L5;
    private Double power_factor_L6;
    public Date date;


}
