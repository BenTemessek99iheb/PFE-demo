package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceData {
    private Long deviceId;
    private String deviceType;
    private List<AttributeData> attributes;

}
