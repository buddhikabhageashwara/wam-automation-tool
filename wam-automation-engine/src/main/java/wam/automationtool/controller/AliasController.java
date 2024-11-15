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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wam.automationtool.application.dto.alias.AliasAddRequestDto;
import wam.automationtool.application.dto.alias.AliasListResponseDto;
import wam.automationtool.application.dto.alias.AliasResponseDto;
import wam.automationtool.application.impl.alias.AliasService;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "alias")
@RequiredArgsConstructor
public final class AliasController {

  private final AliasService aliasService;

  @PostMapping
  public ResponseEntity<Void> addAlias(
      @RequestBody @Valid final AliasAddRequestDto aliasAddRequestDto) {
    aliasService.addAlias(aliasAddRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<AliasListResponseDto> getAliasList() {
    return ResponseEntity.ok(aliasService.getAliasList());
  }

  @GetMapping("/{alias-id}")
  public ResponseEntity<AliasResponseDto> getAlias(
      @PathVariable("alias-id") long aliasId) {
    return ResponseEntity.ok(aliasService.getAlias(aliasId));
  }

  @DeleteMapping("/{alias-id}")
  public ResponseEntity<Void> deleteAlias(@PathVariable("alias-id") long aliasId) {
    aliasService.deleteAlias(aliasId);
    return ResponseEntity.noContent().build();
  }
}
