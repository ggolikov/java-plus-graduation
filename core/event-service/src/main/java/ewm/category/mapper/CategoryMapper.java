package ewm.category.mapper;

import ewm.category.dto.NewCategoryDto;
import ewm.category.model.Category;
import ewm.common.dto.category.CategoryDto;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static Category toEntity(NewCategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public static CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    public static void update(Category category, CategoryDto dto) {
        category.setName(dto.getName());
    }
}
