package com.hashmanager.hashmanager.exception.persistence;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Объект с таким идентификатором не найден")
public class EntityNotFoundException extends RuntimeException{
}