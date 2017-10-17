package agents;

// Exemple de Bitxo
public class Bitxo1 extends Agent
{
    static final boolean DEBUG = false;

    // per entendre millor els valors
    
    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;

    private Estat estat;
    private int espera = 0;

    public Bitxo1(Agents pare) {
        super(pare, "Bitxo1Grup20", "imatges/robotank1.gif");
    }

    @Override
    public void inicia()
    {
        setVelocitatAngular(7);
        espera = 0;
    }

    @Override
    public void avaluaComportament()
    {
        gira(1);
        dispara();
    }
    
    private int minim(double a, double b)
    {
        if (a <= b) return (int) a;
        else return (int) b;
    }
    
    private int minimaDistancia()   // Calcula la distància mínima dels visors
    {
        return  minim(estat.distanciaVisors[ESQUERRA], minim(estat.distanciaVisors[CENTRAL], estat.distanciaVisors[DRETA]));
    }
}