package com.krokochik.ideasForum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    String publicPart;
    String privatePart;

    @Override
    public String toString() {
        return publicPart + privatePart;
    }
}
