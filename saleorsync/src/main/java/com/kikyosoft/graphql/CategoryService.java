package com.kikyosoft.graphql;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryService {

  // In-memory demo store
  private final Map<String, Category> bySlug = new HashMap<>();

  public CategoryService() {
    bySlug.put("wine", new Category("1", "Wine", "wine", "All wine", null));
    bySlug.put("red-wine", new Category("2", "Red Wine", "red-wine", null, "1"));
  }

  public List<Category> list() {
    return new ArrayList<>(bySlug.values());
  }

  public Category getBySlug(String slug) {
    return bySlug.get(slug);
  }

  /** Simple POJO for JDK 11 */
  public static class Category {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String parentId;

    public Category() {}
    public Category(String id, String name, String slug, String description, String parentId) {
      this.id = id; this.name = name; this.slug = slug; this.description = description; this.parentId = parentId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getParentId() { return parentId; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setDescription(String description) { this.description = description; }
    public void setParentId(String parentId) { this.parentId = parentId; }
  }
}

