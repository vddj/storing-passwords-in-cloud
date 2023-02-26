package DiplomskiRad;

import com.google.api.services.drive.model.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author PC
 */
public class JSONfiles {

    public static JSONObject createNewObject(String web, String user, String pass, String str, String time) {

        JSONObject objectDetails = new JSONObject();
        objectDetails.put("Website", web);
        objectDetails.put("Username", user);
        objectDetails.put("Password", pass);
        objectDetails.put("Strength", str);
        objectDetails.put("Date&Time", time);
        JSONObject object = new JSONObject();
        object.put("password", objectDetails);

        return object;
    }

    public static void writeJSONfile(File fileFromDrive) {
        JSONArray jsonArray = new JSONArray();
        //fileFromDrive.
        try ( FileWriter file = new FileWriter("../app/src/main/resources/example.json")) {
            file.write(jsonArray.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rewriteJSONfile(Object objekti[]) {

        JSONArray jsonArray = new JSONArray();
        for (Object object : objekti) {
            String s = object.toString();
            s = s.substring(1, s.length() - 1);
            String[] tokens = s.split(", ");
            System.out.println("JSONfiles -> rewriteJSONfile -> " + Arrays.toString(tokens));
            JSONObject myObject = JSONfiles.createNewObject(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]);
            jsonArray.add(myObject);
        }

        try ( FileWriter file = new FileWriter("../app/src/main/resources/example.json")) {
            file.write(jsonArray.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray readFromJSONfile() {
        JSONParser jsonParser = new JSONParser();

        try ( FileReader reader = new FileReader("../app/src/main/resources/example.json")) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray lista = (JSONArray) obj;
            System.out.println("JSONfiles -> readFromJSONfile -> \n\t" + lista);
            return lista;
        } catch (FileNotFoundException e) {
            System.out.println("JSONfiles -> readFromJSONfile -> Ne postoji fajl example.json");
            return null;
        } catch (IOException | ParseException e) {
            System.out.println("JSONfiles -> readFromJSONfile -> File empty");
            return null;
        }
    }

    public static String[] parseObject(JSONObject object) {
        JSONObject objDetails = (JSONObject) object.get("password");
        String web = (String) objDetails.get("Website");
        String user = (String) objDetails.get("Username");
        String pass = (String) objDetails.get("Password");
        String str = (String) objDetails.get("Strength");
        String time = (String) objDetails.get("Date&Time");
        String ret[] = {web, user, pass, str, time};
        return ret;
    }

    public static String getPassForThis() {
        JSONArray passwords = JSONfiles.readFromJSONfile();
        if (passwords == null) {
            return "";
        }

        for (int i = 0; i < passwords.size(); i++) {
            JSONObject passJSON = (JSONObject) passwords.get(i);
            JSONObject passJSON1 = (JSONObject) passJSON.get("password");
            //System.out.println("JSONfiles -> getPassForThis -> object: " + (String) passJSON1.get("Website"));
            if ("this".equals((String) passJSON1.get("Website"))) {
                System.out.println("JSONfiles -> getPassForThis -> ////////// " + (String) passJSON1.get("Password"));
                return (String) passJSON1.get("Password");
            }
        }
        return "";
    }

    public static void main(String args[]) {
        JSONfiles.getPassForThis();
    }
}
