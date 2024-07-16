package org.acme.service;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.Alert;
import org.acme.model.devices.Device;
import org.acme.model.devices.ElectricityMeter;
import org.acme.repository.AlertRepo;
import org.acme.repository.devices.DeviceRepository;
import org.acme.util.tools.Exceptions.DeviceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped

public class AlertScheduler {
    @Inject
    DeviceRepository deviceRepo;

    @Inject
    AlertRepo alertRepo;

    @Inject
    MetaDataService metadataService;
    @Inject
    AlertWebSocket alertWebSocket;


    private void triggerAlert(String alert) {

        alertWebSocket.sendMessage("Alert triggered: " + alert);
    }
    private void triggerAlerts(List<Alert> alert) {
        String ids = "";
        for (Alert a : alert) {
            ids += a.getId() + ",";
        }

        alertWebSocket.sendMessage(ids);
    }


    @Scheduled(every = "15m")
    @Transactional
    public void checkAlerts() {
        Log.info("Checking alerts...");
        List<Alert> alerts = alertRepo.listAll();
        List<Alert> alertsToSend = new ArrayList<>();
        String ids = "";

        for (Alert alert : alerts) {
            Log.info("Scheduling alert: " + alert);
           if (scheduleAlert(alert.getDevice().getId(), alert)){
               alertsToSend.add(alert);

            }
        }
            triggerAlerts(alertsToSend);
    }
    public Boolean scheduleAlert(Long userId, Alert alert) {

        List<Device> devices = deviceRepo.getDevicesByUserId(userId);
        for (Device device : devices) {
            if (device instanceof ElectricityMeter) {
                switch (alert.getAttribute()) {
                    case "totalConsumption":
                        if (checkTotalConsumptionAlert(alert, device.getId())) {
                         //  triggerAlert(alert); // Trigger alert when condition met
                            return true;
                        }
                        break;
                    case "activePower":
                        if (checkActivePowerAlert(alert, device.getId())) {
                       //     triggerAlert(alert); // Trigger alert when condition met
                            return true;
                        }
                        break;
                    case "powerFactor":
                        if (checkPowerFactorAlert(alert, device.getId())) {
                     //       triggerAlert(alert); // Trigger alert when condition met
                            return true;
                        }
                        break;
                    default:
                        throw new DeviceNotFoundException("Attribute not found with ID: " + alert.getAttribute());
                }
            }
        }
        return false;
    }

    private boolean checkTotalConsumptionAlert(Alert alert, Long deviceId) {
        double consumption = metadataService.calculateTotalEnergyConsumptionFromStartDate(deviceId, alert.getDatedebut());
        return consumption > Double.parseDouble(alert.getValue());
    }

    private boolean checkActivePowerAlert(Alert alert, Long deviceId) {
        double activePower = metadataService.calculateTotalActivePowerFromStartDate(deviceId, alert.getDatedebut());
        return activePower > Double.parseDouble(alert.getValue());
    }

    private boolean checkPowerFactorAlert(Alert alert, Long deviceId) {
        double powerFactor = metadataService.calculateTotalPowerFactorFromStartDate(deviceId, alert.getDatedebut());
        return powerFactor < Double.parseDouble(alert.getValue());
    }


}
