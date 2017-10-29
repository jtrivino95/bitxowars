package agents;

// Exemple de Bitxo
public class Bitxo3 extends Agent
{
    static final boolean DEBUG = true;

    // per entendre millor els valors
    
    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;
    
    final int CERCA = 40;
    final int VELOCIDAD_LINEAL_POR_DEFECTO = 5;

    private Estat estat;
    private int repetir = 0;
    private int colisionTotalConsecutivas = 0;

    public Bitxo3(Agents pare) {
        super(pare, "Javi", "imatges/robotank3.gif");
    }

    @Override
    public void inicia()
    {
        setAngleVisors(5);
        setDistanciaVisors(400);
        setVelocitatLineal(VELOCIDAD_LINEAL_POR_DEFECTO);
        setVelocitatAngular(VELOCIDAD_LINEAL_POR_DEFECTO);
    }

    @Override
    public void avaluaComportament(){
        inicia();
        
        if(repetir > 0){
            repetir--;
            return;
        }
        
        evaluarEventos();
        endavant();
        
    }
    
    private void evaluarEventos(){
        estat = estatCombat();
        
        if(atascado()){
            if(estat.hyperespaiDisponibles > 0)
                hyperespai();
            else {
                // TODO: PROBLEMONNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
            }
        }
        
        else if(colisionTotal()){
            setVelocitatLineal(7);
            setVelocitatAngular(7);
            gira(45);
            setVelocitatLineal(VELOCIDAD_LINEAL_POR_DEFECTO);
            setVelocitatAngular(VELOCIDAD_LINEAL_POR_DEFECTO);
        }
        
        else if(colisionInminente()){
            evitarChoque();
        }
        
        else if(disparoInminente()){
            esquivarDisparos();
        }
        
    }
    
    
    /*
     * Event triggers 
     */
    
    private boolean colisionTotal(){
        
        if(estat.enCollisio){
            colisionTotalConsecutivas++;
            return true;
        } else {
            colisionTotalConsecutivas = 0;
            return false;
        }
    }
    
    private boolean atascado(){
        return colisionTotalConsecutivas > 60;
    }
    
    private boolean colisionInminente(){
        
        for (int i = 0; i < 3; i++) {
            if(
                    estat.estatVisor[i] &&
                    estat.objecteVisor[i] == PARET &&
                    estat.distanciaVisors[i] <= CERCA
                    ){
                return true;
            }
        }
        
        return false;
    }
    
    private boolean disparoInminente(){
        return false;
    }
    
    private boolean enemigoCerca(){
        return false;
    }
    
    
    
    
    /*
     * Event handlers
     */
    private enum situacion { L, I, D, C, IC, ID, CD, ICD }
    private void evitarChoque(){
        boolean distanciaCerca[] = {false, false, false};
        situacion s = situacion.L;
        
        for (int i = 0; i < estat.distanciaVisors.length; i++) {
            if(estat.distanciaVisors[i] > CERCA) distanciaCerca[i] = true;
        }
        
        if(distanciaCerca[0] && distanciaCerca[1] && !distanciaCerca[2]) s = situacion.D;
        else if(distanciaCerca[0] && !distanciaCerca[1] && distanciaCerca[2]) s = situacion.C;
        else if(distanciaCerca[0] && !distanciaCerca[1] && !distanciaCerca[2]) s = situacion.CD;
        else if(!distanciaCerca[0] && distanciaCerca[1] && distanciaCerca[2]) s = situacion.I;
        else if(!distanciaCerca[0] && distanciaCerca[1] && !distanciaCerca[2]) s = situacion.ID;
        else if(!distanciaCerca[0] && !distanciaCerca[1] && distanciaCerca[2]) s = situacion.IC;
        else if(!distanciaCerca[0] && !distanciaCerca[1] && !distanciaCerca[2]) s = situacion.ICD;
        
        atura();
        
        switch(s){
            case I:
                gira(-10);
                break;
            case D:
                gira(10);
                break;
            case C:
                gira(-10);
                break;
            case IC:
                gira(-10);
                break;
            case CD:
                gira(10);
                break;
            case ICD:
                gira(10);
                break;
            case L:
                break;
        }
    }
    
    private void esquivarDisparos(){
        
    }
    
    private void alejarseDeEnemigo(){
        
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