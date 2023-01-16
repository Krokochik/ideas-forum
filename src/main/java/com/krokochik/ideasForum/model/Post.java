package com.krokochik.ideasForum.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.sql.Date;

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
    String title;
    @NotNull
    @Column(length = 60_000)
    String content;

    String fulltextTitle = "";
    @Column(length = 60_000)
    String fulltextContent = "";
    

    @NotNull
    String tags;
    @NotNull
    String author;

    @Type(type = "java.sql.Date")
    Date creationDate = new Date(System.currentTimeMillis());
}