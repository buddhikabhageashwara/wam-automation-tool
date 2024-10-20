package wam.automationtool.controller;

import wam.automationtool.application.dto.home.HomeDetailsResponseDto;
import wam.automationtool.application.impl.home.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/wam/automation/home/details")
@RequiredArgsConstructor
public final class HomeController {

  private final HomeService homeService;

  @GetMapping
  public ResponseEntity<HomeDetailsResponseDto> getHomeDetails() {
    return ResponseEntity.ok(homeService.getHomeDetails());
  }
}
