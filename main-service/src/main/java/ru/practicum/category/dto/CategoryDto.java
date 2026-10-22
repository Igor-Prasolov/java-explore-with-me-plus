package ru.practicum.category.dto;


import lombok.Data;

@Data
public class CategoryDto { // я хз надо тут аннотации или нет, нужно разбираться в контроллерах
    private Long id;
    private String name;
}
