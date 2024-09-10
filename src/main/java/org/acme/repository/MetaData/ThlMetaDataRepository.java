package org.acme.repository.MetaData;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.MetaData.THLMetaData;
import org.acme.model.devices.Device;
import org.acme.model.devices.ElectricityMeter;
import org.acme.model.devices.THL;
import org.acme.repository.ClientDeviceRepo;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped

public class ThlMetaDataRepository implements PanacheRepositoryBase<THLMetaData, Long> {
    @Inject
    ClientDeviceRepo clientDeviceRepo;

    @Transactional
    public List<THLMetaData> findByDeviceId(Long deviceId) {

        return find("thl.id", deviceId).list();
    }
    public List<THLMetaData> getTHLMetaDataByUserId(Long userId) {
        List<THLMetaData> allMetaData = new ArrayList<>();
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);

        for (Device device : devices) {
            if (device instanceof THL) {
                List<THLMetaData> deviceMetaData = findByDeviceId(device.getId());
                allMetaData.addAll(deviceMetaData);
            }
        }
        return allMetaData;
    }
    public int countTHLByUserId(Long userId) {
        List<Device> devices = clientDeviceRepo.devicesByUserId(userId);
        int count = 0;
        for (Device device : devices) {
            if (device instanceof THL) {
                count++;
            }
        }
        return count;
    }

}
