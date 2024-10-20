package wam.automationtool.application.exception;

import static wam.automationtool.application.config.AppConstant.AuthConstants.REQUEST_FIELD_VALIDATION_CODE;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import wam.automationtool.application.config.AppConstant;
import wam.automationtool.application.config.ResourceMessages;
import wam.automationtool.application.dto.ErrorResponse;

@ControllerAdvice
@Slf4j
public final class GlobalErrorHandler extends ResponseEntityExceptionHandler {

  /**
   * Error response returned to the front end with the error list.
   *
   * @param commonErrorsException Exception with error and fail code.
   * @return ErrorResponse containing error list and codes.
   */
  private final ResourceMessages resourceMessages;

  public GlobalErrorHandler(final ResourceMessages resourceMessages) {

    this.resourceMessages = resourceMessages;
  }

  @ExceptionHandler({UserTypeExistException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleUserTypeExistException(
      final UserTypeExistException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    final ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();
    log.error("UserTypeExistException: {}", errorMessage);
    return new ResponseEntity<>(errorResponse, exception.getStatus());
  }

  @ExceptionHandler({UserException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleUserException(final UserException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    final ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();
    log.error("UserException: {}", errorMessage);
    return new ResponseEntity<>(errorResponse, exception.getStatus());
  }

  @ExceptionHandler({PasswordMismatchedException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handlePasswordMismatchedException(
      final PasswordMismatchedException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    final ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();
    log.error("PasswordMismatchedException: {}", errorMessage);
    return new ResponseEntity<>(errorResponse, exception.getStatus());
  }

  @ExceptionHandler({ConstraintViolationException.class})
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      final ConstraintViolationException exception) {
    final List<CustomError> errorList =
        exception.getConstraintViolations().stream()
            .map(violation -> new CustomError(violation.getMessage()))
            .collect(Collectors.toList());
    final ErrorResponse errorResponse =
        ErrorResponse.builder().code(REQUEST_FIELD_VALIDATION_CODE).errors(errorList).build();
    log.error("ConstraintViolationException: {}", exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({InvalidCredentialsException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
      final InvalidCredentialsException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    final ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();
    log.error("InvalidCredentialsException: {}", errorMessage);
    return new ResponseEntity<>(errorResponse, exception.getStatus());
  }

  @ExceptionHandler({ServicePermissionException.class})
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ErrorResponse> handleServicePermissionException(
      final ServicePermissionException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    final ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();
    log.error("ServicePermissionException: {}", errorMessage);
    return new ResponseEntity<>(errorResponse, exception.getStatus());
  }

  @ExceptionHandler({AuthTokenValidationException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorResponse> handleAuthTokenValidationException(
      final AuthTokenValidationException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    final ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();
    log.error("AuthTokenValidationException: {}", errorMessage);
    return new ResponseEntity<>(errorResponse, exception.getStatus());
  }

  @ExceptionHandler({AuthTokenMissingException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorResponse> handleAuthTokenMissingException(
      final AuthTokenMissingException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    final ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();
    log.error("AuthTokenMissingException: {}", errorMessage);
    return new ResponseEntity<>(errorResponse, exception.getStatus());
  }

  @ExceptionHandler({CommonErrorsException.class})
  public ResponseEntity<ErrorResponse> handleCommonErrors(
      final CommonErrorsException commonErrorsException) {

    ArrayList<CustomError> list = new ArrayList<>();

    commonErrorsException
        .getErrors()
        .forEach(
            customError -> {
              CustomError customErrorMessage =
                  new CustomError(resourceMessages.getErrorMessage(customError.getMessage()));
              list.add(customErrorMessage);
            });

    ErrorResponse errorResponse =
        ErrorResponse.builder().code(commonErrorsException.getCode()).errors(list).build();
    log.error("Error {}", commonErrorsException.getErrors());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleRunTimeErrors(
      final RuntimeException runtimeException) {
    ArrayList<CustomError> list = new ArrayList<>();
    list.add(new CustomError(resourceMessages.getErrorMessage("internal.error.occurred")));
    ErrorResponse errorResponse =
        ErrorResponse.builder().code(AppConstant.AuthConstants.FAILED_CODE).errors(list).build();
    log.error("", runtimeException);
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({ResponseStatusException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleUsageExceptions(
      final ResponseStatusException exception) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(String.valueOf(exception.getStatusCode()))
            .errors(
                Collections.singletonList(
                    new CustomError(resourceMessages.getErrorMessage(exception.getReason()))))
            .build();
    return new ResponseEntity<>(errorResponse, exception.getStatusCode());
  }

  @ExceptionHandler({ValidationErrorException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleValidationErrorException(
      final ValidationErrorException validationErrorException) {
    ArrayList<CustomError> list = new ArrayList<>();
    list.add(
        new CustomError(resourceMessages.getErrorMessage(validationErrorException.getMessage())));
    ErrorResponse errorResponse = ErrorResponse.builder().code("").errors(list).build();
    log.error("Error {}", validationErrorException.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({CustomResponseStatusException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleCustomResponseStatusException(
      final CustomResponseStatusException exception) {
    final String errorMessage = resourceMessages.getErrorMessage(exception.getMsgKey());
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(exception.getCode())
            .errors(Collections.singletonList(new CustomError(errorMessage)))
            .build();

    log.error("Custom Response Status error {}", errorMessage);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({InvalidFormatException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleInvalidFormatException(
      final InvalidFormatException exception) {
    String fieldName = exception.getPath().get(0).getFieldName();
    StringBuilder msgBuilder = new StringBuilder();
    if (StringUtils.hasText(exception.getValue().toString())) {
      msgBuilder
          .append(exception.getValue().toString())
          .append(" is invalid for ")
          .append(fieldName);
    } else {
      msgBuilder.append(fieldName).append(" cannot be blank");
    }

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .errors(Collections.singletonList(new CustomError(msgBuilder.toString())))
            .build();
    log.error("Invalid format error {}", exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleInvalidPathParamExceptions(
      final MethodArgumentTypeMismatchException exception) {

    String pathParameterName = exception.getName();

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .errors(
                Collections.singletonList(new CustomError(pathParameterName.concat(" is invalid"))))
            .build();
    log.error("Argument mismatch error {}", exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<ErrorResponse> handleAll(final Exception ex) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .errors(Collections.singletonList(new CustomError(ex.getMessage())))
            .build();
    log.error("", ex);
    return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }
}
