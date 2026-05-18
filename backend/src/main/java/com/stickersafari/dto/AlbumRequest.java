package com.stickersafari.dto;

import com.stickersafari.enums.AlbumStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AlbumRequest {
    @NotBlank(message = "Album name is required")
    private String name;
    
    private AlbumStatus status;
    
    private List<Long> stickerIds;
}
