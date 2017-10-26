/**
*
AGENT

	MOVE();


	IF(DANGER ) { // BULLET DETECTED OR ATTACKING US FROM BLIND SPOT
		
		IF(LT 2 ENEMY LEFT){

			IF(ENEMY HAS MORE LIFE){
				KEEP SAFE();
			}ELSE{
				KILL();
			}

		}ELSE {
				KEEP SAFE();
		}
		

	}ELSE { // WE’RE SAFE
		
		
		IF(ENEMY DETECTED){ //BLIND SPOT
				KILL();
		}ELSE{
				TAKE RESOURCES ();
		}
	} 
*/
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
    
    private int prev_pasitivitat = 0;
    private int prev_impactesRebuts = 0;
    private int atascado = 0;
    private boolean paredCerca = false;
    private boolean inColissionFrontal = false;
    
    private final int BITXO_LADO = 25; 
    private final int BITXO_ESPACIO = 20; 
    private final int MIN_DIST_ESQ = BITXO_LADO + BITXO_ESPACIO;
    private final int MAX_ANGLE_GIR = 90;
    private final int MIN_ANGLE_GIR = 5;
    private final int MAX_VECES_COLISION = 4;  
    private final int MAX_DIST_REC = MIN_DIST_ESQ-10;
    private final int MAX_ANGLE_VISOR = 30;
    private final int MIN_ANGLE_VISOR = 10;

    public Bitxo1(Agents pare) {
        super(pare, "Bitxo1Grup20", "imatges/robotank1.gif");
    }

    @Override
    public void inicia()
    {
        
        setAngleVisors(30);
        setDistanciaVisors(350);
        setVelocitatLineal(5);
        setVelocitatAngular(9);
        espera = 0;
    }

    @Override
    public void avaluaComportament()
            
    {
       
        if(espera > 0){
            espera--;
            return;
        }
        atura();
        
        estat = estatCombat();
                      
        esquivarParedes();
//        
        if(!paredCerca && !inColissionFrontal){
            obtenerRecursos();
        }else if(paredCerca){
              System.out.println(" pared  !");
        }else if(inColissionFrontal){
             System.out.println(" inColissionFrontal  !");
        }
        
        
        
               
            
            prev_impactesRebuts = estat.impactesRebuts;
            prev_pasitivitat = this.getPassivitat();
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
    
    private void esquivarParedes(){
        
        
        if(paredCerca){
            if(estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < MIN_DIST_ESQ){
                gira(-3);
            }else  if(estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < MIN_DIST_ESQ){
                gira(3);
            }
            setAngleVisors(MAX_ANGLE_VISOR);
            paredCerca = false;
            endavant();
            return;
        }
        
//        if(atascado > MAX_VECES_COLISION){
//            hyperespai();
//            atascado = 0;
//        }

         inColissionFrontal = false;
         for (int i = 0; i < estat.distanciaVisors.length; i++) {
            if(!inColissionFrontal && estat.distanciaVisors[i] <= 10){
                inColissionFrontal = true;
            }
        }
        
        if(inColissionFrontal) {
            enrere();
            espera = MAX_VECES_COLISION;
            return;
        }
        

        double d_esq = estat.distanciaVisors[ESQUERRA];
        double d_cen = estat.distanciaVisors[CENTRAL];
        double d_dre = estat.distanciaVisors[DRETA];
        
        
        /*
            Detecta los visores de menor a mayor .
        */
        
        
        int factorGiro = -1;
        int visor_mayor = ESQUERRA;
        int visor_menor = ESQUERRA;
        int visor_medio = ESQUERRA + CENTRAL + DRETA;
        for (int i = 1; i < estat.distanciaVisors.length; i++) {
            if(estat.distanciaVisors[i] > estat.distanciaVisors[visor_mayor]) {
                visor_mayor = i;
            }
            
            if(estat.distanciaVisors[i] < estat.distanciaVisors[visor_menor]){
                visor_menor = i;
            }
            
        }
        
        visor_medio = visor_medio - visor_mayor - visor_menor;
       
        
        /*
            Calcula el angulo de giro . valor = [MIN_ANGULO , MAX_ANGULO]
                
                angulo = MIN_ANGULO + (MAX_ANGULO-MIN_ANGULO)*B ,donde B = [0 , 1]
                B es proporcional al visor_menor
        
        */
        paredCerca = false;
        int angulo = 0;
        if(estat.distanciaVisors[visor_menor] < MIN_DIST_ESQ){  
            try {
                 double factorB = Math.abs((estat.distanciaVisors[visor_menor]*estat.distanciaVisors[visor_menor] + (estat.distanciaVisors[visor_medio] + estat.distanciaVisors[visor_mayor])/2)/(MIN_DIST_ESQ*MIN_DIST_ESQ + (estat.distanciaVisors[visor_medio]+estat.distanciaVisors[visor_mayor])/2));
                  angulo = MIN_ANGLE_GIR + (int)((MAX_ANGLE_GIR-MIN_ANGLE_GIR)*(1-factorB));
                  paredCerca = true;
            } catch(Exception e){
                System.out.println("return");
                return;
            }
           
            
        }
        
        //System.out.println("(int)estat.distanciaVisors[visor_menor] "+(int)estat.distanciaVisors[visor_menor]);
       
        /*
            Si hay una pared cerca :
                si el visor_mayor es lateral ir en ese lado
                si no 
                    si es el central
                        
        */
        if(paredCerca){
             setAngleVisors(MIN_ANGLE_VISOR);
            double deltaDist_EC = Math.abs(d_esq-d_cen);
            double deltaDist_DC = Math.abs(d_dre-d_cen);
            
//            if(deltaDist_EC < deltaDist_DC){ // Hay un obstáculo más cerca a la derecha
//                        // girar izquierda
//                        gira(angulo);
//            }else{
//                        // girar derecha
//                        gira(-angulo);
//            }
            
            switch (visor_mayor) {
                case CENTRAL:
                    
                    angulo = (int)Math.log(angulo);
                   if(deltaDist_EC < deltaDist_DC){ // Hay un obstáculo más cerca a la derecha
                        // girar izquierda
                        gira(angulo); 
                        System.out.println("central izq");
                    }else{
                        // girar derecha
                        gira(-angulo);
                        System.out.println("central der");
                    }   
                    break;               
                case ESQUERRA:
                    System.out.println("ESQUERRA");
                    gira(angulo);
                    break;
                default:
                    System.out.println("DRETA");
                    gira(-angulo);
                    break;
            }
            
            
        }
        endavant();
  }
    
    private void obtenerRecursos(){
        
            
        
            /*
                Obtener el recurso más cerca
            */
            int tipus , bx ,by ,mx ,my ;
            int min_x = -1,min_y = -1;
            int min_distX = (int)Math.sqrt(Integer.MAX_VALUE);
            int min_distY = (int)Math.sqrt(Integer.MAX_VALUE);
            //System.out.println(min_distX + " , "+min_distY);
            boolean found = false;
            for (Bonificacio bonificacion : estat.bonificacions) {
                // bonificación
                bx = bonificacion.posicio.x;
                by = bonificacion.posicio.y;
                tipus = bonificacion.tipus;
                // mi bitxo
                mx = estat.posicio.x;
                my = estat.posicio.y;
                //si no es mina calcular distancia y actualizar min_dist
                if(tipus != Agent.MINA){
                    int distX = Math.abs(mx-bx);
                    int distY = Math.abs(my-by);
                    double distRecurso = distX*distX+distY*distY;
                    distRecurso = Math.sqrt(distRecurso);
                    if(distX*distY < min_distX*min_distY && distRecurso < MAX_DIST_REC){
                        found = true;
                        min_distX = distX;
                        min_distY = distY;
                        min_x = bx;
                        min_y = by;
                    }
                }
            }
            
            // a por el recurso
            if(found){
                System.out.println(" obtener recursos !");
                mira(min_x ,min_y);
            }
    
            endavant();
    }
}