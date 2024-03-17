package com.hashmanager.hashmanager.controller;

import com.hashmanager.hashmanager.api.dto.HashCrackTaskDTO;
import com.hashmanager.hashmanager.api.dto.SimpleIdentity;
import com.hashmanager.hashmanager.api.dto.StatusDTO;
import com.hashmanager.hashmanager.service.HashService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.paukov.combinatorics3.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api")

public class HashController {
    private static final Logger logger = LoggerFactory.getLogger(HashController.class);
    private HashService hashService;
    public HashController(HashService hashService1)
    {
        hashService = hashService1;
    }
    @PostMapping(value = "/hash/crack")
    public SimpleIdentity getHash(@Valid @RequestBody HashCrackTaskDTO task)
    {
        logger.debug("Отправка задачи взлома хэша : {}", task);
        SimpleIdentity identity = hashService.startHashCrackTask(task);
        logger.debug("Задача с идентификатором {} создана успешно", identity.getRequestId());
        return identity;
    }

    @GetMapping(value = "/hash/status")
    public StatusDTO getTaskStatus(@Valid @NotNull @PositiveOrZero @RequestParam(name = "requestId", required = true) Long requestId)
    {
        return hashService.getTaskStatus(requestId);
    }
}