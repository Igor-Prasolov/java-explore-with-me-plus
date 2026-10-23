package ru.practicum.category.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto { // я хз надо тут аннотации или нет, нужно разбираться в контроллерах
    private Long id;
    private String name;
}
