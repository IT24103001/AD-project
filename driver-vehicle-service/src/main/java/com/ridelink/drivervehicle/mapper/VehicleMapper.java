package com.ridelink.drivervehicle.mapper;

import com.ridelink.drivervehicle.dto.VehicleRequest;
import com.ridelink.drivervehicle.dto.VehicleResponse;
import com.ridelink.drivervehicle.model.Vehicle;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VehicleMapper {

    /** Vehicle numbers are normalised (trimmed + upper-case) so "cab-1234" and "CAB-1234" count as the same plate. */
    public static String normalizeVehicleNumber(String vehicleNumber) {
        return vehicleNumber.trim().toUpperCase();
    }

    public Vehicle toEntity(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        Instant now = Instant.now();
        vehicle.setCreatedAt(now);
        applyUpdate(vehicle, request);
        return vehicle;
    }

    public void applyUpdate(Vehicle vehicle, VehicleRequest request) {
        vehicle.setDriverId(request.driverId().trim());
        vehicle.setVehicleNumber(normalizeVehicleNumber(request.vehicleNumber()));
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setColor(request.color());
        vehicle.setUpdatedAt(Instant.now());
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getDriverId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt());
    }
}
