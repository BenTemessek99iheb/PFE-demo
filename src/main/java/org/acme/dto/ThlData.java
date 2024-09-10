package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThlData {
    public String deviceName;
    public double temperature;
    public double humidity;
    public double luminosity;
    public Date date;

}
