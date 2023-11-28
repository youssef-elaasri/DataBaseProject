public class Lot {
    public String modele;
    public String marque;
    public int annee;

    public Lot(String modele,String marque, int annee){
        this.modele = modele;
        this.marque = marque;
        this.annee = annee;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Lot other = (Lot) o;
        return modele.equals((other).modele) && marque.equals((other).marque)  && annee==other.annee;
    }

    @Override
    public int hashCode(){
        return 0;
    }
}

