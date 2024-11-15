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
import wam.automationtool.application.dto.alias.AliasParameterTypeAddRequestDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeResponseDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeUpdateRequestDto;
import wam.automationtool.application.dto.alias.AliasParameterTypesResponseDto;
import wam.automationtool.application.impl.alias.AliasParameterTypeService;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "aliasparametertypes")
@RequiredArgsConstructor
public final class AliasParameterTypeController {

  private final AliasParameterTypeService aliasParameterTypeService;

  @PostMapping
  public ResponseEntity<Void> addAliasParameterType(
      @RequestBody @Valid
          final AliasParameterTypeAddRequestDto aliasParameterTypeAddRequestDto) {
    aliasParameterTypeService.addAliasParameterType(aliasParameterTypeAddRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<AliasParameterTypesResponseDto> getAliasParameterTypes() {
    return ResponseEntity.ok(
            aliasParameterTypeService.getAliasParameterTypes());
  }

  @GetMapping("/{aliasparametertype-id}")
  public ResponseEntity<AliasParameterTypeResponseDto> getAliasParameterType(
      @PathVariable("aliasparametertype-id") long aliasParameterTypeId) {
    return ResponseEntity.ok(
        aliasParameterTypeService.getAliasParameterType(aliasParameterTypeId));
  }

  @PutMapping("/{aliasparametertype-id}")
  public ResponseEntity<Void> updateAliasParameterType(
      @PathVariable("aliasparametertype-id") long aliasParameterTypeId,
      @RequestBody @Valid
          final AliasParameterTypeUpdateRequestDto aliasParameterTypeUpdateRequestDto) {
    aliasParameterTypeService.updateAliasParameterType(
        aliasParameterTypeId, aliasParameterTypeUpdateRequestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{aliasparametertype-id}")
  public ResponseEntity<Void> deleteAliasParameterType(
      @PathVariable("aliasparametertype-id") long aliasParameterTypeId) {
    aliasParameterTypeService.deleteAliasParameterType(aliasParameterTypeId);
    return ResponseEntity.noContent().build();
  }
}
