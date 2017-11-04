package agents;

public class Bitxo20 extends Agent
{
    static final boolean DEBUG = false;
    
    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;
    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;
    static final int VELOCIDAD_LINEAL_POR_DEFECTO = 4;
    static final int VELOCIDAD_ANGULAR_POR_DEFECTO = 5;
    static final int DISTANCIA_VISORES_POR_DEFECTO = 300;
    static final int ANGULO_VISORES_POR_DEFECTO = 8;
    static final int OBSTACULO_CERCANO = 60;
    static final int RECURSO_CERCANO = 90;
    static final int BALA_CERCANA = 40;
    static final int MAX_DIST_ENEMIGO = 100;

    private Estat estat;
    private int espera;
    private int colisionesConsecutivas;
    private Bonificacio recursoMasCercano;
    private Punt enemigoMasCercano;
    private int prev_impactesRebuts;
    private int prev_distanciaBalaEnemiga;
    private int girosConsecutivos;
    private int giroActual;

    public Bitxo20(Agents pare) {
        super(pare, "Lover", "imatges/corasao.png");
    }

    @Override
    public void inicia()
    {
        setAngleVisors(ANGULO_VISORES_POR_DEFECTO);
        setDistanciaVisors(DISTANCIA_VISORES_POR_DEFECTO);
        setVelocitatLineal(VELOCIDAD_LINEAL_POR_DEFECTO);
        setVelocitatAngular(VELOCIDAD_LINEAL_POR_DEFECTO);
        espera = 0;
        colisionesConsecutivas = 0;
        recursoMasCercano = null;
        prev_impactesRebuts = 0;
        prev_distanciaBalaEnemiga = Integer.MAX_VALUE;
        girosConsecutivos = 0;
        giroActual = 0;
    }

    @Override
    public void avaluaComportament(){
        
        if(espera > 0){
            espera--;
            return;
        }
        
        evaluarEventos();
    }
    
    private void evaluarEventos(){
        estat = estatCombat();
        
        if(disparoRecibido() || (balaAcercandose() && aPuntoDeMorir())) activaEscut();
        
        if(atascado()){
            hyperespai();
        }
        
        /* Combate */
        else if(balaCercanaDetectada() && balaAcercandose()){
            alejarseDeBala();
        }
        else if(disparoRecibido() && aPuntoDeMorir() && estat.hyperespaiDisponibles > 0){
            hyperespai();
        }
        
        else if(enemigoDetectado() && hayBalas()){
            atacarEnemigoMasCercano();
        }
        
        /* Movimiento */
        else if(colisionOcurrida()){
            enrere();
            evitarChoque();
            espera = 8;
        }                          
        else if(colisionConParedInminente()){
            evitarChoque();
            endavant();
        }            
        else if(recursoCercanoDetectado()){
            mira(recursoMasCercano.posicio.x, recursoMasCercano.posicio.y);
            endavant();
        }
        else {
            boolean esquerra = ((int)(Math.random()*100 % 2) == 0);
            if(girosConsecutivos > 20){
                if(esquerra) giroActual = -1;
                else         giroActual = 1;
                girosConsecutivos = 0;
                System.out.println(" ");
            }
            
            girosConsecutivos++;
            System.out.println(giroActual);
            gira(giroActual);
            
            endavant();
        }
        
        actualizarMemoria();
    }
    
    
    /*
     * Event triggers 
     */
    
    private boolean enemigoDetectado(){

            double distanciaEnemigoMasCercano = Integer.MAX_VALUE;
            enemigoMasCercano = null;
            
            for (Punt posicionEnemigo : estat.posicioEnemic) {
                if(posicionEnemigo != null){
                    if(posicionEnemigo.x != 0 && posicionEnemigo.y != 0){
                        double distEnemigo = estat.posicio.distancia(posicionEnemigo);
                        if(distEnemigo < distanciaEnemigoMasCercano){
                            distanciaEnemigoMasCercano = distEnemigo; 
                            enemigoMasCercano = posicionEnemigo;
                        }
                    }
                }
            }
            return enemigoMasCercano != null;
    }
    
    private boolean balaAcercandose(){
        return prev_distanciaBalaEnemiga < estat.distanciaBalaEnemiga;
    }
    
    private boolean disparoRecibido(){
        return estat.impactesRebuts > prev_impactesRebuts;
    }
    
    private boolean balaCercanaDetectada(){
        return estat.balaEnemigaDetectada && estat.distanciaBalaEnemiga < BALA_CERCANA;
    }
    
    private boolean aPuntoDeMorir(){
        return estat.impactesRebuts > 2;
    }
    
    private boolean colisionOcurrida(){
        if(estat.enCollisio){
            colisionesConsecutivas++;
            return true;
        } else {
            colisionesConsecutivas = 0;
            return false;
        }
    }
    
    private boolean atascado(){
        return colisionesConsecutivas >= 6;
    }
    
    private boolean colisionConParedInminente(){
        for (int i = 0; i < 3; i++) {
            if(estat.estatVisor[i] &&
                    estat.objecteVisor[i] == PARET &&
                    estat.distanciaVisors[i] <= OBSTACULO_CERCANO){
                return true;
            }
        }
        return false;
    }
    
    private boolean recursoCercanoDetectado(){
        double distanciaRecursoMasCercano = Integer.MAX_VALUE;
        recursoMasCercano = null;
        Bonificacio escudoMasCercano = null;
            
        for (Bonificacio bonificacio : estat.bonificacions) {
            if (bonificacio.tipus == MINA) continue;
            
            double distanciaRecursoActual = estat.posicio.distancia(bonificacio.posicio);
            
            if(distanciaRecursoActual <= RECURSO_CERCANO && 
                    distanciaRecursoActual < distanciaRecursoMasCercano)
            {
                distanciaRecursoMasCercano = distanciaRecursoActual; 
                recursoMasCercano = bonificacio;
            }
        }
        
        // Priorizamos escudos
        //if(escudoMasCercano != null) recursoMasCercano = escudoMasCercano;
        
        return recursoMasCercano != null;
    }
    
    
    /*
     * Event handlers
     */
    
    private enum situacion { L, I, D, C, IC, ID, CD, ICD }
    private void evitarChoque(){
        boolean distanciaCerca[] = {false, false, false};
        situacion s = situacion.L;
        int derecha = -10, izquierda = 20;
        
        for (int i = 0; i < estat.distanciaVisors.length; i++) {
            if(estat.distanciaVisors[i] <= OBSTACULO_CERCANO) distanciaCerca[i] = true;
        }
        
        if     (distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.ICD;     // 111
        else if(distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.IC;     // 110
        else if(distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.ID;     // 101
        else if(distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.I;    // 100
        else if(!distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.CD;     // 011
        else if(!distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.C;    // 010
        else if(!distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.D;    // 001
        else if(!distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.L;   // 000
        
        
        switch(s){
            case I:
                gira(derecha);
                break;
            case IC:
                gira(derecha);
                break; 
            case D:
                gira(izquierda);
                break;
            case C:
                gira(izquierda);
                break;
            case CD:
                gira(izquierda);
                break;
            case ID:
                gira(izquierda*2);
            case ICD:
                gira(izquierda*2);
                break;
            case L:
                gira(izquierda);
                break;
        }
    }
    
    private boolean hayBalas(){
            return estat.bales > 0 || estat.perforadores > 0;
    }
    
    private void atacarEnemigoMasCercano(){
        mira(enemigoMasCercano.x,enemigoMasCercano.y);
        if(estat.perforadores > 0)  perforadora();
        else                        dispara();
        endavant();
    }
    
    private void alejarseDeBala(){
        gira(-5);
        enrere();
    }
    
    
    /**
     * Funciones auxiliares
     */
    
    private void actualizarMemoria(){
        prev_impactesRebuts = estat.impactesRebuts;
        prev_distanciaBalaEnemiga = estat.distanciaBalaEnemiga;
    }
   
}