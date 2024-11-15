package wam.automationtool.controller;

import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_BASE_PATH;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeAddRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeResponseDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeUpdateRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypesResponseDto;
import wam.automationtool.application.impl.parameter.PreferenceParameterTypeService;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "preferenceparametertypes")
@RequiredArgsConstructor
public final class PreferenceParameterTypeController {

  private final PreferenceParameterTypeService preferenceParameterTypeService;

  @PostMapping
  public ResponseEntity<Void> addPreferenceParameterType(
      @RequestBody @Valid
          final PreferenceParameterTypeAddRequestDto preferenceParameterTypeAddRequestDto) {
    preferenceParameterTypeService.addPreferenceParameterType(preferenceParameterTypeAddRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<PreferenceParameterTypesResponseDto> getPreferenceParameterTypes() {
    return ResponseEntity.ok(
            preferenceParameterTypeService.getPreferenceParameterTypes());
  }

  @GetMapping("/{preferenceparametertype-id}")
  public ResponseEntity<PreferenceParameterTypeResponseDto> getPreferenceParameterType(
      @PathVariable("preferenceparametertype-id") long preferenceParameterTypeId) {
    return ResponseEntity.ok(
        preferenceParameterTypeService.getPreferenceParameterType(preferenceParameterTypeId));
  }

  @PutMapping("/{preferenceparametertype-id}")
  public ResponseEntity<Void> updatePreferenceParameterType(
      @PathVariable("preferenceparametertype-id") long preferenceParameterTypeId,
      @RequestBody @Valid
          final PreferenceParameterTypeUpdateRequestDto preferenceParameterTypeUpdateRequestDto) {
    preferenceParameterTypeService.updatePreferenceParameterType(
        preferenceParameterTypeId, preferenceParameterTypeUpdateRequestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{preferenceparametertype-id}")
  public ResponseEntity<Void> deletePreferenceParameterType(
      @PathVariable("preferenceparametertype-id") long preferenceParameterTypeId) {
    preferenceParameterTypeService.deletePreferenceParameterType(preferenceParameterTypeId);
    return ResponseEntity.noContent().build();
  }
}
