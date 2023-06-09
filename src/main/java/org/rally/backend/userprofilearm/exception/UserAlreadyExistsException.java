package org.rally.backend.userprofilearm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User Name is already taken!")
public class UserAlreadyExistsException extends RuntimeException{
}
