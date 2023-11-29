public class Formation {
    public int annee;
    public int rang;

    public Formation(int annee, int rang) {
        this.annee = annee;
        this.rang = rang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Formation other = (Formation) o;
        return annee == other.annee && rang == other.rang;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
