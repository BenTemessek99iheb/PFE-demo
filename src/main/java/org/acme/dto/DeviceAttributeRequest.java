package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceAttributeRequest {
    private Long deviceId;
    private List<String> attributes;
    @Override
    public String toString() {
        return "DeviceAttributeRequest{" +
                "deviceId=" + deviceId +
                ", attributes=" + attributes +
                '}';
    }

}
