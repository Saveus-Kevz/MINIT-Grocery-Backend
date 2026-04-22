// File: com/minimartph/Minit/exceptions/ErrorResponse.java
package com.minimartph.Minit.exceptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private int code;
  private String message;
  private String detail;
  private int status;
  private String error;
  private String path;
  private LocalDateTime timestamp;
  private List<ValidationError> validationErrors;
  private Map<String, Object> additionalInfo;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ValidationError {
    private String field;
    private String message;
    private Object rejectedValue;
  }
}
