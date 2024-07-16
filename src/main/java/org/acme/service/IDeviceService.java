package org.acme.service;

import jakarta.ws.rs.core.Response;
import org.acme.model.devices.Device;

import java.util.List;

public interface IDeviceService  {
    public List<Device> getDevices() ;
    public Response create(Device device) ;
    public Device update(Long id, Device device) ;
    public Response delete(Long id) ;


    }
