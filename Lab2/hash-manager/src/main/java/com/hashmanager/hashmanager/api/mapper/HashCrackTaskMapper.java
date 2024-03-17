package com.hashmanager.hashmanager.api.mapper;

import com.hashmanager.hashmanager.api.dto.HashCrackTaskDTO;
import com.hashmanager.hashmanager.api.dto.StatusDTO;
import com.hashmanager.hashmanager.api.entity.DiapasonPartEntity;
import com.hashmanager.hashmanager.api.entity.ExecutorServicePartEntity;
import com.hashmanager.hashmanager.api.entity.HashCrackTaskEntity;
import hashapi.api.DiapasonPartDTO;
import hashapi.api.ExecutorServicePartDTO;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class HashCrackTaskMapper {
    @Mapping(target = "parts", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", expression = "java(com.hashmanager.hashmanager.api.entity.HashCrackTaskEntity.Status.IN_PROGRESS)")
    @Mapping(target = "data", ignore = true)
    public abstract HashCrackTaskEntity dtoToEntity(HashCrackTaskDTO dto);
    @Mapping(target = "taskId", source = "entity.parentTask.id")
    @Mapping(target = "hash", source = "hash")
    public abstract ExecutorServicePartDTO prtEntityToDTO(ExecutorServicePartEntity entity, String hash);
    public abstract DiapasonPartDTO diapasonEntityToDTO(DiapasonPartEntity entity);
    public abstract StatusDTO hashTaskToDTO(HashCrackTaskEntity entity);
}
