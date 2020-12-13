package sample.util;

import java.util.Random;

public class AppGenerator
{
    private static String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static String numbers = "1234567890";
    private static String specials = "_-";
    private static String[] App = {"notePad", "steam", "discord", "telegram", "twitch", "bluestacks", "googleChrome"};

    private static Random random = new Random();

    public static String generate()
    {
        String result = "";

        if(random.nextInt(10) == 0)
        {
            result += App[random.nextInt(App.length)];
        }
        else
        {
            int nameLength = random.nextInt(7) + 5;

            for (int i = 0; i < nameLength; i++)
            {
                int r = random.nextInt(10);
                result += r == 0 ? getRandomSpecial() : r < 6 ? getRandomLetter() : getRandomNumber();
            }
        }

        result += ".exe";

        return result;
    }

    public static String getRandomLetter()
    {
        return "" + (letters.toCharArray()[random.nextInt(letters.length())]);
    }

    public static String getRandomNumber()
    {
        return "" + (numbers.toCharArray()[random.nextInt(numbers.length())]);
    }

    public static String getRandomSpecial()
    {
        return "" + (specials.toCharArray()[random.nextInt(specials.length())]);
    }
}
