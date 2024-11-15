package wam.automationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wam.automationtool.application.dto.user.type.UserTypeAddRequestDto;
import wam.automationtool.application.dto.user.type.UserTypesResponseDto;
import wam.automationtool.application.impl.user.type.UserTypeService;
import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_BASE_PATH;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "usertypes")
@RequiredArgsConstructor
public final class UserTypeController {

  private final UserTypeService userTypeService;

  @PostMapping
  public ResponseEntity<Void> addUserType(
      @RequestBody @Valid final UserTypeAddRequestDto userTypeAddRequestDTO) {
    userTypeService.addUserType(userTypeAddRequestDTO);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<UserTypesResponseDto> getUserTypes() {
    return ResponseEntity.ok(userTypeService.getUserTypes());
  }
}
