package com.stickersafari.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StickerRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Latin name is required")
    private String latinName;
    
    @NotBlank(message = "Habitat is required")
    private String habitat;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Image source is required")
    private String src;
    
    private Long albumId;
}
