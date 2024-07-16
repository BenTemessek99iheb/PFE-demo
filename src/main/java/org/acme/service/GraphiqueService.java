package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.Client;
import org.acme.model.charts.EGraphiqueType;
import org.acme.model.charts.Graphique;

import org.acme.model.devices.Device;
import org.acme.repository.charts.*;
import org.acme.repository.devices.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Transactional
@ApplicationScoped

public class GraphiqueService {

    @Inject
    GraphiqueRepository graphiqueRepository;

    @Inject
    DeviceRepository deviceRepository;

    private static final Logger logger = LoggerFactory.getLogger(GraphiqueService.class);


    public List<Graphique> getGraphiquesByDevice(Long deviceId) {
        return graphiqueRepository.find("device.id", deviceId).list();
    }

    @Transactional
    public List<Graphique> getGraphiquesByUserId(Long userId) {
        return graphiqueRepository.getGraphiquesByUserId(userId);
    }

/*    @Transactional
    public Map<String, List<Graphique>> getGraphiquesByDeviceId(Long deviceId) {
        return graphiqueRepository.getGraphiqueTypesByDeviceId(deviceId);
    }*/
    @Transactional
    public Map<EGraphiqueType, List<Graphique>> getGraphiqueTypesByDeviceId(Long deviceId) {
        Map<EGraphiqueType, List<Graphique>> graphiquesByType = new HashMap<>();

        List<Graphique> graphiques = graphiqueRepository.fetchGraphiquesByDeviceId(deviceId);

        for (Graphique graphique : graphiques) {
            EGraphiqueType type = graphique.getType();
            graphiquesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(graphique);
        }

        return graphiquesByType;
    }
    @Transactional
    public Map<EGraphiqueType, List<Graphique>> GraphiquesAndTypesByUserId(Long deviceId) {
        return graphiqueRepository.getGraphiquesAndTypesByUserId(deviceId);
    }
    @Transactional
    public List<Graphique> getGraphiquesByUserId2(Long userId) {
        return graphiqueRepository.getGraphiquesByUserId2(userId);
    }
    @Transactional
    public void deleteGraphiqueById(Long id) {
        graphiqueRepository.deleteById(id);
    }
    @Transactional
    public void deleteAll() {
        graphiqueRepository.deleteAll();
    }

    @Transactional
    public void saveGraphique(Graphique graphique, List<Long> deviceIds) {
        // Log the received parameters
        logger.info("Received LineChart: {}", graphique);
        logger.info("Received Device IDs: {}", deviceIds);

        // Find devices by IDs and set them to the graphique
        Set<Device> devices = new HashSet<>(deviceRepository.findAllById(deviceIds));
        graphique.setDevices(devices); // Assuming LineChart has a setDevices method

        // Log details about the devices found
        logger.info("Devices found: {}", devices);

        // Get the client associated with the devices and set it to the graphique
        Client client = deviceRepository.getClientIdByDevicesIds(deviceIds);
        graphique.setClient(client);

        // Log details about the client
        logger.info("Client found: {}", client);

        // Ensure the bi-directional relationship is maintained
        for (Device device : devices) {
            if (device.getGraphiques() == null) {
                device.setGraphiques(new HashSet<>());
            }
            if (!device.getGraphiques().contains(graphique)) {
                device.getGraphiques().add(graphique);
            }
        }

        // Log details before persisting
        logger.info("Saving graphique: Title: {}, Subtitle: {}, YAxisItems: {}, Devices: {}",
               graphique.getType(), graphique.getTitle(), graphique.getSubtitle(), graphique.getYaxisitems(), deviceIds);

        // Persist the graphique
        graphiqueRepository.persist(graphique);
    }
 /*   @Transactional
    public Graphique saveGraphique(Graphique graphique, List<Long> deviceIds) {
        Set<Device> devices = new HashSet<>();
        for (Long deviceId : deviceIds) {
            Device device = deviceRepository.findById(deviceId);
            if (device != null) {
                devices.add(device);
            }
        }
        graphique.setDevices(devices);

        graphiqueRepository.persist(graphique);
        return graphique;
    }*/

    // get graphiques by clientId
    @Transactional
    public List<Graphique> getGraphiqueByClientId(Long userId) {
        List<Graphique> graphiques = graphiqueRepository.getGraphiqueByClientId(userId);

        if (graphiques.isEmpty()) {
            logger.warn("No graphiques found for client with ID: {}", userId);
        }

        return graphiques;
    }
//saveGraphique
  @Transactional
    public Graphique saveGraphique (Graphique graphique){
       graphiqueRepository.persist(graphique);
       return graphique;
    }
}
