package org.acme.model.MetaData;

import jakarta.persistence.*;
import lombok.Data;
import org.acme.model.devices.Device;
import org.acme.model.devices.THL;
import org.acme.model.devices.WaterMeter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Data
public class WaterMeterMetaData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private WaterMeter waterMeter;
    private Date date;
    private double forwardFlow;
    private double forwardFlowBack;
    private double reverseFlow;
    private double reverseFlowBack;
    private double tamp;
    private double tampBack;
    private long uptime=0;
    private double consoH;
    private double consoJ;
    private double consoM;
    private double reverseConsoH;
    private double reverseConsoJ;
    private double reverseConsoM;
    private double infractionConsoH;
    private double infractionConsoJ;
    private double infractionConsoM;
    private int alertState;
    private Date startCheckAlertDate;
    private int highConsumptionCounter;
    private int excessiveConsumptionCounter;
    private int lowConsumptionCounter;


}
