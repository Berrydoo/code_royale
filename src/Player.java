import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.math.*;


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
                int structureType = in.nextInt(); // -1 = No structure, 2 = Barracks
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
                int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
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

    int gold;
    int touchedSite;
    List<Site> sites;
    List<Structure> structures;
    List<Unit> units;

    public QueenDecisionMaker( int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.sites = sites;
        this.structures = structures;
        this.units = units;
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
        return getStructureBySiteId(siteId).structureType == Constants.EMPTY_SITE;
    }

    private Structure getStructureBySiteId(int siteId ){

        // log("getStructureBySiteId " + siteId);
        return this.structures.stream()
                .filter( s -> s.siteId == siteId)
                .findAny()
                .orElse(null);
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
        Site closestEmptySite = getClosestEmptySite();
        if( Objects.nonNull(closestEmptySite) ){
            System.out.println("MOVE " + closestEmptySite.x + " " + closestEmptySite.y);
        } else {
            Site closestOppSite = getClosestOpponentSite();
            if( Objects.nonNull(closestOppSite) ){
                System.out.println("MOVE " + closestOppSite.x + " " + closestOppSite.y);
            } else {
                System.out.println("WAIT");
            }
        }
    }

    private Site getClosestEmptySite(){
        // log("getClosestEmptySite");
        Structure structure = this.structures.stream()
                .filter( s -> s.structureType == Constants.NO_STRUCTURE )
                .min( (s1, s2) -> Double.compare(getDistanceFromQueen(s1), getDistanceFromQueen(s2) ) )
                .get();

        if( Objects.nonNull(structure) ){
            Site site = getSiteById( structure.siteId );
            Logger.log("Closest Structure: " + site.x + ", " + site.y);
            return site;
        } else {
            return null;
        }
    }

    private Site getClosestOpponentSite(){

        // log("getClosestOpponentSite");
        Structure structure = this.structures.stream()
                .filter( s -> s.structureType == Constants.NO_STRUCTURE )
                .min( (s1, s2) -> Double.compare(getDistanceFromQueen(s1), getDistanceFromQueen(s2) ) )
                .get();

        if( Objects.nonNull(structure)){
            return getSiteById( structure.siteId );
        } else {
            return null;
        }
    }

    private double getDistanceFromQueen(Structure structure){

        // log("getDistanceFromQueen " + structure);

        Unit queen = findFriendlyUnitByUnitType(Constants.QUEEN);
        Site site = getSiteById(structure.siteId);

        int yDiff = Math.max(queen.y, site.y) - Math.min(queen.y, site.y);
        int xDiff = Math.max(queen.x, site.x) - Math.min(queen.x, site.x);

        return Math.sqrt( (yDiff*yDiff) + (xDiff*xDiff) );

    }

    private Unit findFriendlyUnitByUnitType( int type){

        // log("findUnitByType " + type);
        return this.units.stream()
                .filter( u -> u.owner == Constants.FRIENDLY_OWNER)
                .filter( u -> u.unitType == type)
                .findFirst()
                .get();
    }

    private Unit findEnemyUnitByUnitType( int type){

        // log("findUnitByType " + type);
        return this.units.stream()
                .filter( u -> u.owner == Constants.ENEMY_OWNER)
                .filter( u -> u.unitType == type)
                .findFirst()
                .get();
    }

    private Site getSiteById( int siteId){

        // log("getSiteById " + siteId);
        return this.sites.stream()
                .filter( s -> s.siteId == siteId)
                .findAny()
                .orElse(null);
    }

}


class TrainingDecisionMaker implements DecisionMaker {

    int gold;
    int touchedSite;
    List<Site> sites;
    List<Structure> structures;
    List<Unit> units;

    public TrainingDecisionMaker(int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.sites = sites;
        this.structures = structures;
        this.units = units;
    }

    public void decide(){

        // log("makeTrainingDecision");
        List<Structure> friends = getAllFriendlyStructures();

        if( capableOfTraining(friends.size()) ) {
            if( allUnitTypesAvailable() ){
                int targetType = getWhichTypeToBuild();
                List<Structure> friendlyStructsOfType = getStructuresOfTargetType(targetType);
                int targetSiteId = getRandomFriendlySiteId(friendlyStructsOfType);
                System.out.println("TRAIN " + targetSiteId );
            } else {
                System.out.println("TRAIN " + getRandomFriendlySiteId(friends) );
            }
        } else {
            System.out.println("TRAIN");
        }

    }

    private List<Structure> getAllFriendlyStructures(){
        return this.structures.stream()
                .filter( s -> s.owner == Constants.FRIENDLY_OWNER)
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

        int result = archerCount > knightCount ? Constants.KNIGHT : Constants.ARCHER;
        Logger.log("Archer Count: "  + archerCount + ", Knight Count: " + knightCount + ", Target type: " + result);
        return result;
    }

    private List<Structure> getStructuresOfTargetType( int targetType){

        List<Structure> structsOfType =  this.structures.stream()
                .filter( s -> s.owner == Constants.FRIENDLY_OWNER)
                .filter( s -> s.siteId % 2 == targetType)
                .collect(Collectors.toList());
        Logger.log("Structs of target type: " + structsOfType.stream().map(s -> s.siteId).map(String::valueOf).collect(Collectors.joining(", ") )  );
        return structsOfType;
    }

    private int getRandomFriendlySiteId(List<Structure> friends){
        Double index = getRandomIndexOfFriend(friends.size());
        int selectedSite = friends.get(index.intValue()).siteId;

        Logger.log("Selected site for new unit: " + selectedSite);
        return selectedSite;

    }

    private Double getRandomIndexOfFriend(int friendsCount){
        return Math.random() * (friendsCount-1);
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
    static int QUEEN = -1;
    static int KNIGHT = 0;
    static int ARCHER = 1;
    static int FRIENDLY_OWNER = 0;
    static int ENEMY_OWNER = 1;
    static int ARCHER_COST = 100;
    static int KNIGHT_COST = 80;
}