package com.yapily.marvel.model;

import com.arnaudpiroelle.marvel.api.objects.Image;
import lombok.Data;

@Data
//@Builder
public class CustomCharacter {
    private Integer id;
    private String name;
    private String description;
    private Image thumbnail;
}

