package com.example.socialmediawebsemantique;

import com.example.socialmediawebsemantique.tools.jenaEngine;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
            try (QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel)) {
                ResultSet results = qexec.execSelect();

                jsonArray = new JsonArray();
                while (results.hasNext()) {
                    QuerySolution solution = results.next();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.put("id", solution.get("id").toString().split("\\^\\^")[0]); // Extract the ID without the datatype
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            String jsonResult = jsonArray != null ? jsonArray.toString() : "";
            System.out.println(jsonResult);
            return jsonResult;
        }
        return null;
    }



    @DeleteMapping("/deleteGroup")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteGroup(@RequestParam("id") Integer id) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?Group rdf:type ns:Group;\n" +
                "         ns:id ?id;\n" +
                "         ns:name ?name;\n" +
                "         ns:date ?date;\n" +
                "         ns:capacity ?capacity;\n" +

                "} WHERE {\n" +
                "  ?Group rdf:type ns:Group;\n" +
                "         ns:id ?id;\n" +
                "         ns:name ?name;\n" +
                "         ns:date ?date;\n" +
                "         ns:capacity ?capacity;\n" +
                "  FILTER (?id = " + id + ")\n" +
                "}\n";

        System.out.println(sparqlDeleteQuery);



        // Exécuter la requête SPARQL pour supprimer l'individu
        UpdateAction.parseExecute(sparqlDeleteQuery, ontModel);

        // Enregistrer le modèle RDF mis à jour
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression du groupe.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Groupe supprimé avec succès.");
    }






}





