package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.VehicleRequest;
import com.ridelink.drivervehicle.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {

    VehicleResponse createVehicle(VehicleRequest request);

    VehicleResponse getVehicleById(String id);

    List<VehicleResponse> getAllVehicles();

    List<VehicleResponse> getVehiclesByDriverId(String driverId);

    VehicleResponse updateVehicle(String id, VehicleRequest request);

    void deleteVehicle(String id);
}
