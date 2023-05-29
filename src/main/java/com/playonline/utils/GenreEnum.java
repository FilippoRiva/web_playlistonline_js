package com.playonline.utils;

public enum GenreEnum {
    POP("Pop",1),
    ROCK("Rock",2),
    RAP("Rap",3),
    EDM("Edm",4),
    BLUES("Blues",5),
    SOUL("Soul",6),
    FUNK("Funk",7),
    JAZZ("Jazz",8),
    LOFI("LoFi",9),
    INDIE("Indie",10),
    METAL("Metal",11),
    CLASSIC("Classic",12),
    OTHERS("Others",13);
    final String name;
    final int id;
    GenreEnum(String name, int id){
        this.name = name;
        this.id=id;
    }
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
