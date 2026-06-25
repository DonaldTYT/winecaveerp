package com.kikyosoft.graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CategoryGraphQLController {
  private final CategoryService svc;
  public CategoryGraphQLController(CategoryService svc) { this.svc = svc; }

  @QueryMapping
  public List<CategoryService.Category> categories() { return svc.list(); }

  @QueryMapping
  public CategoryService.Category categoryBySlug(@Argument String slug) {
    return svc.getBySlug(slug);
  }
}
