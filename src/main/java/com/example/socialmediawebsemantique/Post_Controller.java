package com.example.socialmediawebsemantique;

import com.example.socialmediawebsemantique.tools.jenaEngine;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
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
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(value = "Type", required = false) String Type,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam (value = "attribute" ,required = false) String attribute,
            @RequestParam (value = "regexParam" , required = false) String regexParam


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

            if (orderBy != null && !orderBy.isEmpty() && Type != null && !Type.isEmpty()
                    && (Type.toUpperCase().equals("ASC") || Type.toUpperCase().equals("DESC"))) {
                queryStr = queryStr.replace("?orderBy",  '?'+orderBy.toLowerCase() );
                queryStr = queryStr.replace("?Type",  Type.toUpperCase() );
            } else {
                queryStr = queryStr.replace("ORDER BY ?Type(?orderBy)", "");
            }

            if (regexParam != null && !regexParam.isEmpty() && attribute != null && !attribute.isEmpty()) {
                queryStr = queryStr.replace("?regexParam", '\"' + regexParam + '\"');
                queryStr = queryStr.replace("?attribute",  '?'+attribute.toLowerCase() );
            } else {
                queryStr = queryStr.replace("FILTER regex(?attribute, ?regexParam, \"i\")", "");
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








    //Delete Post
    // Supprimer un post par title en utilisant une requête SPARQL
    @DeleteMapping("/deletePost")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deletePost(@RequestParam("title") String title) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?Post rdf:type ns:Post;\n" +
                "         ns:title ?title;\n" +
                "         ns:nomUser ?nomUser;\n" +
                "         ns:contenu ?contenu;\n" +
                "         ns:date ?date;\n" +
                "} WHERE {\n" +
                "  ?Post rdf:type ns:Post;\n" +
                "         ns:title ?title;\n" +
                "         ns:nomUser ?nomUser;\n" +
                "         ns:contenu ?contenu;\n" +
                "         ns:date ?date;\n" +
                "FILTER (?title = \"" + title + "\")\n" +
                "}\n";

        System.out.println(sparqlDeleteQuery);



        // Exécuter la requête SPARQL pour supprimer l'individu
        UpdateAction.parseExecute(sparqlDeleteQuery, ontModel);

        // Enregistrer le modèle RDF mis à jour
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression de la Post.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Post supprimé avec succès.");
    }






    //Delete Comment
    // Supprimer un comment par title en utilisant une requête SPARQL
    @DeleteMapping("/deleteComment")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteComment(@RequestParam("title") String title) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?Comment rdf:type ns:Comment;\n" +
                "         ns:title ?title;\n" +
                "         ns:nomUser ?nomUser;\n" +
                "         ns:contenu ?contenu;\n" +
                "         ns:date ?date;\n" +
                "} WHERE {\n" +
                "  ?Comment rdf:type ns:Comment;\n" +
                "         ns:title ?title;\n" +
                "         ns:nomUser ?nomUser;\n" +
                "         ns:contenu ?contenu;\n" +
                "         ns:date ?date;\n" +
                "FILTER (?title = \"" + title + "\")\n" +
                "}\n";

        System.out.println(sparqlDeleteQuery);



        // Exécuter la requête SPARQL pour supprimer l'individu
        UpdateAction.parseExecute(sparqlDeleteQuery, ontModel);

        // Enregistrer le modèle RDF mis à jour
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression de la Comment.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Comment supprimé avec succès.");
    }



    //Create Post

    @PostMapping("/createPost")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createPost(@RequestBody PostModel postRequest) throws ParseException {
        // Extract data from the EventRequest object
        Integer id = postRequest.getId();
        String nomUser = postRequest.getNomUser();
        String title = postRequest.getTitle();
        String contenu = postRequest.getContenu();
        String dateString = postRequest.getDate();


        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Parse the date from the string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = dateFormat.parse(dateString);

        // Create the RDF properties using the correct URIs
        String eventURI = NS + "Post_" + generateUniqueID();
        String dateURI = NS + "date";
        String nomUserURI = NS + "nomUser";
        String titleURI = NS + "title";
        String contenuURI = NS + "contenu";
        String idURI = NS + "id";


        // Create an individual for the new event with the appropriate URI
        Individual newPost = ontModel.createIndividual(eventURI, ontModel.createClass(NS + "Post"));

        // Set the properties of the event using the correct URIs
        newPost.addProperty(ontModel.getProperty(idURI), ontModel.createTypedLiteral(id, XSDDatatype.XSDinteger));
        newPost.addProperty(ontModel.getProperty(titleURI), title);
        newPost.addProperty(ontModel.getProperty(contenuURI), contenu);
        newPost.addProperty(ontModel.getProperty(dateURI), ontModel.createTypedLiteral(date, XSDDatatype.XSDdateTime));
        newPost.addProperty(ontModel.getProperty(nomUserURI), nomUser);

        // Save the updated RDF model
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create the post.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Post created successfully.");
    }

    // Generate a unique ID for the new event
    private String generateUniqueID() {
        return String.valueOf(System.currentTimeMillis());
    }



    //modifier Post

    @PutMapping("/updatePost")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> updatePost(@RequestBody PostModel postRequest, @RequestParam("id") Integer id) {
        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Define the properties and URIs to update
        String dateURI = NS + "date";
        String nomUserURI = NS + "nomUser";
        String titleURI = NS + "title";
        String contenuURI = NS + "contenu";

        // Use a prepared SPARQL query to avoid injection vulnerabilities and improve readability
        String sparqlFindQuery = String.format(
                "SELECT ?Post WHERE { ?Post <%s> ?id. FILTER (?id = %d) }",
                NS + "id",
                id
        );

        QueryExecution findQueryExec = QueryExecutionFactory.create(sparqlFindQuery, ontModel);
        ResultSet findResults = findQueryExec.execSelect();

        if (findResults.hasNext()) {
            // Individual with the specified ID exists, proceed with the update
            Individual existingPost = ontModel.getIndividual(findResults.next().getResource("Post").getURI());

            // Extract the data to update from the request

            String newNomUser = postRequest.getNomUser();
            String newTitle = postRequest.getTitle();
            String newContenu = postRequest.getContenu();
            String newDateString = postRequest.getDate();

            // Parse the new date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                Date newDate = dateFormat.parse(newDateString);
                Literal newDateLiteral = ontModel.createTypedLiteral(newDate, XSDDatatype.XSDdateTime);

                // Update the properties of the existing event
                existingPost.setPropertyValue(ontModel.getProperty(titleURI), ontModel.createLiteral(newTitle));
                existingPost.setPropertyValue(ontModel.getProperty(contenuURI), ontModel.createLiteral(newContenu));
                existingPost.setPropertyValue(ontModel.getProperty(dateURI), newDateLiteral);
                existingPost.setPropertyValue(ontModel.getProperty(nomUserURI), ontModel.createLiteral(newNomUser));
            } catch (ParseException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format.");
            }

            // Save the updated RDF model
            try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the post.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Post updated successfully.");
        } else {
            // Individual with the specified ID doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post with ID " + id + " not found.");
        }
    }


    //Post ById :


    @GetMapping("/getPostById")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> getPostById(@RequestParam("id") Integer id) {
        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Define the properties and URIs to retrieve



        String dateURI = NS + "date";
        String nomUserURI = NS + "nomUser";
        String titleURI = NS + "title";
        String contenuURI = NS + "contenu";

        // Find the individual by its ID
        // Find the individual by its ID
        String sparqlFindQuery = "SELECT ?title ?contenu ?date ?nomUser WHERE { " +
                "?Post <" + NS + "id> ?id. " +
                "  FILTER (?id = " + id + ")" +
                "?Post <" + titleURI + "> ?title. " +
                "?Post <" + contenuURI + "> ?contenu. " +
                "?Post <" + dateURI + "> ?date. " +
                "?Post <" + nomUserURI + "> ?nomUser. " +
                "}";



        QueryExecution findQueryExec = QueryExecutionFactory.create(sparqlFindQuery, ontModel);
        ResultSet findResults = findQueryExec.execSelect();

        if (findResults.hasNext()) {
            // Individual with the specified ID exists, retrieve its data
            QuerySolution solution = findResults.next();
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("title", solution.get("title").toString());
            jsonObject.put("contenu", solution.get("contenu").toString());
            jsonObject.put("date", solution.get("date").toString());
            jsonObject.put("nomUser", solution.get("nomUser").toString());

            // Convert the JSON to a string
            String jsonResult = jsonObject.toString();
            return ResponseEntity.status(HttpStatus.OK).body(jsonResult);
        } else {
            // Individual with the specified ID doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post with ID " + id + " not found.");
        }
    }


}
