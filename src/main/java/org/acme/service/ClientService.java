package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.model.Client;
import org.acme.model.ClientDevice;
import org.acme.model.devices.*;
import org.acme.repository.ClientDeviceRepo;
import org.acme.repository.ClientRepo;
import org.acme.repository.devices.DeviceRepository;

import java.util.List;

@Transactional
@ApplicationScoped
public class ClientService {
    @Inject
    ClientRepo clientRepo;

    @Inject
    DeviceRepository deviceRepo;

    @Inject
    ClientDeviceRepo clientDeviceRepo;

    public Client save(Client client) {
        clientRepo.persist(client);
        return client;
    }

    @Transactional
    public void assignEmToUser(Client client, ElectricityMeter device) {
        deviceRepo.persist(device); // Save the device first

        ClientDevice clientDevice = new ClientDevice();
        clientDevice.setClient(client);
        clientDevice.setDevice(device);
        clientDevice.setDeviceType(EdeviceType.ElectricityMeter);
        clientDeviceRepo.persist(clientDevice);
    }
    @Transactional
    public void assignSolarPanelToUser(Client client, SolarPanel device) {
        deviceRepo.persist(device); // Save the device first

        ClientDevice clientDevice = new ClientDevice();
        clientDevice.setClient(client);
        clientDevice.setDevice(device);
        clientDevice.setDeviceType(EdeviceType.SolarPanel);
        clientDeviceRepo.persist(clientDevice);
    }

    @Transactional
    public void assignWmToUser(Client client, WaterMeter device) {
        deviceRepo.persist(device); // Save the device first

        ClientDevice clientDevice = new ClientDevice();
        clientDevice.setClient(client);
        clientDevice.setDevice(device);
        clientDevice.setDeviceType(EdeviceType.WaterMeter);
        clientDeviceRepo.persist(clientDevice);
    }

    @Transactional
    public void assignTHLToUser(Client client, THL device) {
        deviceRepo.persist(device); // Save the device first

        ClientDevice clientDevice = new ClientDevice();
        clientDevice.setClient(client);
        clientDevice.setDevice(device);
        clientDevice.setDeviceType(EdeviceType.THL);
        clientDeviceRepo.persist(clientDevice);
    }

    public Client findUserById(Long userId) {

        return clientRepo.findById(userId);
    }

    public Client saveClient(Client client) {
        clientRepo.persist(client);
        return client;
    }

    public List<Client> getAllClients() {
        return clientRepo.listAll();
    }
    //getClientsWithDevices
    public List<ClientDevice> getClientsWithDevices() {
        return clientDeviceRepo.getClientsWithDevices();
    }
    @Transactional
    public void deleteClientAndDevice(Long clientId, Long deviceId) {
        // Delete the relationship in ClientDevice
        clientDeviceRepo.deleteClientDevice(clientId, deviceId);

        // Delete the device itself if needed
        Device device = deviceRepo.findById(deviceId);
        if (device != null) {
            deviceRepo.delete(device);
        }

        // Delete the client if needed
        Client client = clientRepo.findById(clientId);
        if (client != null) {
            clientRepo.delete(client);
        }
    }
    public Response deleteUser(Long userId) {
        Client client = clientRepo.findById(userId);
        if (client == null) {
            throw new WebApplicationException("Device with id of " + userId + " does not exist.", 404);
        }
        clientRepo.delete(client);
        return Response.status(204).build();
    }


}
