package org.acme.model.MetaData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.acme.model.devices.Device;
import org.acme.model.devices.ElectricityMeter;
import org.acme.model.devices.THL;

import java.io.Serializable;
import java.util.Date;

@Entity
@Data
public class THLMetaData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private THL thl;
    private Date date;
    // Temperature value recorded by the device
    private Double temperature;

    // Humidity level in the environment
    private Double humidity;

    // Intensity of light measured by the device
    private Double light;

    // Index of particulate matter present in the air
    private Double particulate_index;

    // Average power consumption recorded by the device
    private Double average_power;

    // Minimum power consumption recorded by the device
    private Double minimum_power;

    // Maximum power consumption recorded by the device
    private Double maximum_power;

    // Received Signal Strength Indication, representing signal strength
    private Integer rssi;

    // Signal-to-Noise Ratio, representing signal quality
    private Integer snr;

    // Level of charge remaining in the device's battery
    private Integer battery_level;

    // Counter or cumulative value associated with certain events
    private Integer counter;

    // Number of alert events triggered by the device
    private Integer number_of_alerts;

    // Identifier or unique key associated with the gateway device
    private Integer gateway;

    // Indicates whether the device is currently powered
    private Boolean powered;

    // Represents the state of the device (on or off)
    private Boolean on_off;

}
