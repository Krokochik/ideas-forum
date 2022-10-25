package com.krokochik.ideasForum.model;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name="pst")
public class Post
{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;

    @NotNull
    String title;
    @NotNull
    String content;
    @NotNull
    String tags;
    @NotNull
    String creationTime;
    @NotNull
    String creationDate;
    @NotNull
    String author;
}
