package com.krokochik.ideasForum.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Date;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@ToString(of = {"title", "author", "creationDate"})
@Entity
@Table(name="pst")
public class Post
{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;

    @NotNull
    @Column(length = 45)
    String title;
    @NotNull
    @Column(length = 60_000)
    String content;

    @Column(length = 45)
    String fulltextTitle;
    @Column(length = 60_000)
    String fulltextContent;

    @NotNull
    String tags;
    @NotNull
    String author;

    @Type(type = "java.util.Date")
    Date creationDate = new Date();
}
