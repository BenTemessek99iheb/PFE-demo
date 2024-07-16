package org.acme.dto;

import jakarta.ws.rs.GET;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private Long userId;
    private List<DeviceData> devices;
}
