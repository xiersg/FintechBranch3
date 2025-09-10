package com.financialfinshieldguard.gold.utils;

import java.util.Random;

public class NickNameGeneratorUtil {
    static String [] adjectives = {"粘人的", "聪明的"};

    static String [] animals = {"猫", "狗"};

    public static String generateNickName() {
        Random random = new Random();
        String adj = adjectives[random.nextInt(adjectives.length)];
        String a = animals[random.nextInt(animals.length)];

        return adj + a;
    }
}
