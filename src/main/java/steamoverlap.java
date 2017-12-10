import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class steamoverlap {

    final static String API_KEY = "XXXXX";

    public static void main(String[] args) {

        Map<String, String> players = new HashMap<>();
        HashMap<String, ArrayList<String>> gamesMap = new HashMap<>();
        players.put("76561197965863638", "Pierre"); // Pierre
        players.put("76561197976344898", "Tobi"); // Tobi
        players.put("76561198028487549", "Robert"); // Robert
        players.put("76561197986948868", "Johannes"); // Johannes
        players.put("76561197985853719", "Frank"); // Frank
        players.put("76561198014515527", "Marcel"); // Marcel
        String appStoreLink = "http://store.steampowered.com/app/";


        for (Map.Entry<String, String> player : players.entrySet()) {
            ArrayList<String> playerApps = getPlayerGames(player.getKey());
            for (String app : playerApps) {
                ArrayList<String> newList;
                if (gamesMap.containsKey(app)) {
                    newList = gamesMap.get(app);
                } else {
                    newList = new ArrayList<>();
                }
                newList.add(player.getValue());
                gamesMap.put(app, newList);
            }
        }

        String result = gamesMap.entrySet().stream()
                .filter(map -> 2 == map.getValue().size())
                .filter(map -> isNotFreeAndMultiplayerGame(map.getKey()))
                .map(map -> map.getValue() + "\n" + appStoreLink + map.getKey() + "\n")
                .collect(Collectors.joining());

        System.out.println(result);

    }

    // http://store.steampowered.com/api/appdetails?appids=

    private static ArrayList<String> getPlayerGames(String playerId) {
        String url = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + API_KEY + "&steamid=" + playerId + "&format=json";
        ArrayList<String> apps = new ArrayList<>();
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream stream = entity.getContent()) {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(stream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("appid\":")) {
                            line = line.replaceAll(",", "");
                            String[] appLine = line.split(": ");
                            apps.add(appLine[1]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apps;
    }

    private static boolean isNotFreeAndMultiplayerGame(String appId) {

        String url = "http://store.steampowered.com/api/appdetails?appids=" + appId;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream stream = entity.getContent()) {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(stream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("\"is_free\":true")) {
                            return false;
                        }
                        if (line.contains("\"id\":1,\"description\":\"Multi-player\"")) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
