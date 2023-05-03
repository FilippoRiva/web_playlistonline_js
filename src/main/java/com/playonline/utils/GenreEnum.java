package com.playonline.utils;

public enum GenreEnum {
    POP("Pop",1),
    ROCK("Rock",2),
    RAP("Rap",3),
    EDM("Edm",4),
    BLUES("Blues",5),
    SOUL("Soul",6),
    FUNK("Funk",7);
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
