package wam.automationtool.controller;

import wam.automationtool.application.dto.home.HomeDetailsResponseDto;
import wam.automationtool.application.impl.home.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_BASE_PATH;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "homedetails")
@RequiredArgsConstructor
public final class HomeController {

  private final HomeService homeService;

  @GetMapping
  public ResponseEntity<HomeDetailsResponseDto> getHomeDetails() {
    return ResponseEntity.ok(homeService.getHomeDetails());
  }
}
