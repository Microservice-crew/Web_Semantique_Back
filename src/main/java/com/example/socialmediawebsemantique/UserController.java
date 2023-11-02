package com.example.socialmediawebsemantique;

import com.example.socialmediawebsemantique.tools.jenaEngine;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
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
public class UserController {

    Model model = jenaEngine.readModel("data/oneZero.owl");

    // Users:



    @GetMapping("/verifutilisateurSearch")
    @CrossOrigin(origins = "*")
    public String getVerifiedUsers(
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

            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_VerifiedUser.txt");



            // Set the value of ?domainParam
            if (domain != null && !domain.isEmpty()) {
                // Replace the parameter placeholder with the actual domain value
                queryStr = queryStr.replace("?nomUserParam", '\"' + domain + '\"');
            } else {
                // If domain is not provided, remove the parameter and the FILTER condition from the query
                queryStr = queryStr.replace("FILTER (?nomUserParam != \"\" && ?nomUser = ?nomUserParam)", "");
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
                jsonObject.put("VerifiedUser", solution.get("VerifiedUser").toString());
                jsonObject.put("nomUser", solution.get("nomUser").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("email", solution.get("email").toString());
                jsonObject.put("phone", solution.get("phone").toString());
                jsonObject.put("age", solution.get("age").toString());
                jsonObject.put("badge", solution.get("badge").toString());
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



    @GetMapping("/utilisateurSearch")
    @CrossOrigin(origins = "*")
    public String getUsers(
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

            String queryStr = FileManager.get().readWholeFileAsUTF8("data/query_User.txt");



            // Set the value of ?domainParam
            if (domain != null && !domain.isEmpty()) {
                // Replace the parameter placeholder with the actual domain value
                queryStr = queryStr.replace("?nomUserParam", '\"' + domain + '\"');
            } else {
                // If domain is not provided, remove the parameter and the FILTER condition from the query
                queryStr = queryStr.replace("FILTER (?nomUserParam != \"\" && ?nomUser = ?nomUserParam)", "");
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
                jsonObject.put("User", solution.get("User").toString());
                jsonObject.put("nomUser", solution.get("nomUser").toString());
                jsonObject.put("title", solution.get("title").toString());
                jsonObject.put("email", solution.get("email").toString());
                jsonObject.put("age", solution.get("age").toString());
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





    //Create User

    @PostMapping("/createUser")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createUser(@RequestBody UserModel userRequest) throws ParseException {
        // Extract data from the EventRequest object
        Integer id = userRequest.getId();
        String nomUser = userRequest.getNomUser();
        String title = userRequest.getTitle();
        String email = userRequest.getEmail();
        Integer age = userRequest.getAge();
        String dateString = userRequest.getDate();


        // Load RDF data from a file
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Create an OntModel for inferencing with the correct namespace
        String NS = "http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#";
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Parse the date from the string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = dateFormat.parse(dateString);

        // Create the RDF properties using the correct URIs
        String userURI = NS + "User_" + generateUniqueID();
        String dateURI = NS + "date";
        String nomUserURI = NS + "nomUser";
        String titleURI = NS + "title";
        String emailURI = NS + "email";
        String ageURI = NS + "age";
        String idURI = NS + "id";


        // Create an individual for the new event with the appropriate URI
        Individual newUser = ontModel.createIndividual(userURI, ontModel.createClass(NS + "User"));

        // Set the properties of the event using the correct URIs
        newUser.addProperty(ontModel.getProperty(idURI), ontModel.createTypedLiteral(id, XSDDatatype.XSDinteger));
        newUser.addProperty(ontModel.getProperty(titleURI), title);
        newUser.addProperty(ontModel.getProperty(emailURI), email);
        newUser.addProperty(ontModel.getProperty(dateURI), ontModel.createTypedLiteral(date, XSDDatatype.XSDdateTime));
        newUser.addProperty(ontModel.getProperty(nomUserURI), nomUser);
        newUser.addProperty(ontModel.getProperty(ageURI), String.valueOf(age));

        // Save the updated RDF model
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("data/oneZero.owl"))) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create the user.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("User created successfully.");
    }

    // Generate a unique ID for the new event
    private String generateUniqueID() {
        return String.valueOf(System.currentTimeMillis());
    }



    //Delete User
    // Supprimer un post par title en utilisant une requête SPARQL
    @DeleteMapping("/deleteUser")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> deleteUser(@RequestParam("title") String title) {
        // Charger les données RDF depuis un fichier
        Model model = jenaEngine.readModel("data/oneZero.owl");

        // Créer un modèle Ont qui effectue des inférences
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, model);

        // Construire la requête SPARQL pour supprimer l'individu par ID
        String sparqlDeleteQuery = "PREFIX ns: <http://www.semanticweb.org/sadok/ontologies/2023/9/untitled-ontology-9#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "DELETE {\n" +
                "  ?User rdf:type ns:User;\n" +
                "         ns:title ?title;\n" +
                "         ns:nomUser ?nomUser;\n" +
                "         ns:email ?email;\n" +
                "         ns:age ?age;\n" +
                "         ns:date ?date;\n" +
                "} WHERE {\n" +
                "  ?User rdf:type ns:User;\n" +
                "         ns:title ?title;\n" +
                "         ns:nomUser ?nomUser;\n" +
                "         ns:email ?email;\n" +
                "         ns:age ?age;\n" +
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la suppression de User.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("User supprimé avec succès.");
    }









}
