package com.hashmanager.hashmanager.exception.mapper;

import lombok.AllArgsConstructor;

public class OutboundSerialisationException extends RuntimeException{
    public OutboundSerialisationException(Exception cause)
    {
        super(cause);
    }
}
