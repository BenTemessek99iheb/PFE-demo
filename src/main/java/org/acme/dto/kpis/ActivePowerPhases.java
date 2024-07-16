package org.acme.dto.kpis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivePowerPhases {
    private Long deviceId;

    private Double active_power_L1;
    private Double active_power_L2;
    private Double active_power_L3;
    private Date date ;
}
