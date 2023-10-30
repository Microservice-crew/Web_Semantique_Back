package com.example.socialmediawebsemantique;

import com.example.socialmediawebsemantique.tools.jenaEngine;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class GroupController {

    Model model = jenaEngine.readModel("data/oneZero.owl");

    @GetMapping("/GroupSearch")
    @CrossOrigin(origins = "*")
    public String getGroups(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "capacity", required = false) String capacity
    ) {
        String NS = "";
        JsonArray jsonArray = null;
        if (model != null) {
            NS = model.getNsPrefixURI("");

            Model inferredModel = jenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_Group.txt");

            if (name != null && !name.isEmpty()) {
                queryStr = queryStr.replace("?nameParam", '\"' + name + '\"');
            } else {
                queryStr = queryStr.replace("FILTER (?nameParam != \"\" && ?name = ?nameParam)", "");
            }

            if (date != null && !date.isEmpty()) {
                queryStr = queryStr.replace("?dateParam", '\"' + date + '\"');
            } else {
                queryStr = queryStr.replace("FILTER (?dateParam != \"\" && ?date = ?dateParam)", "");
            }

            if (capacity != null && !capacity.isEmpty()) {
                queryStr = queryStr.replace("?capacityParam", '\"' + capacity + '\"');
            } else {
                queryStr = queryStr.replace("FILTER (?capacityParam != \"\" && ?capacity = ?capacityParam)", "");
            }

            Query query = QueryFactory.create(queryStr);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel);
            ResultSet results = qexec.execSelect();

            jsonArray = new JsonArray();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("Group", solution.get("Group").toString());
                jsonObject.put("name", solution.get("name").toString());

                // Extracting the corrected date
                String dateString = solution.get("date").toString();
                try {
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date dateObject = parser.parse(dateString.split("\\^\\^")[0]);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = formatter.format(dateObject);
                    jsonObject.put("date", formattedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Extracting the capacity as an integer
                String capacityString = solution.get("capacity").toString();
                String capacityValue = capacityString.split("\\^\\^")[0];
                try {
                    int capacityInt = Integer.parseInt(capacityValue);
                    jsonObject.put("capacity", capacityInt);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                jsonArray.add(jsonObject);
            }

            String jsonResult = jsonArray != null ? jsonArray.toString() : "";
            System.out.println(jsonResult);
            return jsonResult;
        }
        return null;
    }

}
