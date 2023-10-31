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

@RestController
public class Post_Controller {

    Model model = jenaEngine.readModel("data/oneZero.owl");


    @GetMapping("/CommentSearch")
    @CrossOrigin(origins = "*")
    public String getComments(
            @RequestParam(value = "domain", required = false) String domain
    ) {
        String NS = "";
        // lire le model a partir d'une ontologie
        JsonArray jsonArray = null;
        if (model != null) {
            // lire le Namespace de l�ontologie
            NS = model.getNsPrefixURI("");

            // apply our rules on the owlInferencedModel
            Model inferedModel = jenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // query on the model after inference


            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_Comment.txt");


            // Set the value of ?domainParam
            if (domain != null && !domain.isEmpty()) {
                // Replace the parameter placeholder with the actual domain value
                queryStr = queryStr.replace("?titleParam", '\"' + domain + '\"');
            } else {
                // If domain is not provided, remove the parameter and the FILTER condition from the query
                queryStr = queryStr.replace("FILTER (?titleParam != \"\" && ?title = ?titleParam)", "");
            }

            System.out.println(queryStr);
            // Execute the query
            Query query = QueryFactory.create(queryStr);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferedModel);

            // Execute the query
            ResultSet results = qexec.execSelect();

            jsonArray = new JsonArray();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("Comment", solution.get("Comment").toString());
                jsonObject.put("nomUser", solution.get("nomUser").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("contenu", solution.get("contenu").toString());
                jsonObject.put("date", solution.get("date").toString());

                jsonArray.add(jsonObject);
            }


        }

        // Convert the JSON to a string
        String jsonResult = jsonArray.toString();

        System.out.println(jsonResult);
        return jsonResult;




    }


    @GetMapping("/PostSearch")
    @CrossOrigin(origins = "*")
    public String getPosts(
            @RequestParam(value = "domain", required = false) String domain
    ) {
        String NS = "";
        // lire le model a partir d'une ontologie
        JsonArray jsonArray = null;
        if (model != null) {
            // lire le Namespace de l�ontologie
            NS = model.getNsPrefixURI("");

            // apply our rules on the owlInferencedModel
            Model inferedModel = jenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // query on the model after inference
            /*OutputStream res =  JenaEngine.executeQueryFile(inferedModel, "data/query_Skill.txt");

            System.out.println(res);
            return res.toString();*/

            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_Post.txt");



            // Set the value of ?domainParam
            if (domain != null && !domain.isEmpty()) {
                // Replace the parameter placeholder with the actual domain value
                queryStr = queryStr.replace("?titleParam", '\"' + domain + '\"');
            } else {
                // If domain is not provided, remove the parameter and the FILTER condition from the query
                queryStr = queryStr.replace("FILTER (?titleParam != \"\" && ?title = ?titleParam)", "");
            }

            System.out.println(queryStr);
            // Execute the query
            // Exécuter la requête pour les événements en ligne
            Query query = QueryFactory.create(queryStr);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferedModel);
            ResultSet results = qexec.execSelect();

// Créer un tableau JSON pour stocker les résultats de la requête sur les événements en ligne
            jsonArray = new JsonArray();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("Post", solution.get("Post").toString());
                jsonObject.put("nomUser", solution.get("nomUser").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("contenu", solution.get("contenu").toString());
                String dateValue = solution.get("date").toString();
                String cleanedDate = dateValue.replace("^^http://www.w3.org/2001/XMLSchema#dateTime", "");

                jsonObject.put("date", cleanedDate);
                jsonArray.add(jsonObject);
            }



// Convertir le JSON en une chaîne
            String jsonResult = jsonArray.toString();

            System.out.println(jsonResult);
            return jsonResult;


        }

        return null;


    }

}