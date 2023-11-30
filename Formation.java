public class Formation {
    private int annee;
    private int rang;
    private int idRes;


    public Formation(int annee, int rang) {
        this.annee = annee;
        this.rang = rang;
        this.idRes = 0;
    }

    public int getAnnee(){
        return this.annee;
    }

    public int getRang(){
        return this.rang;
    }

    public void setIdRes(int idRes){
        this.idRes = idRes;
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
