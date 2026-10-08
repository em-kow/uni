package narzedzia;
public class Czas {

    private static final int GODZINA_STARTU = 9;

    private Czas() {
        //  Klasa ma tylko metody statyczne.
    }

    // Zamienia czas w formacie HH:MM:SS na liczbę sekund od 9:00.
    public static int hmsNaSekundy(String hms) {
        String[] czesci = hms.split(":");

        int godziny = Integer.parseInt(czesci[0]);
        int minuty = Integer.parseInt(czesci[1]);
        int sekundy = Integer.parseInt(czesci[2]);

        int sekundyOdPolnocy = godziny * 3600 + minuty * 60 + sekundy;
        int sekundyStartu = GODZINA_STARTU * 3600;

        return sekundyOdPolnocy - sekundyStartu;
    }

    // Zamienia liczbę sekund od startu na format HH:MM:SS.
    public static String sekundyNaHms(int czas) {
        int sekundyOdPolnocy = GODZINA_STARTU * 3600 + czas;

        int godziny = sekundyOdPolnocy / 3600;
        int minuty = (sekundyOdPolnocy % 3600) / 60;
        int sekundy = sekundyOdPolnocy % 60;

        return String.format("%02d:%02d:%02d", godziny, minuty, sekundy);
    }
}