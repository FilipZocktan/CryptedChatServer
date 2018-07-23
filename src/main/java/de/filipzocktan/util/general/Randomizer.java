package de.filipzocktan.util.general;

import java.util.Random;

/**
 * Standardized randomizer class by Filip Zocktan
 *
 * @author Filip Zocktan @ Filip Zocktan Studios
 * @version 1.0
 * @since 31 01 2017 - 21:55:21
 */
public class Randomizer {

    static Random r = new Random();

    /**
     * Chooses a random String from the Variable rndm
     *
     * @param rndm A String[] from where the String is chosen
     * @return A random String from the Variable rndm
     */
    public static String getRandomfromStringArray(String[] rndm) {
        String str = rndm[r.nextInt(rndm.length)];

        return str;
    }

    /**
     * Generates Random Integer with maximum of the Variable i
     *
     * @param i The Maximum of the generated Integer
     * @return Random Integer with maximum of the Variable i
     */
    public static Integer getRandom(int i, boolean notzero) {
        int e = r.nextInt(i);
        if (e == 0 && notzero) {
            return getRandom(i, true);
        } else {
            return e;
        }
    }

    public static Integer getRandomMin(int max, int min) {
        return (r.nextInt(max - min) + min);
    }

    /**
     * Generates a random boolean
     *
     * @return Random Boolean
     */
    public static Boolean getBool() {
        return r.nextBoolean();
    }

}
