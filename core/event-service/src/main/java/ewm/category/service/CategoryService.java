package ewm.category.service;

import ewm.category.dto.NewCategoryDto;
import ewm.common.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto dto);

    CategoryDto update(Long id, NewCategoryDto dto);

    void delete(Long id);

    List<CategoryDto> findAll(int from, int size);

    CategoryDto findById(Long id);
}
