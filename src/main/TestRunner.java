package main;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static main.TestUtils.logPassed;

class TestRunner {
    public static void main(String[] args){
        QueryTests.main(new String[]{"run"});
        QueenDecisionTests.main(new String[]{"run"});
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
        Query target = new Query( utils.structures, utils.units, utils.sites);
        QueryTests queryTests = new QueryTests(target, utils);

        queryTests.getSiteById();
        queryTests.getClosestSiteToUnit();
        queryTests.getDistanceFromUnitToSite();
        queryTests.getClosestFromListToUnit_fail();
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

    public void getClosestFromListToUnit_fail(){
        Predicate<Structure> badPred = s -> s.structureType == 99;
        Site site = target.getClosestFromListToUnit(utils.structures, badPred, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
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
        List<Unit> units = target.getAllUnitsOfType(Predicates.archerUnitType, Predicates.enemyUnit);
        assertEquals(3, units.size());
        assertEquals(Constants.ARCHER, units.get(0).unitType);
        assertEquals(Constants.ENEMY_OWNER, units.get(0).owner);
        logPassed("getAllUnitsOfType");
    }

    public void getUnitTypeOf(){
        String unitType = target.unitTypeOf(Constants.QUEEN);
        assertEquals("Queen", unitType );
        logPassed("getUnitTypeOf");
    }

}

class QueenDecisionTests {

    private final QueenDecisionMaker target;

    public QueenDecisionTests( QueenDecisionMaker target){
        this.target = target;
    }

    public static void main(String[] strings){
        TestUtils utils = new TestUtils();
        QueenDecisionMaker target = new QueenDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.structures, utils.units);
        QueenDecisionTests tests = new QueenDecisionTests(target);

        tests.isInstanceOf();
        tests.canBuildStructure();
        tests.getNextStructureType_archer();
        tests.getNextStructureType_knight();
        tests.getNextStructureType_tower();
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
        target.touchedSite = 1;
        assertEquals(" BARRACKS-ARCHER", target.getNextStructureType());
        logPassed("getNextStructureType_archer");
    }

    public void getNextStructureType_knight(){
        target.touchedSite = 2;
        assertEquals(" BARRACKS-KNIGHT", target.getNextStructureType());
        logPassed("getNextStructureType_knight");
    }

    public void getNextStructureType_tower(){
        target.touchedSite = 3;
        assertEquals(" TOWER", target.getNextStructureType());
        logPassed("getNextStructureType_tower");
    }
}

class TrainingDecisionTests {

    private final TrainingDecisionMaker target;

    public TrainingDecisionTests( TrainingDecisionMaker target){
        this.target = target;
    }

    public static void main(String[] strings) {
        TestUtils utils = new TestUtils();
        TrainingDecisionMaker target = new TrainingDecisionMaker(utils.gold, utils.touchedSite, utils.sites, utils.structures, utils.units);
        TrainingDecisionTests tests = new TrainingDecisionTests(target);

        tests.getWhichTypeToBuild();
//        tests.isInstanceOf();
    }

    public void getWhichTypeToBuild(){
        int type = target.getWhichTypeToBuild();
    }
}


class TestUtils {

    public TestUtils(){}

    int gold = 100;
    int touchedSite = 1;

    // Structures
    Structure structure1 = new Structure(1,0,0,Constants.NO_STRUCTURE, Constants.NO_OWNER, 0, 0);
    Structure structure2 = new Structure(2,0,0,Constants.BARRACKS, Constants.FRIENDLY_OWNER, 0, 0);
    Structure structure3 = new Structure(3,0,0,Constants.BARRACKS, Constants.FRIENDLY_OWNER, 0, 0);
    Structure structure4 = new Structure(4,0,0,Constants.BARRACKS, Constants.ENEMY_OWNER, 0, 0);

    List<Structure> structures = Arrays.asList(structure1, structure2, structure3, structure4);

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
    Unit knight4 = new Unit(100, 100, Constants.ENEMY_OWNER, Constants.KNIGHT, 100 );
    Unit knight5 = new Unit(200, 100, Constants.ENEMY_OWNER, Constants.KNIGHT, 100 );
    Unit knight6 = new Unit(300, 100, Constants.ENEMY_OWNER, Constants.KNIGHT, 100 );
    Unit archer1 = new Unit(1150, 1000, Constants.FRIENDLY_OWNER, Constants.ARCHER, 100 );
    Unit archer2 = new Unit(1250, 1000, Constants.FRIENDLY_OWNER, Constants.ARCHER, 100 );
    Unit archer3 = new Unit(1350, 1000, Constants.FRIENDLY_OWNER, Constants.ARCHER, 100 );
    Unit archer4 = new Unit(150, 100, Constants.ENEMY_OWNER, Constants.ARCHER, 100 );
    Unit archer5 = new Unit(250, 100, Constants.ENEMY_OWNER, Constants.ARCHER, 100 );
    Unit archer6 = new Unit(350, 100, Constants.ENEMY_OWNER, Constants.ARCHER, 100 );
    Unit badUnit = new Unit(901, 901, 10, 20, 0);

    List<Unit> units = Arrays.asList(queen1, queen2, knight1, knight2, knight3, knight4, knight5, knight6, archer1, archer2, archer3, archer4, archer5, archer6, badUnit);

    public static void logPassed(String msg){
        System.out.println(msg + " passed");
    }

}

