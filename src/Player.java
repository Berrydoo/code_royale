import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        List<Site> sites = new ArrayList<>();
        int numSites = in.nextInt();
        for (int i = 0; i < numSites; i++) {
            int siteId = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();
            sites.add(new Site(siteId, x, y, radius));
        }

        // game loop
        while (true) {
            int gold = in.nextInt();
            int touchedSite = in.nextInt(); // -1 if none

            List<Structure> structures = new ArrayList<>();
            for (int i = 0; i < numSites; i++) {
                int siteId = in.nextInt();
                int ignore1 = in.nextInt(); // used in future leagues
                int ignore2 = in.nextInt(); // used in future leagues
                int structureType = in.nextInt(); // -1 = No structure, 1 = Tower, 2 = Barracks
                int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
                int param1 = in.nextInt();
                int param2 = in.nextInt();
                structures.add(new Structure(siteId, ignore1, ignore2, structureType, owner, param1, param2));
            }

            List<Unit> units = new ArrayList<>();
            int numUnits = in.nextInt();
            for (int i = 0; i < numUnits; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                int owner = in.nextInt();
                int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER, 2 = GIANT
                int health = in.nextInt();
                units.add(new Unit(x, y, owner, unitType, health));
            }

            Manager manager = new Manager(gold, touchedSite, sites, structures, units);
            manager.manage();

        }
    }
}

class Manager {

    int gold;
    int touchedSite;
    List<Site> sites;
    List<Structure> structures;
    List<Unit> units;

    public Manager( int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.sites = sites;
        this.structures = structures;
        this.units = units;
    }

    public void manage(){
        makeQueenDecision();
        makeTrainingDecision();
    }

    public void makeQueenDecision(){
        QueenDecisionMaker qdm = new QueenDecisionMaker(this.gold, this.touchedSite, this.sites, this.structures, this.units );
        qdm.decide();
    }

    public void makeTrainingDecision(){

        TrainingDecisionMaker tdm = new TrainingDecisionMaker(this.gold, this.touchedSite, this.sites, this.structures, this.units );
        tdm.decide();

    }

}

interface DecisionMaker {

    void decide();
}

class QueenDecisionMaker implements DecisionMaker {

    private int gold;
    private final int touchedSite;
    private List<Site> sites;
    private List<Structure> structures;
    private List<Unit> units;

    Query query;

    public QueenDecisionMaker( int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.sites = sites;
        this.structures = structures;
        this.units = units;
        this.query = new Query(structures, units, sites);
    }

    public void decide(){
        if( canBuildStructure() ){
            buildStructure();
        } else {
            move();
        }
    }

    private boolean canBuildStructure(){

        // log("canBuildStructure");
        return this.touchedSite != Constants.NOT_TOUCHING
                && siteIsEmpty(this.touchedSite);
    }

    private boolean siteIsEmpty( int siteId){
        // log("siteIsEmpty " + siteId);
        return query.getStructureBySiteId(siteId).structureType == Constants.EMPTY_SITE;
    }

    private void buildStructure(){
        // log("buildStructure");
        System.out.println("BUILD " + this.touchedSite + " BARRACKS-" + getNextStructureType());
    }

    private String getNextStructureType(){
        // log("getNextStructureType");
        return this.touchedSite % 2 == 0 ? "KNIGHT" : "ARCHER";
    }

    private void move(){
        // log("move");
        Site closestEmptySite = query.getClosestSiteToUnit(Predicates.emptyOrBarracksStructure, Predicates.enemyOrNoOwner, Predicates.friendlyUnit, Predicates.queenUnitType);
        if( Objects.nonNull(closestEmptySite) ){
            System.out.println("MOVE " + closestEmptySite.x + " " + closestEmptySite.y);
        } else {
            System.out.println("WAIT");
        }
    }

}


class TrainingDecisionMaker implements DecisionMaker {

    int gold;
    int touchedSite;
    List<Site> sites;
    List<Structure> structures;
    List<Unit> units;
    Query query;

    public TrainingDecisionMaker(int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.sites = sites;
        this.structures = structures;
        this.units = units;
        this.query = new Query(structures, units, sites);
    }

    public void decide(){

        // log("makeTrainingDecision");

        List<Structure> friendlyStructures = getStructures(Predicates.friendlyStructure);

        printUnitCounts();

        if( capableOfTraining(friendlyStructures.size()) ) {
            if( allUnitTypesAvailable() ){
                int targetType = getWhichTypeToBuild();
                if(targetType == Constants.KNIGHT){
                    Logger.log("train knight closest to enemy queen");
                    System.out.println("TRAIN " + getKnightFactoryClosestToEnemyQueen());
                } else {
                    Logger.log("train archer closest to our queen");
                    System.out.println("TRAIN " + getArcherFactoryClosestToFriendlyQueen());
                }
            } else {
                Logger.log("train the one type available, closest to our queen");
                if( archerTypeIsAvailable() ){
                    System.out.println("TRAIN " + getArcherFactoryClosestToFriendlyQueen());
                } else if ( knightTypeIsAvailable()){
                    System.out.println("TRAIN " + getKnightFactoryClosestToEnemyQueen());
                }
            }
        } else {
            noTraining();
        }

    }

    private void chooseAmongAllUnitTypes(){
        int targetType = getWhichTypeToBuild();
        if(targetType == Constants.KNIGHT && knightTypeIsAvailable() ){
            Logger.log("train knight closest to enemy queen");
            System.out.println("TRAIN " + getKnightFactoryClosestToEnemyQueen());
        } else if( archerTypeIsAvailable() ){
            Logger.log("train archer closest to our queen");
            System.out.println("TRAIN " + getArcherFactoryClosestToFriendlyQueen());
        }
    }

    private void chooseAvailableUnitType(){
        Logger.log("train the one type available, closest to our queen");
        if( archerTypeIsAvailable() ){
            System.out.println("TRAIN " + getArcherFactoryClosestToFriendlyQueen());
        } else if ( knightTypeIsAvailable()){
            System.out.println("TRAIN " + getKnightFactoryClosestToEnemyQueen());
        }
    }

    private void noTraining(){
        Logger.log("cannot train");
        System.out.println("TRAIN");
    }

    private List<Structure> getStructures(Predicate<? super Structure> structurePredicate){
        return this.structures.stream()
                .filter( structurePredicate )
                .collect(Collectors.toList());
    }

    private boolean capableOfTraining(int friendsCount){
        return friendsCount > 0
                && this.gold >= Constants.ARCHER_COST;
    }

    private boolean allUnitTypesAvailable(){
        boolean knightAvailable = this.structures.stream().filter(s -> s.owner == Constants.FRIENDLY_OWNER).anyMatch( s -> s.siteId % 2 == Constants.KNIGHT);
        boolean archerAvailable = this.structures.stream().filter(s -> s.owner == Constants.FRIENDLY_OWNER).anyMatch( s -> s.siteId % 2 == Constants.ARCHER);

        boolean result = knightAvailable && archerAvailable;
        Logger.log("All types available: " + result);
        return result;
    }

    private int getWhichTypeToBuild(){
        long archerCount = this.units.stream().filter( unit -> unit.unitType == Constants.ARCHER).count();
        long knightCount = this.units.stream().filter( unit -> unit.unitType == Constants.KNIGHT).count()/2;
        long giantCount = this.units.stream().filter( unit -> unit.unitType == Constants.GIANT).count();

        int result;
        if( archerCount >= knightCount ){
            result = Constants.KNIGHT;
        } else {
            result = Constants.ARCHER;
        }
        Logger.log("Archer Count: "  + archerCount + ", Knight Count: " + knightCount + ", Giants: " + giantCount + ", Target type: " + query.unitTypeOf(result));
        return result;
    }

    private boolean knightTypeIsAvailable(){
        return !query.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure).isEmpty();
    }

    private boolean archerTypeIsAvailable(){
        return !query.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure).isEmpty();
    }

    private int getKnightFactoryClosestToEnemyQueen(){
            Site closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure),
                                                                Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.enemyUnit);
            return closestSite.siteId;
    }

    private int getArcherFactoryClosestToFriendlyQueen(){
            Site closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure),
                    Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
            return closestSite.siteId;
    }

    private void printUnitCounts(){

        int enemyKnights = query.getAllUnitsOfType(Predicates.knightUnitType, Predicates.enemyUnit).size();
        int enemyArchers = query.getAllUnitsOfType(Predicates.archerUnitType, Predicates.enemyUnit).size();
        int enemyGiants = query.getAllUnitsOfType(Predicates.giantUnitType, Predicates.enemyUnit).size();
        int enemyFactories = query.getAllStructuresOfType(Predicates.barracksStructure, Predicates.enemyStructure).size();
        int friendlyKnights = query.getAllUnitsOfType(Predicates.knightUnitType, Predicates.friendlyUnit).size();
        int friendlyArchers = query.getAllUnitsOfType(Predicates.archerUnitType, Predicates.friendlyUnit).size();
        int friendlyGiants = query.getAllUnitsOfType(Predicates.giantUnitType, Predicates.friendlyUnit).size();
        int friendlyFactories = query.getAllStructuresOfType(Predicates.barracksStructure, Predicates.friendlyStructure).size();


        Logger.log("Enemy Knights:" + enemyKnights + ", Archers: " + enemyArchers + ", Giants: " + enemyGiants + ", Factories: " + enemyFactories);
        Logger.log("Friend Knights:" + friendlyKnights + ", Archers: " + friendlyArchers + ", Giants: " + friendlyGiants + ", Factories: " + friendlyFactories);

    }

}

class Logger {
    public static void log(String msg){
        System.err.println(msg);
    }

}

class Site {

    int siteId;
    int x;
    int y;
    int radius;

    public Site( int siteId, int x, int y, int radius){
        this.siteId = siteId;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
}

class Unit {

    int x;
    int y;
    int owner;
    int unitType;
    String unitText;
    int health;

    public Unit( int x, int y, int owner, int unitType, int health){
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.unitType = unitType;
        this.unitText = unitType == -1 ? "QUEEN" : unitType == 0 ? "KNIGHT" : "ARCHER";
        this.health = health;
    }
}

class Structure {

    int siteId;
    int ignore1;
    int ignore2;
    int structureType;
    String structureText;
    int owner;
    String ownerText;
    int param1;
    int param2;
    String factoryType;

    public Structure(int siteId, int ig1, int ig2, int structureType, int owner, int param1, int param2){
        this.siteId = siteId;
        this.ignore1 = ig1;
        this.ignore2 = ig2;
        this.structureType = structureType;
        this.structureText = structureType ==  -1 ? "No Structure" : "Barracks";
        this.owner = owner;
        this.ownerText = owner == -1 ? "No Structure" : owner == 0 ? "Friendly" : "Enemy";
        this.param1 = param1;
        this.param2 = param2;
        this.factoryType = null;
    }

}

class Constants {
    static int EMPTY_SITE = -1;
    static int NOT_TOUCHING = -1;
    static int NO_STRUCTURE = -1;
    static int TOWER = 1;
    static int BARRACKS = 2;
    static int QUEEN = -1;
    static int KNIGHT = 0;
    static int ARCHER = 1;
    static int GIANT = 2;
    static int FRIENDLY_OWNER = 0;
    static int ENEMY_OWNER = 1;
    static int NO_OWNER = -1;
    static int ARCHER_COST = 100;
    static int KNIGHT_COST = 80;
}

class Predicates {
    public static final Predicate<Structure> friendlyStructure = s -> s.owner == Constants.FRIENDLY_OWNER;
    public static final Predicate<Structure> enemyStructure = s -> s.owner == Constants.ENEMY_OWNER;
    public static final Predicate<Structure> noOwner = s -> s.owner == Constants.NO_OWNER;
    public static final Predicate<Structure> enemyOrNoOwner = s -> (s.owner == Constants.NO_OWNER) || (s.owner == Constants.ENEMY_OWNER);

    public static final Predicate<Structure> emptyStructure = s -> s.structureType == Constants.NO_STRUCTURE;
    public static final Predicate<Structure> barracksStructure = s -> s.structureType == Constants.BARRACKS;
    public static final Predicate<Structure> emptyOrBarracksStructure = s -> (s.structureType == Constants.NO_STRUCTURE) || (s.structureType == Constants.BARRACKS);

    public static final Predicate<Structure> factoryTypeArcher = s -> s.siteId % 2 == Constants.ARCHER;
    public static final Predicate<Structure> factoryTypeKnight = s -> s.siteId % 2 == Constants.KNIGHT;

    public static final Predicate<Unit> friendlyUnit = s -> s.owner == Constants.FRIENDLY_OWNER;
    public static final Predicate<Unit> enemyUnit = s -> s.owner == Constants.ENEMY_OWNER;
    public static final Predicate<Unit> queenUnitType = s -> s.unitType == Constants.QUEEN;
    public static final Predicate<Unit> archerUnitType = s -> s.unitType == Constants.ARCHER;
    public static final Predicate<Unit> knightUnitType = s -> s.unitType == Constants.KNIGHT;
    public static final Predicate<Unit> giantUnitType = s -> s.unitType == Constants.GIANT;

}

class Query {

    private final List<Structure> structures;
    private final List<Unit> units;
    private final List<Site> sites;

    public Query(List<Structure> structures, List<Unit> units, List<Site> sites){
        this.structures = structures;
        this.units = units;
        this.sites = sites;
    }

    public Site getClosestFromListToUnit(List<Structure> structures, Predicate<? super Structure> structureType,
                                         Predicate<? super Structure> structureOwner, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){

        Structure structure = structures.stream()
                .filter( structureType )
                .filter( structureOwner )
                .min(Comparator.comparingDouble(s -> getDistanceFromUnitToSite(s.siteId, unitOwner, unitType)))
                .orElse(null);

        if( Objects.nonNull(structure) ){
            Site site = getSiteById( structure.siteId );
            Logger.log("Closest Structure: Site " + site.siteId + ", X:" + site.x + ", Y:" + site.y);
            return site;
        } else {
            return null;
        }

    }

    public Site getClosestSiteToUnit(Predicate<? super Structure> structureType, Predicate<? super Structure> structureOwner, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner){
        return getClosestFromListToUnit(this.structures, structureType, structureOwner, unitType, unitOwner);
    }

    public double getDistanceFromUnitToSite(int siteId, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){

        // log("getDistanceFromQueen " + structure);

        Unit unit = findUnitByTypeAndOwner(unitType, unitOwner );
        Site site = getSiteById(siteId);

        int yDiff = Math.max(unit.y, site.y) - Math.min(unit.y, site.y);
        int xDiff = Math.max(unit.x, site.x) - Math.min(unit.x, site.x);

        return Math.sqrt( (yDiff*yDiff) + (xDiff*xDiff) );

    }

    public Unit findUnitByTypeAndOwner( Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){
        return this.units.stream()
                .filter( unitType )
                .filter( unitOwner )
                .findFirst()
                .orElse(null);

    }

    public Site getSiteById( int siteId){

        // log("getSiteById " + siteId);
        return this.sites.stream()
                .filter( s -> s.siteId == siteId)
                .findAny()
                .orElse(null);
    }

    public Structure getStructureBySiteId(int siteId ){

        // log("getStructureBySiteId " + siteId);
        return this.structures.stream()
                .filter( s -> s.siteId == siteId)
                .findAny()
                .orElse(null);
    }

    public List<Structure> getAllStructuresOfType(Predicate<? super Structure> factoryType, Predicate<? super Structure> structureOwner){

        List<Structure> structsOfType =  this.structures.stream()
                .filter( structureOwner )
                .filter( factoryType )
                .collect(Collectors.toList());
        Logger.log("Structs of target type: " + structsOfType.stream().map(s -> s.siteId).map(String::valueOf).collect(Collectors.joining(", ") )  );
        return structsOfType;
    }

    public List<Unit> getAllUnitsOfType(Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner){
        return this.units.stream()
                .filter( unitType )
                .filter( unitOwner )
                .collect(Collectors.toList());
    }

    public String unitTypeOf(int unitType){
        return unitType == Constants.ARCHER ? "Archer" : "Knight";
    }

}