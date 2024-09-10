package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.acme.model.ClientDevice;
import org.acme.model.devices.Device;

import java.util.List;

@ApplicationScoped
public class ClientDeviceRepo implements PanacheRepositoryBase<ClientDevice, Long> {
    public List<ClientDevice> getDevicesByUserId(Long userId) {
        TypedQuery<ClientDevice> query = getEntityManager().createQuery("" +
                "SELECT cd FROM ClientDevice cd WHERE cd.client.id = :userId", ClientDevice.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    public List<Device> devicesByUserId(Long userId) {
        TypedQuery<Device> query = getEntityManager().createQuery(
                "SELECT cd.device FROM ClientDevice cd WHERE cd.client.id = :userId", Device.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    // getClients with devices

    public List<ClientDevice> getClientsWithDevices() {
        TypedQuery<ClientDevice> query = getEntityManager().createQuery(
                "SELECT cd FROM ClientDevice cd JOIN FETCH cd.client JOIN FETCH cd.device", ClientDevice.class
        );
        return query.getResultList();
    }
    // delete client device
    public void deleteClientDevice(Long clientId, Long deviceId) {
        getEntityManager().createQuery(
                "DELETE FROM ClientDevice cd WHERE cd.client.id = :clientId AND cd.device.id = :deviceId"
        ).setParameter("clientId", clientId).setParameter("deviceId", deviceId).executeUpdate();
    }

}
