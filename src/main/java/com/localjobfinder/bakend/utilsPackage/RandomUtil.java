package com.localjobfinder.bakend.utilsPackage;

import java.util.Random;
import java.util.UUID;

public class RandomUtil {

    public static int generateOtp() {
        Random random = new Random();
       return 100000 + random.nextInt(900000);
    }

    public static String randomString(){
        return UUID.randomUUID().toString().replace("-", "");
    }

}
