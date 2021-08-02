package com.tanaguru.config;

import com.tanaguru.domain.dto.ErrorDTO;
import com.tanaguru.domain.exception.CustomIllegalStateException;
import com.tanaguru.domain.exception.CustomInvalidArgumentException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.NoSuchElementException;

/**
 * @author rcharre
 */
@ControllerAdvice
public class TanaguruControllerAdvice {
    
    private final Logger LOGGER = LoggerFactory.getLogger(TanaguruControllerAdvice.class);

    @ResponseBody
    @ExceptionHandler(CustomEntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorDTO customEntityNotFoundHandler(CustomEntityNotFoundException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage(),ex.getContent());
    }

    @ResponseBody
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorDTO noSuchElementExceptionHandler(NoSuchElementException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(CustomForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorDTO entityForbidden(CustomForbiddenException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage(), ex.getContent());
    }

    @ResponseBody
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorDTO badCredential(BadCredentialsException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorDTO entityForbidden(HttpClientErrorException.Unauthorized ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(InternalError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorDTO internalError(InternalError ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(CustomInvalidEntityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorDTO customInvalidEntityError(CustomInvalidEntityException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage(), ex.getContent());
    }

    @ResponseBody
    @ExceptionHandler(CustomIllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorDTO customInvalidEntityError(CustomIllegalStateException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage(), ex.getContent());
    }
    
    @ResponseBody
    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorDTO userBlocked(LockedException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage());
    }
    
    @ResponseBody
    @ExceptionHandler(CustomInvalidArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorDTO customInvalidArgumentError(CustomInvalidArgumentException ex) {
        LOGGER.error(ex.getMessage());
        return new ErrorDTO(ex.getMessage(), ex.getContent());
    }
}
