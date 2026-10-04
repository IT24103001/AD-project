package com.ridelink.account.dto;

import com.ridelink.account.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @Schema(example = "SUSPENDED", allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
        @NotNull(message = "Status is required (ACTIVE, INACTIVE or SUSPENDED)")
        AccountStatus status) {
}
