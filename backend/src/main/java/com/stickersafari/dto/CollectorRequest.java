package com.stickersafari.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollectorRequest {
    @NotBlank(message = "Username is required")
    private String username;
}
