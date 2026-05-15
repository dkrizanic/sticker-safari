package com.stickersafari.mapper;

import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollectorMapper {
    
    CollectorResponse toResponse(Collector collector);
    
    List<CollectorResponse> toResponseList(List<Collector> collectors);
}
