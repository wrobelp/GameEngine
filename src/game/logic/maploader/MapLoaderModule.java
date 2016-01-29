package game.logic.maploader;

import engine.utilities.ErrorHandler;
import game.Game;
import game.place.Place;
import game.place.map.Map;
import game.place.map.WarpPoint;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by przemek on 18.08.15.
 */
public class MapLoaderModule implements Runnable {

    private ArrayList<Map> maps = new ArrayList<>(); // TODO zamienić na wczytywanie z pliku
    private MapLoadContainer list1 = new MapLoadContainer();
    private MapLoadContainer list2 = new MapLoadContainer();
    private MapLoadContainer resourcelist1 = new MapLoadContainer();
    private MapLoadContainer resourcelist2 = new MapLoadContainer();
    private boolean run, firstActive, pause, firstResourceActive;
    private Game game;

    public MapLoaderModule(Game game) {
        this.game = game;
    }

    private Map loadMap(String name) {
        for (Map map : maps) {
            if (map.getName() == name) {
                return map;
            }
        }
        return null;
    }

    @Override
    public void run() {
        maps.addAll(game.getPlace().maps.stream().collect(Collectors.toList()));
        run = true;
        while (run) {
            try {
                loadMaps();
                loadTextures();
//                game.getPlace().getSprite("bark", "");
            } catch (Exception exception) {
                ErrorHandler.swallowLogAndPrint(exception);
            }
        }
    }

    private void loadMaps() {
        if (this.game != null) {
            Place place = game.getPlace();
            if (place != null && !pause) {
                MapLoadContainer workingList = firstActive ? list1 : list2;
                if (workingList != null) {
                    for (int i = 0; i < workingList.size(); i++) {
                        MapLoad mapLoad = workingList.get(i);
                        Map placeMap = mapLoad.map;
                        if (placeMap == null) {
                            placeMap = loadMap(mapLoad.name);   //TODO Informacja które areas wczytać z pliku!
                            loadAreas(mapLoad, placeMap);
                            ArrayList<Map> workingMap = (place.firstMapsToAddActive ? place.mapsToAdd2 : place.mapsToAdd1);
                            workingMap.add(placeMap);
                        } else {
                            loadAreas(mapLoad, placeMap);
                        }
                    }
                    workingList.clear();
                }
                firstActive = !firstActive;
            }
        }
    }

    private void loadTextures() {
        if (this.game != null) {
            Place place = game.getPlace();
            if (place != null && !pause) {
//                MapLoadContainer workingList = firstActive ? list1 : list2;
//                if (workingList != null) {
//                    for (int i = 0; i < workingList.size(); i++) {
//                        MapLoad mapLoad = workingList.get(i);
//                        Map placeMap = mapLoad.map;
//                        if (placeMap == null) {
//                            placeMap = loadMap(mapLoad.name);   //TODO Informacja które areas wczytać z pliku!
//                            loadAreas(mapLoad, placeMap);
//                            ArrayList<Map> workingMap = (place.firstMapsToAddActive ? place.mapsToAdd2 : place.mapsToAdd1);
//                            workingMap.add(placeMap);
//                        } else {
//                            loadAreas(mapLoad, placeMap);
//                        }
//                    }
//                    workingList.clear();
//                }
//                firstActive = !firstActive;
            }
        }
    }

    private void loadAreas(MapLoad mapLoad, Map placeMap) {
        for (int area : mapLoad.areas) {            // TODO przerobić na wczytywanie z pliku
            if (placeMap != null && placeMap.areas[area] == null) {
                placeMap.areas[area] = placeMap.areasCopies[area];
            }
        }
    }

    public synchronized void requestMap(String name, WarpPoint warp) {
        pause = true;
        Iterable<Integer> areas = new ArrayList<>(1);
        MapLoadContainer workingList = firstActive ? list2 : list1;
        workingList.add(name, areas);
        pause = false;
    }

    public synchronized void updateList(Set<Map> tempMaps) {
        pause = true;
        MapLoadContainer workingList = firstActive ? list2 : list1;
        for (Map map : tempMaps) {
            workingList.add(map.getName(), map.getAreasToUpdate(), map);
        }
        pause = false;
    }


    public void stop() {
        list1.clear();
        list2.clear();
        maps.clear();
        run = false;
    }

    public boolean isRunning() {
        return run;
    }
}
