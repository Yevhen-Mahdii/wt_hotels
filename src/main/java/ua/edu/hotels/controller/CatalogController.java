package ua.edu.hotels.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.hotels.data.dto.CatalogResponse;
import ua.edu.hotels.service.CatalogService;

/**
 * Exposes public display metadata so the frontend does not duplicate enum labels.
 */
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {
  private final CatalogService catalogService;

  @GetMapping
  public CatalogResponse catalog() {
    return catalogService.catalog();
  }
}
