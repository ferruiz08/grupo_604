package com.example.tp2;

public class Global {

    private static String token = "";
    private static int indicePreferencias;


    public static String getToken(){
        return token;
    }

    public static void setToken(String t){
        token = t;
    }

    public static int getIndicePreferencias(){
        return indicePreferencias;
    }

    public static void setIndicePreferencias(int i){
        indicePreferencias = i;
    }

    public static void incIndicePreferencias(){
        indicePreferencias++;
    }
}
