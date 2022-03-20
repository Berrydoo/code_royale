package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static main.TestUtils.logPassed;

class TestRunner {
    public static void main(String[] args){
        QueryTests.main(new String[]{"run"});
        QueenDecisionTests.main(new String[]{"run"});
        TrainingDecisionTests.main(new String[]{"run"});

    }
}

class QueryTests {

    private final Query target;
    private final TestUtils utils;

    public QueryTests( Query target, TestUtils utils){
        this.target = target;
        this.utils = utils;
    }

    public static void main(String[] strings){
        TestUtils utils = new TestUtils();
        Query target = new Query( utils.getAllStructures(), utils.getAllUnits(), utils.sites);
        QueryTests queryTests = new QueryTests(target, utils);

        queryTests.getSiteById();
        queryTests.getClosestSiteToUnit();
        queryTests.getDistanceFromUnitToSite();
        queryTests.getClosestFromListToUnit_good();
        queryTests.getClosestFromListToUnit_fail();
        queryTests.getClosestFriendlyTower();
        queryTests.getStructureBySiteId();
        queryTests.getAllStructuresOfType();
        queryTests.getAllUnitsOfType();
        queryTests.getUnitTypeOf();
    }

    public void getSiteById(){
        assertNotNull(target.getSiteById(1));
        logPassed("getSiteById");
    }

    public void getClosestSiteToUnit(){
        Site site = target.getClosestSiteToUnit(Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
        assertEquals(2, site.siteId);
        logPassed("getClosestSiteToUnit");
    }

    public void getClosestFriendlyTower(){
        Site targetSite = target.getClosestFromListToUnit( target.getAllStructuresOfType(Predicates.towerStructure, Predicates.friendlyStructure),
                Predicates.barracksStructure, Predicates.friendlyStructure,Predicates.queenUnitType, Predicates.friendlyUnit );
        assertTrue(targetSite.siteId == 3);
        logPassed("getClosestFriendlyTower");
    }

    public void getClosestFromListToUnit_good(){
        Site site = target.getClosestFromListToUnit(utils.getAllStructures(), Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
        assertNotNull(site);
        logPassed("getClosestFromListToUnit_good");
    }

    public void getClosestFromListToUnit_fail(){
        Predicate<Structure> badPred = s -> s.structureType == 99;
        Site site = target.getClosestFromListToUnit(utils.getAllStructures(), badPred, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
        assertNull(site);
        logPassed("getClosestFromListToUnit_fail");
    }

    public void getDistanceFromUnitToSite(){

        double distance = target.getDistanceFromUnitToSite(1, Predicates.queenUnitType, Predicates.friendlyUnit);
        assertEquals(127.25, distance, 0.1);
        logPassed("getDistanceFromUnitToSite");
    }

    public void getStructureBySiteId(){
        Structure s = target.getStructureBySiteId(1);
        assertNotNull(s);
        assertEquals(s.siteId, 1);
        logPassed("getStructureBySiteId");
    }

    public void getAllStructuresOfType(){
        List<Structure> structures = target.getAllStructuresOfType(Predicates.barracksStructure, Predicates.friendlyStructure);
        assertEquals(2, structures.size());
        assertEquals(2, structures.get(0).siteId);
        logPassed("getAllStructuresOfType");
    }

    public void getAllUnitsOfType(){
        List<Unit> units = target.getAllUnitsOfType(utils.getAllUnits(),Predicates.archerUnitType, Predicates.enemyUnit);
        assertEquals(3, units.size());
        assertEquals(Constants.ARCHER, units.get(0).unitType);
        assertEquals(Constants.ENEMY_OWNER, units.get(0).owner);

        units = target.getAllUnitsOfType(utils.getAllUnits(),Predicates.archerUnitType, Predicates.friendlyUnit);
        assertEquals(4, units.size());

        units = target.getAllUnitsOfType(utils.getAllUnits(),Predicates.knightUnitType, Predicates.friendlyUnit);
        assertEquals(3, units.size());

        logPassed("getAllUnitsOfType");
    }

    public void getUnitTypeOf(){
        String unitType = target.unitTypeOf(Constants.QUEEN);
        assertEquals("Queen", unitType );
        logPassed("getUnitTypeOf");
    }

}

class QueenDecisionTests {

    private QueenDecisionMaker target;
    private final TestUtils utils;

    public QueenDecisionTests( QueenDecisionMaker target, TestUtils utils){
        this.target = target;
        this.utils = utils;
    }

    public static void main(String[] strings){
        TestUtils utils = new TestUtils();
        QueenDecisionMaker target = new QueenDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getAllStructures(), utils.getAllUnits());
        QueenDecisionTests tests = new QueenDecisionTests(target, utils);

        tests.isInstanceOf();
        tests.canBuildStructure();
        tests.getNextStructureType_archer();
        tests.getNextStructureType_knight();
        tests.getNextStructureType_tower();
        tests.getNextStructureType_giant();
        tests.getNextStructureType_mine();
        tests.move_healthy();
    }

    public void isInstanceOf(){
        assertNotNull(target);
        logPassed("isInstanceOf");
    }

    public void canBuildStructure(){
        boolean canBuild = target.canBuildStructure();
        assertTrue(canBuild);
        logPassed("canBuildStructure");
    }

    public void getNextStructureType_archer(){
        target = new QueenDecisionMaker(utils.gold, utils.touchedSite, utils.sites, new ArrayList<>(), utils.getAllUnits());
        assertEquals("BARRACKS-ARCHER", target.getNextStructureType());
        logPassed("getNextStructureType_archer");
    }

    public void getNextStructureType_knight(){
        target = new QueenDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getStructureForKnights(), utils.getAllUnits());
        assertEquals("BARRACKS-KNIGHT", target.getNextStructureType());
        logPassed("getNextStructureType_knight");
    }

    public void getNextStructureType_tower(){
        target = new QueenDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getStructuresForTower(), utils.getAllUnits());
        assertEquals("TOWER", target.getNextStructureType());
        logPassed("getNextStructureType_tower");
    }

    public void getNextStructureType_giant(){
        target = new QueenDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getStructuresForGiant(), utils.getAllUnits());
        assertEquals("BARRACKS-GIANT", target.getNextStructureType());
        logPassed("getNextStructureType_giant");
    }

    public void getNextStructureType_mine(){
        target = new QueenDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getStructureForMines(), utils.getAllUnits());
        assertEquals("MINE", target.getNextStructureType());
        logPassed("getNextStructureType_mine");
    }

    public void move_healthy(){
        System.out.println( target.move() );
    }
}

class TrainingDecisionTests {

    private TrainingDecisionMaker target;
    private TestUtils utils;

    public TrainingDecisionTests( TrainingDecisionMaker target, TestUtils utils){
        this.target = target;
        this.utils = utils;
    }

    public static void main(String[] strings) {
        TestUtils utils = new TestUtils();
        TrainingDecisionMaker target = new TrainingDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getAllStructures(), utils.getAllUnits());
        TrainingDecisionTests tests = new TrainingDecisionTests(target, utils);

        tests.getWhichTypeToBuild_no_units();
        tests.getWhichTypeToBuild_knights();
        tests.getWhichTypeToBuild_archers();
        tests.getWhichTypeToBuild_giants();
        tests.capableOfTraining_no_friendly_structures();
        tests.capableOfTraining_no_gold_for_archers();
        tests.capableOfTraining_no_gold_for_knights();
        tests.capableOfTraining_no_gold_for_giants();
        tests.capableOfTraining_happy();
        tests.capableOfTraining_no_factory_available();

    }

    public void getWhichTypeToBuild_no_units(){
        target =  new TrainingDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getAllStructures(), new ArrayList<>());
        assertEquals(Constants.KNIGHT, target.getWhichTypeToBuild());
        logPassed("getWhichTypeToBuild_no_units");
    }

    public void getWhichTypeToBuild_knights(){
        target =  new TrainingDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getAllStructures(), utils.getMostlyArchers());
        assertEquals(Constants.KNIGHT, target.getWhichTypeToBuild());
        logPassed("getWhichTypeToBuild_knights");
    }

    public void getWhichTypeToBuild_archers(){
        target =  new TrainingDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getAllStructures(), utils.getMostlyKnights());
        assertEquals(Constants.ARCHER, target.getWhichTypeToBuild());
        logPassed("getWhichTypeToBuild_archers");
    }

    public void getWhichTypeToBuild_giants(){
        target =  new TrainingDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getAllStructures(), utils.getNoGiants());
        assertEquals(Constants.GIANT, target.getWhichTypeToBuild());
        logPassed("getWhichTypeToBuild_giants");
    }

    public void capableOfTraining_no_friendly_structures(){
        assertFalse(target.capableOfTraining(0, 1));
        logPassed("capableOfTraining_no_friendly_structures");
    }

    public void capableOfTraining_no_gold_for_archers(){
        target.gold = 80;
        assertFalse(target.capableOfTraining(1, Constants.ARCHER));
        logPassed("capableOfTraining_no_gold_for_archers");
    }

    public void capableOfTraining_no_gold_for_knights(){
        target.gold = 70;
        assertFalse(target.capableOfTraining(1, Constants.KNIGHT));
        logPassed("capableOfTraining_no_gold_for_knights");
    }

    public void capableOfTraining_no_gold_for_giants(){
        target.gold = 100;
        assertFalse(target.capableOfTraining(1, Constants.GIANT));
        logPassed("capableOfTraining_no_gold_for_giants");
    }

    public void capableOfTraining_happy(){
        target.gold = 140;
        assertTrue(target.capableOfTraining(1, Constants.GIANT));
        logPassed("capableOfTraining_happy");
    }

    public void capableOfTraining_no_factory_available(){
        target.gold = 100;
        target =  new TrainingDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.getGiantFactoryOnly(), utils.getAllUnits());
        assertFalse(target.capableOfTraining(1, Constants.KNIGHT));
        logPassed("capableOfTraining_no_factory_available");
    }
}


class TestUtils {

    public TestUtils(){}

    int gold = 100;
    int touchedSite = 1;

    // Structures
    Structure structure1 = new Structure(1,0,0,Constants.NO_STRUCTURE, Constants.NO_OWNER, 0, 0);
    Structure structure2 = new Structure(2,0,0,Constants.BARRACKS, Constants.FRIENDLY_OWNER, 0, 0); // Knight
    Structure structure3 = new Structure(3,0,0,Constants.BARRACKS, Constants.FRIENDLY_OWNER, 0, 0); // knight
    Structure structure4 = new Structure(4,0,0,Constants.BARRACKS, Constants.FRIENDLY_OWNER, 0, 1); // archer
    Structure structure5 = new Structure(5,0,0,Constants.BARRACKS, Constants.FRIENDLY_OWNER, 0, 2); // giant
    Structure structure6 = new Structure(6,0,0,Constants.BARRACKS, Constants.ENEMY_OWNER, 0, 0); // ememy knight
    Structure structure7 = new Structure(7,0,0,Constants.MINE, Constants.FRIENDLY_OWNER, 0, 0); // mine
    Structure structure8 = new Structure(8,0,0,Constants.MINE, Constants.ENEMY_OWNER, 0, 0); // enemy knight
    Structure structure9 = new Structure(9,0,0,Constants.MINE, Constants.ENEMY_OWNER, 0, 0); // enemy knight
    Structure structure10 = new Structure(10,0,0,Constants.TOWER, Constants.FRIENDLY_OWNER, 0, 0); // friendly mine

    //List<Structure> structures = Arrays.asList(structure1, structure2, structure3, structure4);

    // Sites
    Site site1 = new Site(1, 100, 100, 30);
    Site site2 = new Site(2, 200, 200, 30);
    Site site3 = new Site(3, 300, 300, 30);
    Site site4 = new Site(4, 400, 400, 30);
    Site site5 = new Site(5, 900, 900, 30);

    List<Site> sites = Arrays.asList(site1, site2, site3, site4, site5);

    // Units
    Unit queen1 = new Unit(10, 10, Constants.FRIENDLY_OWNER, Constants.QUEEN, 100 );
    Unit queen2 = new Unit(1000, 1000, Constants.ENEMY_OWNER, Constants.QUEEN, 100 );
    Unit knight1 = new Unit(1100, 1000, Constants.FRIENDLY_OWNER, Constants.KNIGHT, 100 );
    Unit knight2 = new Unit(1200, 1000, Constants.FRIENDLY_OWNER, Constants.KNIGHT, 100 );
    Unit knight3 = new Unit(1300, 1000, Constants.FRIENDLY_OWNER, Constants.KNIGHT, 100 );
    Unit knight4 = new Unit(1350, 1000, Constants.FRIENDLY_OWNER, Constants.KNIGHT, 100 );
    Unit knight5 = new Unit(100, 100, Constants.ENEMY_OWNER, Constants.KNIGHT, 100 );
    Unit knight6 = new Unit(200, 100, Constants.ENEMY_OWNER, Constants.KNIGHT, 100 );
    Unit knight7 = new Unit(300, 100, Constants.ENEMY_OWNER, Constants.KNIGHT, 100 );
    Unit giant1 = new Unit(350, 100, Constants.FRIENDLY_OWNER, Constants.GIANT, 100 );
    Unit giant2 = new Unit(400, 100, Constants.ENEMY_OWNER, Constants.GIANT, 100 );
    Unit archer1 = new Unit(1150, 1000, Constants.FRIENDLY_OWNER, Constants.ARCHER, 100 );
    Unit archer2 = new Unit(1250, 1000, Constants.FRIENDLY_OWNER, Constants.ARCHER, 100 );
    Unit archer3 = new Unit(1350, 1000, Constants.FRIENDLY_OWNER, Constants.ARCHER, 100 );
    Unit archer4 = new Unit(1400, 1000, Constants.FRIENDLY_OWNER, Constants.ARCHER, 100 );
    Unit archer5 = new Unit(150, 100, Constants.ENEMY_OWNER, Constants.ARCHER, 100 );
    Unit archer6 = new Unit(250, 100, Constants.ENEMY_OWNER, Constants.ARCHER, 100 );
    Unit archer7 = new Unit(350, 100, Constants.ENEMY_OWNER, Constants.ARCHER, 100 );
    Unit badUnit = new Unit(901, 901, 10, 20, 0);


    public static void logPassed(String msg){
        System.out.println(msg + " passed");
    }

    public List<Unit> getMostlyArchers(){
        List<Unit> units = new ArrayList<>();
        units.add(knight1);
        units.add(archer1);
        units.add(archer2);
        units.add(archer3);
        units.add(archer4);
        return units;
    }

    public List<Unit> getMostlyKnights(){
        List<Unit> units = new ArrayList<>();
        units.add(knight1);
        units.add(knight2);
        units.add(knight3);
        units.add(knight4);
        units.add(archer1);
        return units;
    }

    public List<Unit> getNoGiants(){
        List<Unit> units = new ArrayList<>();
        units.add(knight1);
        units.add(knight2);
        units.add(knight3);
        units.add(knight4);
        units.add(archer1);
        units.add(archer2);
        return units;
    }

    public List<Unit> getAllUnits(){
        List<Unit> units = new ArrayList<>();
        units.add(queen1);
        units.add(queen2);
        units.add(knight1);
        units.add(knight2);
        units.add(knight3);
        units.add(knight4);
        units.add(knight5);
        units.add(knight6);
        units.add(giant1);
        units.add(giant2);
        units.add(archer1);
        units.add(archer2);
        units.add(archer3);
        units.add(archer4);
        units.add(archer5);
        units.add(archer6);
        units.add(archer7);
        units.add(badUnit);
        return units;
    }

    public List<Structure> getAllStructures(){
        List<Structure> structures = new ArrayList<>();
        structures.add(structure1);
        structures.add(structure2);
        structures.add(structure3);
        structures.add(structure4);
        structures.add(structure5);
        return structures;
    }

    public List<Structure> getGiantFactoryOnly(){
        List<Structure> giantFactory = new ArrayList<>();
        giantFactory.add(structure3);
        return giantFactory;
    }

    public List<Structure> getStructureForMines(){
        List<Structure> structures = new ArrayList<>();
        structures.add(structure7);
        structures.add(structure8);
        structures.add(structure9);
        return structures;
    }

    public List<Structure> getStructureForKnights(){
        List<Structure> structures = new ArrayList<>();
        structures.add(structure4);
        structures.add(structure5);
        structures.add(structure7);
        structures.add(structure10);
        return structures;
    }

    public List<Structure> getStructuresForTower(){
        List<Structure> structures = new ArrayList<>();
        structures.add(structure4);
        structures.add(structure5);
        structures.add(structure7);
        structures.add(structure3);
        return structures;
    }

    public List<Structure> getStructuresForGiant(){
        List<Structure> structures = new ArrayList<>();
        structures.add(structure2);
        structures.add(structure4);
        structures.add(structure7);
        structures.add(structure10);
        return structures;
    }
}

