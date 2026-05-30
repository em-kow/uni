package model;
public class Sportowiec {

    //  Numer sportowca.
    private int id;

    //  Poziom umiejętności sportowca.
    private int poziom;

    //  Współczynnik spontaniczności sportowca. Oznacza on z jakim prawdopodo-
    //  bieństwem sportowiec wybierze losowo trasę lub wyciąg. 
    private double spontanicznosc;

    //  Waga atrakcyjności aspektu dopasowania poziomem trudności trasy.
    private double wagaTrudnosci;

    //  Waga atrakcyjności aspektu wyrównania nawierzchni trasy.
    private double wagaNawierzchni;

    //  Węzeł, w którym sportowiec zaczyna dzień.
    private Wezel wezelStartowy;

    //  Węzeł, w którym sportowiec aktualnie się znajduje.
    private Wezel aktualnyWezel;

    //  Czy sportowiec jest śledzony.
    private boolean sledzony;

    //  Czas rozpoczęcia dnia na stoku liczony w sekundach od 9:00.
    private int czasRozpoczecia;

    public Sportowiec(
        int id,
        int poziom,
        double spontanicznosc,
        boolean sledzony,
        double wagaTrudnosci,
        double wagaNawierzchni,
        Wezel wezelStartowy,
        int czasRozpoczecia
    ) {
        this.id = id;
        this.poziom = poziom;
        this.spontanicznosc = spontanicznosc;
        this.sledzony = sledzony;
        this.wagaTrudnosci = wagaTrudnosci;
        this.wagaNawierzchni = wagaNawierzchni;
        this.wezelStartowy = wezelStartowy;
        this.czasRozpoczecia = czasRozpoczecia;
    }

    public Wezel getWezelStartowy() {
        return wezelStartowy;
    }

    public void setAktualnyWezel(Wezel wezel) {
        aktualnyWezel = wezel;
    }

    public boolean czySledzony() {
        return sledzony;
    }

    public int getId() {
        return id;
    }

    public Wezel getAktualnyWezel() {
        return aktualnyWezel;
    }

    public double getSpontanicznosc() {
        return spontanicznosc;
    }

    public int getPoziom() {
        return poziom;
    }

    public double getWagaTrudnosci() {
        return wagaTrudnosci;
    }

    public double getWagaNawierzchni() {
        return wagaNawierzchni;
    }

    public int getCzasRozpoczecia() {
        return czasRozpoczecia;
    }
}
